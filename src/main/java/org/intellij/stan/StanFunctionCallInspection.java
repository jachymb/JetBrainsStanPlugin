package org.intellij.stan;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.intellij.stan.psi.StanTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class StanFunctionCallInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitFile(@NotNull PsiFile file) {
                ASTNode root = file.getNode();
                Map<String, String> typeMap = StanSignatureDatabase.buildTypeMap(root);
                Set<String> userFunctions = collectUserDefinedFunctions(root);
                checkCalls(root, typeMap, userFunctions, holder);
            }
        };
    }

    private Set<String> collectUserDefinedFunctions(ASTNode root) {
        Set<String> names = new HashSet<>();
        collectUserFunctionsRec(root, names);
        return names;
    }

    private void collectUserFunctionsRec(ASTNode node, Set<String> names) {
        if (node.getElementType() == StanTypes.FUNCTION_DEF) {
            // function_def ::= return_type decl_identifier LPAREN …
            // Walk to the first ident node before LPAREN.
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                if (c.getElementType() == StanTypes.LPAREN) break;
                if (c.getElementType() == StanTypes.DECL_IDENTIFIER) {
                    names.add(c.getText()); // .getText() on DECL_IDENTIFIER gets the full name text
                    break;
                }
            }
            return;
        }
        for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext())
            collectUserFunctionsRec(c, names);
    }

    private void checkCalls(ASTNode node, Map<String, String> typeMap,
                            Set<String> userFunctions, ProblemsHolder holder) {
        IElementType t = node.getElementType();
        if (t == StanTypes.FUN_CALL_EXPR || t == StanTypes.COND_DIST_EXPR) {
            checkSingleCall(node, typeMap, userFunctions, holder);
        }
        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext())
            checkCalls(child, typeMap, userFunctions, holder);
    }

    private void checkSingleCall(ASTNode callNode, Map<String, String> typeMap,
                                 Set<String> userFunctions, ProblemsHolder holder) {
        // fun_call_expr / cond_dist_expr ::= ident LPAREN …
        // The first child is the ident node; findLeaf() digs to the IDENTIFIER token.
        ASTNode identNode = callNode.getFirstChildNode(); // ident wrapper
        if (identNode == null || identNode.getElementType() != StanTypes.IDENT) return;

        // Use the leaf for highlighting (the actual IDENTIFIER token).
        ASTNode nameLeaf = StanSyntaxUtil.findLeaf(identNode);
        if (nameLeaf == null) return;
        String fnName = nameLeaf.getText();

        StanSignatureDatabase db = StanSignatureDatabase.getInstance();
        boolean isBuiltin = db.hasFunction(fnName);

        // conditioning-suffix functions called without bar notation with 2+ args.
        if (callNode.getElementType() == StanTypes.FUN_CALL_EXPR
                && StanSyntaxUtil.hasConditioningSuffix(fnName)) {
            List<ASTNode> args = collectCallArgExprs(callNode);
            if (args.size() >= 2) {
                holder.registerProblem(identNode.getPsi(),
                        "'" + fnName + "' requires conditional notation: " + fnName + "(sample | params)",
                        ProblemHighlightType.GENERIC_ERROR);
                return;
            }
        }

        // Undefined function.
        if (!isBuiltin && !userFunctions.contains(fnName)) {
            holder.registerProblem(identNode.getPsi(),
                    "Undefined function '" + fnName + "'",
                    ProblemHighlightType.GENERIC_ERROR);
            return;
        }

        if (!isBuiltin) return;

        List<ASTNode> argExprs = collectCallArgExprs(callNode);
        int actualArity = argExprs.size();

        List<StanSignatureDatabase.Signature> sigs = db.getSignatures(fnName);
        if (sigs.isEmpty()) return;

        // ── Arity check ───────────────────────────────────────────────────────
        boolean arityOk = sigs.stream().anyMatch(s -> s.args.size() == actualArity);
        if (!arityOk) {
            IntSummaryStatistics stats = sigs.stream().mapToInt(s -> s.args.size()).summaryStatistics();
            String expected = stats.getMin() == stats.getMax()
                    ? String.valueOf(stats.getMin())
                    : stats.getMin() + "–" + stats.getMax();
            holder.registerProblem(identNode.getPsi(),
                    "'" + fnName + "' expects " + expected + " argument(s), got " + actualArity,
                    ProblemHighlightType.GENERIC_ERROR);
            return;
        }

        // ── Type check ────────────────────────────────────────────────────────
        List<String> actualTypes = new ArrayList<>(actualArity);
        for (ASTNode arg : argExprs)
            actualTypes.add(StanSignatureDatabase.inferExprType(arg, typeMap));

        if (actualTypes.stream().noneMatch(Objects::nonNull)) return;

        boolean matched = sigs.stream()
                .filter(s -> s.args.size() == actualArity)
                .anyMatch(s -> sigMatches(s, actualTypes));
        if (matched) return;

        for (int i = 0; i < actualArity; i++) {
            final String actual = actualTypes.get(i);
            if (actual == null) continue;
            final int pos = i;
            boolean compatibleWithAny = sigs.stream()
                    .filter(s -> s.args.size() > pos)
                    .anyMatch(s -> StanSignatureDatabase.isCompatible(s.args.get(pos), actual));
            if (!compatibleWithAny) {
                List<String> candidates = new ArrayList<>();
                for (StanSignatureDatabase.Signature s : sigs) {
                    if (s.args.size() == actualArity) {
                        String exp = s.args.get(pos);
                        if (!candidates.contains(exp)) candidates.add(exp);
                    }
                }
                String expectedStr = candidates.isEmpty() ? "a compatible type"
                        : String.join(" or ", candidates);
                holder.registerProblem(argExprs.get(pos).getPsi(),
                        "Argument " + (pos + 1) + " to '" + fnName + "': expected "
                                + expectedStr + ", got " + actual,
                        ProblemHighlightType.GENERIC_ERROR);
                return;
            }
        }
    }

    private boolean sigMatches(StanSignatureDatabase.Signature sig, List<String> actualTypes) {
        for (int i = 0; i < sig.args.size(); i++) {
            String actual = actualTypes.get(i);
            if (actual == null) continue;
            if (!StanSignatureDatabase.isCompatible(sig.args.get(i), actual)) return false;
        }
        return true;
    }

    /** Collect argument expressions from a fun_call_expr or cond_dist_expr. */
    private List<ASTNode> collectCallArgExprs(ASTNode callNode) {
        List<ASTNode> result = new ArrayList<>();
        boolean inArgs = false;
        for (ASTNode c = callNode.getFirstChildNode(); c != null; c = c.getTreeNext()) {
            IElementType ct = c.getElementType();
            if (ct == StanTypes.LPAREN) { inArgs = true; continue; }
            if (ct == StanTypes.RPAREN) break;
            if (!inArgs) continue;
            if (ct == StanTypes.COMMA || ct == StanTypes.BAR) continue;
            if (c.getFirstChildNode() != null) result.add(c);
        }
        return result;
    }
}
