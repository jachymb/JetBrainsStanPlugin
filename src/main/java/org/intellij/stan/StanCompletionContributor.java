package org.intellij.stan;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class StanCompletionContributor extends CompletionContributor {

    private static final List<String> BLOCK_COMPLETIONS = Arrays.asList(
        "functions", "data", "transformed data",
        "parameters", "transformed parameters", "model", "generated quantities"
    );

    private static final List<String> TYPE_KEYWORDS = Arrays.asList(
        "int", "real", "complex",
        "vector", "row_vector", "matrix",
        "complex_vector", "complex_row_vector", "complex_matrix",
        "array", "tuple", "void",
        "ordered", "positive_ordered", "simplex", "unit_vector",
        "sum_to_zero_vector", "sum_to_zero_matrix",
        "cholesky_factor_corr", "cholesky_factor_cov",
        "corr_matrix", "cov_matrix",
        "column_stochastic_matrix", "row_stochastic_matrix"
    );

    // `return` is excluded and added only inside function bodies.
    private static final List<String> STATEMENT_KEYWORDS = Arrays.asList(
        "if", "else", "for", "while", "break", "continue",
        "print", "reject", "fatal_error", "profile", "target", "jacobian"
    );

    private static final String[] DIST_SUFFIXES =
        {"_lpdf", "_lpmf", "_lupdf", "_lupmf"};

    // ── Position-classification token sets ────────────────────────────────────

    private static final TokenSet EXPR_PREV_TOKENS = TokenSet.create(
        StanTokenTypes.PLUS, StanTokenTypes.MINUS,
        StanTokenTypes.TIMES, StanTokenTypes.DIVIDE,
        StanTokenTypes.MODULO, StanTokenTypes.IDIVIDE, StanTokenTypes.LDIVIDE,
        StanTokenTypes.ELT_TIMES, StanTokenTypes.ELT_DIVIDE,
        StanTokenTypes.POW, StanTokenTypes.ELT_POW,
        StanTokenTypes.OR, StanTokenTypes.AND,
        StanTokenTypes.EQUALS, StanTokenTypes.NEQUALS,
        StanTokenTypes.LESS, StanTokenTypes.LEQ, StanTokenTypes.GREATER, StanTokenTypes.GEQ,
        StanTokenTypes.BANG,
        StanTokenTypes.ASSIGN, StanTokenTypes.PLUS_ASSIGN, StanTokenTypes.MINUS_ASSIGN,
        StanTokenTypes.TIMES_ASSIGN, StanTokenTypes.DIVIDE_ASSIGN,
        StanTokenTypes.ELT_TIMES_ASSIGN, StanTokenTypes.ELT_DIVIDE_ASSIGN, StanTokenTypes.ARROW,
        StanTokenTypes.TILDE, StanTokenTypes.BAR, StanTokenTypes.COLON, StanTokenTypes.QUESTION,
        StanTokenTypes.LPAREN, StanTokenTypes.COMMA, StanTokenTypes.LBRACKET,
        StanTokenTypes.RETURN_KW, StanTokenTypes.IN_KW
    );

    private static final TokenSet STMT_START_PREV_TOKENS = TokenSet.orSet(
        StanTokenTypes.TYPE_KEYWORDS,
        TokenSet.create(
            StanTokenTypes.LBRACE, StanTokenTypes.RBRACE,
            StanTokenTypes.SEMICOLON, StanTokenTypes.ELSE_KW
        )
    );

    // ── PSI-ancestor sets for pass-2 classification ───────────────────────────

    /**
     * Expression-structural ancestors that are unambiguous: being inside one of these
     * means we are in an expression.  FUN_CALL_EXPR and VARIABLE_EXPR are excluded —
     * they appear both as expression-statement heads and as nested sub-expressions.
     */
    private static final TokenSet EXPR_PSI_ANCESTORS = TokenSet.create(
        StanElementTypes.ARG_LIST,
        StanElementTypes.BINARY_OP_EXPR,
        StanElementTypes.PREFIX_OP_EXPR,
        StanElementTypes.POSTFIX_OP_EXPR,
        StanElementTypes.TERNARY_IF_EXPR,
        StanElementTypes.PAREN_EXPR,
        StanElementTypes.INDEXED_EXPR,
        StanElementTypes.ARRAY_EXPR,
        StanElementTypes.ROW_VECTOR_EXPR,
        StanElementTypes.TUPLE_EXPR,
        StanElementTypes.COND_DIST_EXPR,
        StanElementTypes.TARGET_EXPR,
        StanElementTypes.TUPLE_PROJECTION_EXPR,
        StanElementTypes.SINGLE_INDEX,
        StanElementTypes.UPFROM_INDEX,
        StanElementTypes.DOWNFROM_INDEX,
        StanElementTypes.BETWEEN_INDEX,
        StanElementTypes.INDEX_LIST
    );

    private static final TokenSet STMT_PSI_ANCESTORS = TokenSet.create(
        StanElementTypes.VAR_DECL, StanElementTypes.DECLARED_VAR, StanElementTypes.PARAM_LIST,
        StanElementTypes.IF_STMT, StanElementTypes.WHILE_STMT,
        StanElementTypes.FOR_RANGE_STMT, StanElementTypes.FOR_EACH_STMT,
        StanElementTypes.BLOCK_STMT,
        StanElementTypes.FUN_CALL_STMT, StanElementTypes.ASSIGNMENT_STMT,
        StanElementTypes.RETURN_STMT, StanElementTypes.PRINT_STMT,
        StanElementTypes.REJECT_STMT, StanElementTypes.FATAL_ERROR_STMT,
        StanElementTypes.TARGET_PLUS_ASSIGN_STMT, StanElementTypes.JACOBIAN_PLUS_ASSIGN_STMT,
        StanElementTypes.TILDE_STMT, StanElementTypes.PROFILE_STMT,
        StanElementTypes.BREAK_STMT, StanElementTypes.CONTINUE_STMT, StanElementTypes.SKIP_STMT,
        StanElementTypes.FUN_DEF,
        StanElementTypes.FUNCTIONS_BLOCK, StanElementTypes.DATA_BLOCK,
        StanElementTypes.TRANSFORMED_DATA_BLOCK, StanElementTypes.PARAMETERS_BLOCK,
        StanElementTypes.TRANSFORMED_PARAMETERS_BLOCK, StanElementTypes.MODEL_BLOCK,
        StanElementTypes.GENERATED_QUANTITIES_BLOCK
    );

    private enum PositionKind { EXPR, STMT_START, UNKNOWN }

    // ── Type constraint ───────────────────────────────────────────────────────

    /**
     * Predicate used to filter completion candidates by type.
     * {@code candidate} is the inferred return/variable type, or {@code null} when unknown.
     * Unknown types are always accepted.
     */
    @FunctionalInterface
    private interface TypeConstraint {
        boolean accepts(@Nullable String candidate);
    }

    private static final TypeConstraint NO_CONSTRAINT = c -> true;

    // ── Entry point ───────────────────────────────────────────────────────────

    public StanCompletionContributor() {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(StanLanguage.INSTANCE),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters parameters,
                                              @NotNull ProcessingContext context,
                                              @NotNull CompletionResultSet result) {
                    PsiElement position = parameters.getPosition();
                    if (isAfterTilde(position)) {
                        addDistributionCompletions(result);
                        return;
                    }
                    IElementType block = enclosingBlock(position);
                    if (block == null) { addBlockNameCompletions(result); return; }
                    addInsideBlockCompletions(result, block, position);
                }
            }
        );
    }

    // ── Context detection ─────────────────────────────────────────────────────

    private static boolean isAfterTilde(PsiElement position) {
        PsiElement leaf = PsiTreeUtil.prevLeaf(position);
        while (leaf != null) {
            IElementType t = tokenType(leaf);
            if (isTrivia(t)) { leaf = PsiTreeUtil.prevLeaf(leaf); continue; }
            return t == StanTokenTypes.TILDE;
        }
        return false;
    }

    @Nullable
    private static IElementType enclosingBlock(PsiElement element) {
        PsiElement p = element.getParent();
        while (p != null && !(p instanceof PsiFile)) {
            IElementType t = nodeType(p);
            if (t == StanElementTypes.FUNCTIONS_BLOCK
                    || t == StanElementTypes.DATA_BLOCK
                    || t == StanElementTypes.TRANSFORMED_DATA_BLOCK
                    || t == StanElementTypes.PARAMETERS_BLOCK
                    || t == StanElementTypes.TRANSFORMED_PARAMETERS_BLOCK
                    || t == StanElementTypes.MODEL_BLOCK
                    || t == StanElementTypes.GENERATED_QUANTITIES_BLOCK) return t;
            p = p.getParent();
        }
        return null;
    }

    @Nullable
    private static ASTNode enclosingFunDefNode(PsiElement element) {
        PsiElement p = element.getParent();
        while (p != null && !(p instanceof PsiFile)) {
            IElementType t = nodeType(p);
            if (t == StanElementTypes.FUN_DEF) return p.getNode();
            if (t == StanElementTypes.FUNCTIONS_BLOCK || t == StanElementTypes.DATA_BLOCK
                    || t == StanElementTypes.TRANSFORMED_DATA_BLOCK
                    || t == StanElementTypes.PARAMETERS_BLOCK
                    || t == StanElementTypes.TRANSFORMED_PARAMETERS_BLOCK
                    || t == StanElementTypes.MODEL_BLOCK
                    || t == StanElementTypes.GENERATED_QUANTITIES_BLOCK) return null;
            p = p.getParent();
        }
        return null;
    }

    private static boolean isRngAllowed(PsiElement element) {
        PsiElement p = element.getParent();
        while (p != null && !(p instanceof PsiFile)) {
            IElementType t = nodeType(p);
            if (t == StanElementTypes.TRANSFORMED_DATA_BLOCK
                    || t == StanElementTypes.GENERATED_QUANTITIES_BLOCK) return true;
            if (t == StanElementTypes.FUN_DEF) {
                ASTNode def = p.getNode();
                for (ASTNode c = def.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                    if (c.getElementType() == StanTokenTypes.LPAREN) break;
                    IElementType ct = c.getElementType();
                    if (ct == StanTokenTypes.IDENTIFIER || ct == StanTokenTypes.BUILTIN_FUNCTION)
                        return c.getText().endsWith("_rng");
                }
                return false;
            }
            p = p.getParent();
        }
        return false;
    }

    /**
     * Two-pass position classifier.
     * Pass 1: nearest preceding non-trivia token (fast, covers most cases).
     * Pass 2: PSI ancestor walk (for ambiguous tokens like identifiers, literals, closing brackets).
     */
    private static PositionKind classifyPosition(PsiElement position) {
        PsiElement leaf = PsiTreeUtil.prevLeaf(position);
        while (leaf != null) {
            IElementType t = tokenType(leaf);
            if (isTrivia(t)) { leaf = PsiTreeUtil.prevLeaf(leaf); continue; }
            if (EXPR_PREV_TOKENS.contains(t))       return PositionKind.EXPR;
            if (STMT_START_PREV_TOKENS.contains(t)) return PositionKind.STMT_START;
            break;
        }
        if (leaf == null) return PositionKind.STMT_START;

        // Pass 2: PSI ancestors
        PsiElement p = position.getParent();
        while (p != null && !(p instanceof PsiFile)) {
            IElementType t = nodeType(p);
            if (t != null && EXPR_PSI_ANCESTORS.contains(t))  return PositionKind.EXPR;
            if (t != null && STMT_PSI_ANCESTORS.contains(t)) return PositionKind.STMT_START;
            p = p.getParent();
        }
        return PositionKind.UNKNOWN;
    }

    // ── Type-constraint computation ───────────────────────────────────────────

    /**
     * Determines the type constraint for expression completions at the cursor.
     * Walks upward through PSI ancestors, stopping at the first structurally
     * meaningful ancestor that implies a type requirement.
     *
     * <ul>
     *   <li>ARG_LIST  → expected argument type from the function's signature</li>
     *   <li>BINARY_OP_EXPR → must form a common type with the other operand</li>
     *   <li>Index nodes → must be {@code int}</li>
     *   <li>DECLARED_VAR → must be compatible with the declaration's type</li>
     *   <li>ASSIGNMENT_STMT → must be compatible with the LHS variable's type</li>
     * </ul>
     *
     * Transparent wrappers (PAREN_EXPR, VARIABLE_EXPR, FUN_CALL_EXPR, …) are
     * skipped so the constraint propagates to the surrounding context.
     */
    private static TypeConstraint computeExpectedType(PsiElement position, Map<String, String> typeMap) {
        PsiElement p = position.getParent();
        while (p != null && !(p instanceof PsiFile)) {
            IElementType t = nodeType(p);
            if (t == null) { p = p.getParent(); continue; }

            // Transparent wrappers: skip them and keep looking upward.
            if (t == StanElementTypes.PAREN_EXPR
                    || t == StanElementTypes.POSTFIX_OP_EXPR
                    || t == StanElementTypes.VARIABLE_EXPR
                    || t == StanElementTypes.FUN_CALL_EXPR
                    || t == StanElementTypes.COND_DIST_EXPR
                    || t == StanElementTypes.ARRAY_EXPR
                    || t == StanElementTypes.ROW_VECTOR_EXPR
                    || t == StanElementTypes.TUPLE_EXPR) {
                p = p.getParent(); continue;
            }

            // Function call argument: filter by expected arg type from the DB.
            if (t == StanElementTypes.ARG_LIST) return constraintFromArgList(p, position);

            // Binary operator: filter by compatibility with the other operand's type.
            if (t == StanElementTypes.BINARY_OP_EXPR)
                return constraintFromBinaryOp(p.getNode(), position, typeMap);

            // Unary prefix: the operand type depends on the operator; skip for simplicity.
            if (t == StanElementTypes.PREFIX_OP_EXPR) return NO_CONSTRAINT;

            // Ternary: too complex to filter usefully.
            if (t == StanElementTypes.TERNARY_IF_EXPR) return NO_CONSTRAINT;

            // Array index: must be int.
            if (t == StanElementTypes.SINGLE_INDEX || t == StanElementTypes.UPFROM_INDEX
                    || t == StanElementTypes.DOWNFROM_INDEX || t == StanElementTypes.BETWEEN_INDEX
                    || t == StanElementTypes.INDEX_LIST)
                return c -> c == null || "int".equals(c);

            // Variable declaration initializer.
            if (t == StanElementTypes.DECLARED_VAR) {
                PsiElement varDecl = p.getParent();
                if (varDecl != null && nodeType(varDecl) == StanElementTypes.VAR_DECL) {
                    String dt = StanSignatureDatabase.typeNodeToString(
                            varDecl.getNode().getFirstChildNode());
                    if (dt != null) return isCompatibleConstraint(dt);
                }
                return NO_CONSTRAINT;
            }

            // Assignment statement RHS.
            if (t == StanElementTypes.ASSIGNMENT_STMT) {
                String lt = lhsTypeFromAssignment(p.getNode(), typeMap);
                return lt != null ? isCompatibleConstraint(lt) : NO_CONSTRAINT;
            }

            // Any other ancestor (statement, block, …): stop without a constraint.
            return NO_CONSTRAINT;
        }
        return NO_CONSTRAINT;
    }

    /** Constraint that accepts types promotable to {@code expected}. */
    private static TypeConstraint isCompatibleConstraint(String expected) {
        return c -> c == null || StanSignatureDatabase.isCompatible(expected, c);
    }

    private static TypeConstraint constraintFromArgList(PsiElement argList, PsiElement position) {
        int argPos = findArgPosition(argList, position);
        if (argPos < 0) return NO_CONSTRAINT;

        PsiElement callExprPsi = argList.getParent();
        if (callExprPsi == null) return NO_CONSTRAINT;
        IElementType ct = nodeType(callExprPsi);
        if (ct != StanElementTypes.FUN_CALL_EXPR && ct != StanElementTypes.COND_DIST_EXPR)
            return NO_CONSTRAINT;

        ASTNode nameNode = callExprPsi.getNode().getFirstChildNode();
        if (nameNode == null) return NO_CONSTRAINT;
        IElementType nt = nameNode.getElementType();
        if (nt != StanTokenTypes.BUILTIN_FUNCTION && nt != StanTokenTypes.IDENTIFIER)
            return NO_CONSTRAINT;

        List<StanSignatureDatabase.Signature> sigs =
                StanSignatureDatabase.getInstance().getSignatures(nameNode.getText());
        if (sigs.isEmpty()) return NO_CONSTRAINT; // user-defined function — unknown signature

        Set<String> expected = new HashSet<>();
        for (StanSignatureDatabase.Signature sig : sigs)
            if (argPos < sig.args.size() && sig.args.get(argPos) != null)
                expected.add(sig.args.get(argPos));

        if (expected.isEmpty()) return NO_CONSTRAINT;
        return c -> c == null || expected.stream().anyMatch(e -> StanSignatureDatabase.isCompatible(e, c));
    }

    /** 0-based index of the argument slot at {@code position} within {@code argList}. */
    private static int findArgPosition(PsiElement argList, PsiElement position) {
        // Find the direct child of argList that is an ancestor of position.
        PsiElement target = position;
        while (target != null && target.getParent() != argList) {
            target = target.getParent();
            if (target == null || target instanceof PsiFile) return 0;
        }
        // Count commas among argList's direct children that appear before target.
        int commas = 0;
        PsiElement sib = argList.getFirstChild();
        while (sib != null && sib != target) {
            if (nodeType(sib) == StanTokenTypes.COMMA) commas++;
            sib = sib.getNextSibling();
        }
        return commas;
    }

    private static TypeConstraint constraintFromBinaryOp(ASTNode binaryOp,
                                                          PsiElement cursorPos,
                                                          Map<String, String> typeMap) {
        // Collect the two operand expression nodes (first-child composites).
        ASTNode left = null, right = null;
        for (ASTNode c = binaryOp.getFirstChildNode(); c != null; c = c.getTreeNext()) {
            if (c.getFirstChildNode() == null) continue; // token
            if (left == null) left = c;
            else if (right == null) { right = c; break; }
        }
        if (left == null || right == null) return NO_CONSTRAINT;

        boolean cursorInRight = isDescendantOf(cursorPos, right.getPsi());
        ASTNode otherSide = cursorInRight ? left : right;
        String otherType = StanSignatureDatabase.inferExprType(otherSide, typeMap);
        if (otherType == null) return NO_CONSTRAINT;

        return c -> c == null || StanSignatureDatabase.commonType(otherType, c) != null;
    }

    /** LHS type of an ASSIGNMENT_STMT, or null if it cannot be determined. */
    @Nullable
    private static String lhsTypeFromAssignment(ASTNode stmt, Map<String, String> typeMap) {
        // First composite child is the LHS expression.
        for (ASTNode c = stmt.getFirstChildNode(); c != null; c = c.getTreeNext()) {
            if (c.getFirstChildNode() == null) continue;
            IElementType t = c.getElementType();
            if (t == StanElementTypes.VARIABLE_EXPR) {
                ASTNode name = c.getFirstChildNode();
                if (name != null) return typeMap.get(name.getText());
            }
            // Indexed / tuple lvalues: skip (type unwrapping is non-trivial).
            return null;
        }
        return null;
    }

    private static boolean isDescendantOf(PsiElement element, PsiElement ancestor) {
        PsiElement p = element;
        while (p != null && !(p instanceof PsiFile)) {
            if (p == ancestor) return true;
            p = p.getParent();
        }
        return false;
    }

    // ── Scope collection ──────────────────────────────────────────────────────

    /**
     * Variable-name → type-string map for the scope at the cursor.
     * Outside a FUN_DEF: all block-level declarations.
     * Inside a FUN_DEF: function parameters + local declarations + for-loop variables.
     */
    private static Map<String, String> collectScopeVarMap(PsiFile file, @Nullable ASTNode funDef) {
        if (funDef == null) return StanSignatureDatabase.buildTypeMap(file.getNode());
        Map<String, String> map = new HashMap<>(StanSignatureDatabase.buildFunctionParamMap(funDef));
        collectLocalVarTypes(funDef, map);
        return map;
    }

    private static void collectLocalVarTypes(ASTNode node, Map<String, String> out) {
        IElementType t = node.getElementType();
        if (t == StanElementTypes.VAR_DECL) {
            ASTNode typeNode = node.getFirstChildNode();
            String typeStr = StanSignatureDatabase.typeNodeToString(typeNode);
            if (typeStr != null) {
                for (ASTNode c = typeNode.getTreeNext(); c != null; c = c.getTreeNext()) {
                    if (c.getElementType() == StanElementTypes.DECLARED_VAR) {
                        ASTNode name = c.getFirstChildNode();
                        if (name != null && name.getElementType() == StanTokenTypes.IDENTIFIER)
                            out.put(name.getText(), typeStr);
                    }
                }
            }
        } else if (t == StanElementTypes.FOR_RANGE_STMT) {
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                if (c.getElementType() == StanTokenTypes.IN_KW) break;
                if (c.getElementType() == StanTokenTypes.IDENTIFIER) { out.put(c.getText(), "int"); break; }
            }
        } else if (t == StanElementTypes.FOR_EACH_STMT) {
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                if (c.getElementType() == StanTokenTypes.IN_KW) break;
                if (c.getElementType() == StanTokenTypes.IDENTIFIER) { out.put(c.getText(), null); break; }
            }
        }
        for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext())
            collectLocalVarTypes(c, out);
    }

    /**
     * User-defined function name → return type string (null = void or unresolvable).
     * Return type is read from the FUN_DEF node: the first child before the function
     * name identifier, interpreted via {@code typeNodeToString}.
     */
    private static Map<String, String> collectUserFunctionMap(PsiFile file) {
        Map<String, String> map = new LinkedHashMap<>();
        collectFunDefs(file.getNode(), map);
        return map;
    }

    private static void collectFunDefs(ASTNode node, Map<String, String> out) {
        if (node.getElementType() == StanElementTypes.FUN_DEF) {
            String name = null;
            String retType = null;
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                if (c.getElementType() == StanTokenTypes.LPAREN) break;
                IElementType ct = c.getElementType();
                if (ct == StanTokenTypes.IDENTIFIER || ct == StanTokenTypes.BUILTIN_FUNCTION) {
                    name = c.getText();
                } else if (name == null) {
                    // Still in return-type position.
                    String s = StanSignatureDatabase.typeNodeToString(c);
                    if (s != null) retType = s;
                }
            }
            if (name != null) out.put(name, retType);
            return; // no nested function definitions
        }
        for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext())
            collectFunDefs(c, out);
    }

    // ── Completion builders ───────────────────────────────────────────────────

    private static void addBlockNameCompletions(CompletionResultSet result) {
        for (String name : BLOCK_COMPLETIONS)
            result.addElement(LookupElementBuilder.create(name).bold());
    }

    private static void addDistributionCompletions(CompletionResultSet result) {
        Set<String> baseNames = new LinkedHashSet<>();
        for (String name : StanSignatureDatabase.getInstance().getDistributionFunctionNames()) {
            for (String suffix : DIST_SUFFIXES) {
                if (name.endsWith(suffix)) {
                    baseNames.add(name.substring(0, name.length() - suffix.length())); break;
                }
            }
        }
        for (String bn : baseNames) result.addElement(LookupElementBuilder.create(bn));
    }

    private static void addInsideBlockCompletions(
            CompletionResultSet result, IElementType block, PsiElement position) {

        boolean isDataOrParams = block == StanElementTypes.DATA_BLOCK
                              || block == StanElementTypes.PARAMETERS_BLOCK;
        ASTNode funDef         = enclosingFunDefNode(position);
        boolean insideFunDef   = funDef != null;
        boolean funTopLevel    = block == StanElementTypes.FUNCTIONS_BLOCK && !insideFunDef;
        boolean declarationsOnly = isDataOrParams || funTopLevel;

        PositionKind kind = declarationsOnly ? PositionKind.STMT_START : classifyPosition(position);

        if (kind == PositionKind.EXPR || kind == PositionKind.UNKNOWN) {
            PsiFile file = position.getContainingFile();
            Map<String, String> typeMap = collectScopeVarMap(file, funDef);
            TypeConstraint constraint = (kind == PositionKind.EXPR)
                    ? computeExpectedType(position, typeMap)
                    : NO_CONSTRAINT;

            if (kind == PositionKind.UNKNOWN) {
                // Also add declaration / control-flow keywords as fallback.
                for (String kw : TYPE_KEYWORDS)
                    result.addElement(LookupElementBuilder.create(kw).bold());
                for (String kw : STATEMENT_KEYWORDS)
                    result.addElement(LookupElementBuilder.create(kw).bold());
                if (insideFunDef)
                    result.addElement(LookupElementBuilder.create("return").bold());
            }
            addExprCompletions(result, position, file, typeMap, constraint);

        } else { // STMT_START
            for (String kw : TYPE_KEYWORDS)
                result.addElement(LookupElementBuilder.create(kw).bold());
            if (!declarationsOnly) {
                for (String kw : STATEMENT_KEYWORDS)
                    result.addElement(LookupElementBuilder.create(kw).bold());
                if (insideFunDef)
                    result.addElement(LookupElementBuilder.create("return").bold());
            }
        }
    }

    /**
     * Adds builtin functions, user-defined functions, and in-scope variables,
     * each filtered by {@code constraint}.
     */
    private static void addExprCompletions(CompletionResultSet result, PsiElement position,
                                           PsiFile file, Map<String, String> typeMap,
                                           TypeConstraint constraint) {
        boolean rngAllowed = isRngAllowed(position);
        StanSignatureDatabase db = StanSignatureDatabase.getInstance();

        // Builtin functions: include if any overload's return type satisfies the constraint.
        for (String name : db.getFunctionNames()) {
            if (!rngAllowed && name.endsWith("_rng")) continue;
            boolean include = db.getSignatures(name).stream()
                    .anyMatch(s -> constraint.accepts(s.ret));
            if (include) result.addElement(LookupElementBuilder.create(name));
        }

        // User-defined functions: filter by extracted return type.
        for (Map.Entry<String, String> e : collectUserFunctionMap(file).entrySet()) {
            if (constraint.accepts(e.getValue()))
                result.addElement(LookupElementBuilder.create(e.getKey())
                        .withTypeText("user fn", true));
        }

        // In-scope variables: filter by declared type.
        for (Map.Entry<String, String> e : typeMap.entrySet()) {
            if (constraint.accepts(e.getValue()))
                result.addElement(LookupElementBuilder.create(e.getKey())
                        .withTypeText(e.getValue() != null ? e.getValue() : "var", true));
        }
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    @Nullable private static IElementType tokenType(PsiElement e) {
        return e.getNode() != null ? e.getNode().getElementType() : null;
    }

    @Nullable private static IElementType nodeType(PsiElement e) {
        return e.getNode() != null ? e.getNode().getElementType() : null;
    }

    private static boolean isTrivia(IElementType t) {
        return t == StanTokenTypes.WHITE_SPACE
            || t == StanTokenTypes.LINE_COMMENT
            || t == StanTokenTypes.BLOCK_COMMENT;
    }
}
