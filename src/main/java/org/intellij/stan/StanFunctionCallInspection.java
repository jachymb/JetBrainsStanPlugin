package org.intellij.stan;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Checks every built-in function call against the signature database generated
 * from stanc3's Generate.ml.  Reports wrong argument counts and type mismatches
 * that the stanc compiler would also reject.
 *
 * User-defined functions (IDENTIFIER token in call position) are not checked
 * here — they are not in the database.
 */
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

    /** Collect all function names declared/defined in the functions block. */
    private Set<String> collectUserDefinedFunctions(ASTNode root) {
        Set<String> names = new HashSet<>();
        collectUserFunctionsRec(root, names);
        return names;
    }

    private void collectUserFunctionsRec(ASTNode node, Set<String> names) {
        if (node.getElementType() == StanElementTypes.FUN_DEF) {
            // The function name is the first IDENTIFIER or BUILTIN_FUNCTION child
            // before the LPAREN. The return type only contains type keywords/nodes.
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                if (c.getElementType() == StanTokenTypes.LPAREN) break;
                if (c.getElementType() == StanTokenTypes.IDENTIFIER
                        || c.getElementType() == StanTokenTypes.BUILTIN_FUNCTION) {
                    names.add(c.getText());
                    break;
                }
            }
            return; // no nested function definitions in Stan
        }
        for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext())
            collectUserFunctionsRec(c, names);
    }

    private void checkCalls(ASTNode node, Map<String, String> typeMap,
                            Set<String> userFunctions, ProblemsHolder holder) {
        IElementType t = node.getElementType();
        if (t == StanElementTypes.FUN_CALL_EXPR || t == StanElementTypes.COND_DIST_EXPR) {
            checkSingleCall(node, typeMap, userFunctions, holder);
            // still recurse — calls can be nested inside argument expressions
        }
        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            checkCalls(child, typeMap, userFunctions, holder);
        }
    }

    private void checkSingleCall(ASTNode callNode, Map<String, String> typeMap,
                                 Set<String> userFunctions, ProblemsHolder holder) {
        ASTNode nameToken = callNode.getFirstChildNode();
        if (nameToken == null) return;
        IElementType nameType = nameToken.getElementType();
        String fnName = nameToken.getText();

        // Rule: conditioning-suffix functions (_lpdf, _lpmf, _cdf, _lcdf, _lccdf, …) called
        // with 2+ arguments WITHOUT '|' notation → stanc3 "conditioning_required" error.
        // Single-arg calls are excluded: stanc3 auto-promotes f(y) to f(y | ) silently.
        if (callNode.getElementType() == StanElementTypes.FUN_CALL_EXPR
                && (nameType == StanTokenTypes.BUILTIN_FUNCTION || nameType == StanTokenTypes.IDENTIFIER)
                && StanParser.hasConditioningSuffix(fnName)) {
            List<ASTNode> condArgExprs = collectArgExprs(callNode);
            if (condArgExprs.size() >= 2) {
                holder.registerProblem(nameToken.getPsi(),
                        "'" + fnName + "' requires conditional notation: " + fnName + "(sample | params)",
                        ProblemHighlightType.GENERIC_ERROR);
                return;
            }
        }

        // Undefined function: IDENTIFIER head that is not declared in the functions block.
        if (nameType == StanTokenTypes.IDENTIFIER && !userFunctions.contains(fnName)) {
            holder.registerProblem(nameToken.getPsi(),
                    "Undefined function '" + fnName + "'",
                    ProblemHighlightType.GENERIC_ERROR);
            return;
        }

        // Only check arity/types for known built-ins; skip user-defined functions.
        if (nameType != StanTokenTypes.BUILTIN_FUNCTION) return;

        StanSignatureDatabase db = StanSignatureDatabase.getInstance();

        // Distribution calls in COND_DIST_EXPR: f(y | mu, sigma)
        // The first ARG_LIST is what comes before |, the second is after.
        // In stanc these are combined: f_lpdf(y | mu, sigma) = f_lpdf(args_before | args_after).
        // We just treat all args as a flat list for arity checking.
        List<ASTNode> argExprs = collectArgExprs(callNode);
        int actualArity = argExprs.size();

        List<StanSignatureDatabase.Signature> sigs = db.getSignatures(fnName);
        if (sigs.isEmpty()) return; // not in DB (shouldn't happen for BUILTIN_FUNCTION)

        // ── Arity check ───────────────────────────────────────────────────────
        boolean arityOk = sigs.stream().anyMatch(s -> s.args.size() == actualArity);
        if (!arityOk) {
            IntSummaryStatistics stats = sigs.stream()
                    .mapToInt(s -> s.args.size())
                    .summaryStatistics();
            String expected = stats.getMin() == stats.getMax()
                    ? String.valueOf(stats.getMin())
                    : stats.getMin() + "–" + stats.getMax();
            holder.registerProblem(nameToken.getPsi(),
                    "'" + fnName + "' expects " + expected + " argument(s), got " + actualArity,
                    ProblemHighlightType.GENERIC_ERROR);
            return; // arity mismatch makes type check meaningless
        }

        // ── Type check ────────────────────────────────────────────────────────
        // Infer the type of each argument expression.
        List<String> actualTypes = new ArrayList<>(actualArity);
        for (ASTNode arg : argExprs)
            actualTypes.add(StanSignatureDatabase.inferExprType(arg, typeMap));

        // If all actual types are unknown, skip (no false positives).
        boolean anyKnown = actualTypes.stream().anyMatch(Objects::nonNull);
        if (!anyKnown) return;

        // Try to find a matching signature.
        boolean matched = sigs.stream()
                .filter(s -> s.args.size() == actualArity)
                .anyMatch(s -> sigMatches(s, actualTypes));
        if (matched) return;

        // No arity-matching signature gave a full type match.
        // For each argument position, check whether the actual type is compatible with
        // ANY overload at that position (across all arities). A type that appears in a
        // different-arity overload is still valid for this function — only flag positions
        // where the actual type is incompatible with every overload. This avoids false
        // positives when, e.g., to_matrix(a[int]) is called with 1 arg even though
        // a[int] only appears in the 3-arg overload.
        for (int i = 0; i < actualArity; i++) {
            final String actual = actualTypes.get(i);
            if (actual == null) continue;
            final int pos = i;
            boolean compatibleWithAny = sigs.stream()
                    .filter(s -> s.args.size() > pos)
                    .anyMatch(s -> StanSignatureDatabase.isCompatible(s.args.get(pos), actual));
            if (!compatibleWithAny) {
                // Collect distinct expected types from arity-matching overloads for the message.
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

    /** True if ALL argument types (that are known) are compatible with this signature. */
    private boolean sigMatches(StanSignatureDatabase.Signature sig, List<String> actualTypes) {
        for (int i = 0; i < sig.args.size(); i++) {
            String actual = actualTypes.get(i);
            if (actual == null) continue; // unknown — give benefit of the doubt
            if (!StanSignatureDatabase.isCompatible(sig.args.get(i), actual)) return false;
        }
        return true;
    }

    /**
     * Collect all expression arguments from a FUN_CALL_EXPR or COND_DIST_EXPR.
     * For COND_DIST_EXPR the args before and after | are both included.
     */
    private List<ASTNode> collectArgExprs(ASTNode callNode) {
        List<ASTNode> result = new ArrayList<>();
        for (ASTNode child = callNode.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            if (child.getElementType() == StanElementTypes.ARG_LIST) {
                for (ASTNode c = child.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                    if (c.getFirstChildNode() != null) // composite node = expression
                        result.add(c);
                }
            }
        }
        return result;
    }
}
