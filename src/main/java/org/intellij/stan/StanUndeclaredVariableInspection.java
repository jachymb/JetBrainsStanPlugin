package org.intellij.stan;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Flags uses of undeclared variables, respecting Stan's block-level waterfall scope:
 *
 *   functions        — only own function parameters (+ other function names for calls)
 *   data             — data variables only
 *   transformed data — data + transformed data
 *   parameters       — data + transformed data + parameters
 *   transformed parameters — ... + parameters
 *   model / generated quantities — all of the above (local only, not exported)
 *
 * Within every block, declarations are sequential (must declare before use).
 * Local { } blocks and for-loop variables are scoped to their construct.
 */
public class StanUndeclaredVariableInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitFile(@NotNull PsiFile file) {
                checkProgram(file.getNode(), holder);
            }
        };
    }

    // ── Scope stack ───────────────────────────────────────────────────────────

    private static final class Scope {
        private final Deque<Set<String>> frames = new ArrayDeque<>();

        Scope()                    { frames.push(new HashSet<>()); }
        Scope(Set<String> initial) { frames.push(new HashSet<>(initial)); }

        void push()               { frames.push(new HashSet<>()); }
        void pop()                { frames.pop(); }
        void declare(String name) { frames.peek().add(name); }

        boolean contains(String name) {
            for (Set<String> f : frames) if (f.contains(name)) return true;
            return false;
        }

        Set<String> flatten() {
            Set<String> all = new HashSet<>();
            for (Set<String> f : frames) all.addAll(f);
            return all;
        }
    }

    // ── Program entry ─────────────────────────────────────────────────────────

    private void checkProgram(ASTNode root, ProblemsHolder holder) {
        // Waterfall: each block's top-level variables become visible in subsequent blocks.
        Set<String> accum = new HashSet<>();

        for (ASTNode block = root.getFirstChildNode(); block != null; block = block.getTreeNext()) {
            IElementType bt = block.getElementType();

            if (bt == StanElementTypes.FUNCTIONS_BLOCK) {
                checkFunctionsBlock(block, holder);

            } else if (bt == StanElementTypes.DATA_BLOCK
                    || bt == StanElementTypes.TRANSFORMED_DATA_BLOCK
                    || bt == StanElementTypes.PARAMETERS_BLOCK
                    || bt == StanElementTypes.TRANSFORMED_PARAMETERS_BLOCK) {
                Scope scope = new Scope(accum);
                Set<String> exports = processBlockBody(block, scope, true, holder);
                accum.addAll(exports);

            } else if (bt == StanElementTypes.MODEL_BLOCK
                    || bt == StanElementTypes.GENERATED_QUANTITIES_BLOCK) {
                processBlockBody(block, new Scope(accum), false, holder);
            }
        }
    }

    // ── Functions block ───────────────────────────────────────────────────────

    private void checkFunctionsBlock(ASTNode funcBlock, ProblemsHolder holder) {
        // Collect all function names so that mutual/forward calls are not flagged.
        Set<String> funcNames = new HashSet<>();
        for (ASTNode c = funcBlock.getFirstChildNode(); c != null; c = c.getTreeNext()) {
            if (c.getElementType() == StanElementTypes.FUN_DEF) {
                ASTNode name = funDefName(c);
                if (name != null) funcNames.add(name.getText());
            }
        }
        for (ASTNode c = funcBlock.getFirstChildNode(); c != null; c = c.getTreeNext()) {
            if (c.getElementType() == StanElementTypes.FUN_DEF)
                checkFunDef(c, funcNames, holder);
        }
    }

    @Nullable
    private ASTNode funDefName(ASTNode funDef) {
        for (ASTNode c = funDef.getFirstChildNode(); c != null; c = c.getTreeNext()) {
            if (c.getElementType() == StanTokenTypes.LPAREN) break;
            IElementType ct = c.getElementType();
            if (ct == StanTokenTypes.IDENTIFIER || ct == StanTokenTypes.BUILTIN_FUNCTION)
                return c;
        }
        return null;
    }

    private void checkFunDef(ASTNode funDef, Set<String> funcNames, ProblemsHolder holder) {
        // Function body sees only other function names and its own formal parameters.
        Scope scope = new Scope(funcNames);
        // Parameters are wrapped in a PARAM_LIST child of FUN_DEF; ARG_DECLs are inside it.
        for (ASTNode c = funDef.getFirstChildNode(); c != null; c = c.getTreeNext()) {
            if (c.getElementType() == StanElementTypes.PARAM_LIST) {
                for (ASTNode param = c.getFirstChildNode(); param != null; param = param.getTreeNext()) {
                    if (param.getElementType() == StanElementTypes.ARG_DECL) {
                        // ARG_DECL: [DATA_KW] unsizedType IDENTIFIER — name is the last token
                        for (ASTNode gc = param.getFirstChildNode(); gc != null; gc = gc.getTreeNext()) {
                            IElementType gt = gc.getElementType();
                            if (gt == StanTokenTypes.IDENTIFIER || gt == StanTokenTypes.BUILTIN_FUNCTION)
                                scope.declare(gc.getText());
                        }
                    }
                }
                break;
            }
        }
        for (ASTNode c = funDef.getFirstChildNode(); c != null; c = c.getTreeNext()) {
            if (c.getElementType() == StanElementTypes.BLOCK_STMT) {
                processBlockBody(c, scope, false, holder);
                break;
            }
        }
    }

    // ── Block body ────────────────────────────────────────────────────────────

    /**
     * Walk the direct children of a block node as a sequential statement list.
     *
     * @param exportTopLevel if true, top-level VAR_DECL names are returned so the
     *                       caller can add them to the waterfall accumulator.
     * @return names declared at the top level of this block (empty if !exportTopLevel).
     */
    private Set<String> processBlockBody(ASTNode blockNode, Scope scope,
                                          boolean exportTopLevel, ProblemsHolder holder) {
        Set<String> exports = new HashSet<>();
        for (ASTNode c = blockNode.getFirstChildNode(); c != null; c = c.getTreeNext())
            processOneStmt(c, scope, exportTopLevel ? exports : null, holder);
        return exports;
    }

    // ── Statement types ───────────────────────────────────────────────────────

    private static final Set<IElementType> STMT_TYPES = new HashSet<>(Arrays.asList(
            StanElementTypes.VAR_DECL,
            StanElementTypes.ASSIGNMENT_STMT,
            StanElementTypes.FUN_CALL_STMT,
            StanElementTypes.TARGET_PLUS_ASSIGN_STMT,
            StanElementTypes.JACOBIAN_PLUS_ASSIGN_STMT,
            StanElementTypes.TILDE_STMT,
            StanElementTypes.RETURN_STMT,
            StanElementTypes.PRINT_STMT,
            StanElementTypes.REJECT_STMT,
            StanElementTypes.FATAL_ERROR_STMT,
            StanElementTypes.BREAK_STMT,
            StanElementTypes.CONTINUE_STMT,
            StanElementTypes.SKIP_STMT,
            StanElementTypes.IF_STMT,
            StanElementTypes.WHILE_STMT,
            StanElementTypes.FOR_RANGE_STMT,
            StanElementTypes.FOR_EACH_STMT,
            StanElementTypes.PROFILE_STMT,
            StanElementTypes.BLOCK_STMT
    ));

    // ── Statement dispatch ────────────────────────────────────────────────────

    /**
     * Process one child node inside a block.
     * Non-statement tokens (keywords, braces, semicolons) are silently ignored.
     *
     * @param exports if non-null, names from top-level VAR_DECLs are collected here.
     */
    private void processOneStmt(ASTNode node, Scope scope,
                                 @Nullable Set<String> exports, ProblemsHolder holder) {
        IElementType t = node.getElementType();
        if (!STMT_TYPES.contains(t)) return;

        // ── Variable declaration ──────────────────────────────────────────────
        if (t == StanElementTypes.VAR_DECL) {
            // Check constraint/size expressions and initializers before declaring names
            // (a variable is not in scope for its own declaration expression).
            checkVarDeclExprs(node, scope, holder);
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                if (c.getElementType() == StanElementTypes.DECLARED_VAR) {
                    ASTNode nameNode = c.getFirstChildNode();
                    if (nameNode != null && nameNode.getElementType() == StanTokenTypes.IDENTIFIER) {
                        String name = nameNode.getText();
                        scope.declare(name);
                        if (exports != null) exports.add(name);
                    }
                }
            }
            return;
        }

        // ── For loops (range and for-each) ────────────────────────────────────
        if (t == StanElementTypes.FOR_RANGE_STMT || t == StanElementTypes.FOR_EACH_STMT) {
            // Children: FOR_KW LPAREN IDENTIFIER IN_KW [range/iter exprs] RPAREN body
            // The body is always the last child.
            ASTNode body = node.getLastChildNode();
            String loopVar = null;
            boolean seenIn = false;
            for (ASTNode c = node.getFirstChildNode(); c != body && c != null; c = c.getTreeNext()) {
                IElementType ct = c.getElementType();
                if (ct == StanTokenTypes.FOR_KW || ct == StanTokenTypes.LPAREN
                        || ct == StanTokenTypes.RPAREN || ct == StanTokenTypes.COLON) continue;
                if (ct == StanTokenTypes.IN_KW) { seenIn = true; continue; }
                if (!seenIn && ct == StanTokenTypes.IDENTIFIER) { loopVar = c.getText(); continue; }
                if (seenIn) checkRefs(c, scope, holder); // range start/end or iterable
            }
            scope.push();
            if (loopVar != null) scope.declare(loopVar);
            if (body != null) processOneStmt(body, scope, null, holder);
            scope.pop();
            return;
        }

        // ── Local block ───────────────────────────────────────────────────────
        if (t == StanElementTypes.BLOCK_STMT) {
            scope.push();
            processBlockBody(node, scope, false, holder);
            scope.pop();
            return;
        }

        // ── If statement ──────────────────────────────────────────────────────
        if (t == StanElementTypes.IF_STMT) {
            // Children: IF_KW LPAREN condition RPAREN then-stmt [ELSE_KW else-stmt]
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                IElementType ct = c.getElementType();
                if (ct == StanTokenTypes.IF_KW || ct == StanTokenTypes.LPAREN
                        || ct == StanTokenTypes.RPAREN || ct == StanTokenTypes.ELSE_KW) continue;
                if (STMT_TYPES.contains(ct)) processOneStmt(c, scope, null, holder);
                else checkRefs(c, scope, holder);
            }
            return;
        }

        // ── While statement ───────────────────────────────────────────────────
        if (t == StanElementTypes.WHILE_STMT) {
            // Children: WHILE_KW LPAREN condition RPAREN body
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                IElementType ct = c.getElementType();
                if (ct == StanTokenTypes.WHILE_KW || ct == StanTokenTypes.LPAREN
                        || ct == StanTokenTypes.RPAREN) continue;
                if (STMT_TYPES.contains(ct)) processOneStmt(c, scope, null, holder);
                else checkRefs(c, scope, holder);
            }
            return;
        }

        // ── Profile statement (creates a local scope like a block) ────────────
        if (t == StanElementTypes.PROFILE_STMT) {
            scope.push();
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext())
                processOneStmt(c, scope, null, holder);
            scope.pop();
            return;
        }

        // ── All other statements: check contained variable references ─────────
        checkRefs(node, scope, holder);
    }

    // ── Expression reference checking ─────────────────────────────────────────

    /**
     * Check constraint/size expressions and initializers inside a VAR_DECL.
     * Skips the declared name(s) — they are not yet in scope.
     */
    private void checkVarDeclExprs(ASTNode varDecl, Scope scope, ProblemsHolder holder) {
        for (ASTNode c = varDecl.getFirstChildNode(); c != null; c = c.getTreeNext()) {
            if (c.getElementType() == StanElementTypes.DECLARED_VAR) {
                boolean nameSkipped = false;
                for (ASTNode gc = c.getFirstChildNode(); gc != null; gc = gc.getTreeNext()) {
                    if (!nameSkipped && gc.getElementType() == StanTokenTypes.IDENTIFIER) {
                        nameSkipped = true; // skip the name being declared
                        continue;
                    }
                    checkRefs(gc, scope, holder);
                }
            } else {
                checkRefs(c, scope, holder); // type node: sizes, constraints, etc.
            }
        }
    }

    /**
     * Recursively check variable references in any expression or simple-statement node.
     * Does NOT change scope — call processOneStmt for scope-creating constructs.
     */
    private void checkRefs(ASTNode node, Scope scope, ProblemsHolder holder) {
        IElementType t = node.getElementType();

        if (t == StanElementTypes.VARIABLE_EXPR) {
            ASTNode id = node.getFirstChildNode();
            if (id != null && id.getElementType() == StanTokenTypes.IDENTIFIER)
                flagIfUndeclared(id, scope, holder);
            return;
        }

        if (t == StanElementTypes.ASSIGNMENT_STMT) {
            checkAssignmentRefs(node, scope, holder);
            return;
        }

        for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext())
            checkRefs(c, scope, holder);
    }

    private void checkAssignmentRefs(ASTNode assignStmt, Scope scope, ProblemsHolder holder) {
        boolean seenOp = false;
        for (ASTNode c = assignStmt.getFirstChildNode(); c != null; c = c.getTreeNext()) {
            IElementType ct = c.getElementType();
            if (!seenOp && StanTokenTypes.ASSIGNMENT_OPS.contains(ct)) {
                seenOp = true;
            } else if (!seenOp) {
                // lvalue region
                if (ct == StanTokenTypes.IDENTIFIER)              flagIfUndeclared(c, scope, holder);
                else if (ct == StanElementTypes.TUPLE_DECL_PACK)  checkTupleLvalueRefs(c, scope, holder);
                else if (ct == StanElementTypes.INDEX_LIST)       checkRefs(c, scope, holder);
            } else {
                checkRefs(c, scope, holder); // RHS expression
            }
        }
    }

    private void checkTupleLvalueRefs(ASTNode tupleNode, Scope scope, ProblemsHolder holder) {
        for (ASTNode c = tupleNode.getFirstChildNode(); c != null; c = c.getTreeNext()) {
            IElementType ct = c.getElementType();
            if (ct == StanTokenTypes.IDENTIFIER)             flagIfUndeclared(c, scope, holder);
            else if (ct == StanElementTypes.TUPLE_DECL_PACK) checkTupleLvalueRefs(c, scope, holder);
            else if (ct == StanElementTypes.INDEX_LIST)      checkRefs(c, scope, holder);
        }
    }

    private void flagIfUndeclared(ASTNode identNode, Scope scope, ProblemsHolder holder) {
        String name = identNode.getText();
        if (!scope.contains(name))
            holder.registerProblem(identNode.getPsi(),
                    "Variable '" + name + "' is not declared",
                    ProblemHighlightType.GENERIC_ERROR);
    }
}
