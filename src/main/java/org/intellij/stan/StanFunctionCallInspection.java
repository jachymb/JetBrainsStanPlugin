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

import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
                Set<String> userFunctions = StanSignatureDatabase.collectUserDefinedFunctionNames(root);
                checkCalls(root, typeMap, userFunctions, holder);
            }
        };
    }

    private void checkCalls(ASTNode node, Map<String, String> typeMap,
                            Set<String> userFunctions, ProblemsHolder holder) {
        IElementType t = node.getElementType();

        if (t == StanElementTypes.FUN_DEF) {
            // Function bodies are isolated: they can only access their own parameters,
            // not variables from data/parameters/model blocks.
            Map<String, String> localMap = StanSignatureDatabase.buildFunctionParamMap(node);
            for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext())
                checkCalls(child, localMap, userFunctions, holder);
            return;
        }

        // An IDENTIFIER inside VARIABLE_EXPR whose text is a known built-in (and not shadowed)
        // means the function name was written without a call — functions are not values in Stan.
        if (t == StanElementTypes.VARIABLE_EXPR) {
            ASTNode first = node.getFirstChildNode();
            if (first != null && first.getElementType() == StanTokenTypes.IDENTIFIER) {
                String name = first.getText();
                StanSignatureDatabase db = StanSignatureDatabase.getInstance();
                if (db.hasFunction(name) && !typeMap.containsKey(name) && !userFunctions.contains(name)) {
                    holder.registerProblem(first.getPsi(),
                            "'" + name + "' is a function and cannot be used as a value; did you forget '(...)'?",
                            ProblemHighlightType.GENERIC_ERROR);
                    return;
                }
            }
        }

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
        if (nameType != StanTokenTypes.IDENTIFIER && nameType != StanTokenTypes.BUILTIN_FUNCTION) return;
        String fnName = nameToken.getText();

        // Conditioning-suffix functions called with 2+ args without '|' notation.
        // Skip if the name is shadowed by a local (typeMap) so we don't false-positive.
        if (callNode.getElementType() == StanElementTypes.FUN_CALL_EXPR
                && StanParser.hasConditioningSuffix(fnName)
                && !typeMap.containsKey(fnName)) {
            List<ASTNode> condArgExprs = StanSignatureDatabase.collectArgExprs(callNode);
            if (condArgExprs.size() >= 2) {
                holder.registerProblem(nameToken.getPsi(),
                        "'" + fnName + "' requires conditional notation: " + fnName + "(sample | params)",
                        ProblemHighlightType.GENERIC_ERROR);
                return;
            }
        }

        // IDENTIFIER head: user-defined function or undefined.
        if (nameType == StanTokenTypes.IDENTIFIER) {
            if (typeMap.containsKey(fnName)) {
                holder.registerProblem(nameToken.getPsi(),
                        "A returning function was expected but a non-function value '" + fnName + "' was supplied.",
                        ProblemHighlightType.GENERIC_ERROR);
                return;
            }
            if (!userFunctions.contains(fnName))
                holder.registerProblem(nameToken.getPsi(),
                        "Undefined function '" + fnName + "'",
                        ProblemHighlightType.GENERIC_ERROR);
            return; // no arity/type check for user-defined functions
        }

        // BUILTIN_FUNCTION head: the lexer guarantees the name is followed by '('.
        // If shadowed by a local variable/parameter, it cannot be called as a function.
        if (typeMap.containsKey(fnName)) {
            holder.registerProblem(nameToken.getPsi(),
                    "A returning function was expected but a non-function value '" + fnName + "' was supplied.",
                    ProblemHighlightType.GENERIC_ERROR);
            return;
        }

        StanSignatureDatabase db = StanSignatureDatabase.getInstance();
        // Distribution calls in COND_DIST_EXPR: f(y | mu, sigma)
        // The first ARG_LIST is what comes before |, the second is after.
        // In stanc these are combined: f_lpdf(y | mu, sigma) = f_lpdf(args_before | args_after).
        // We just treat all args as a flat list for arity checking.
        List<ASTNode> argExprs = StanSignatureDatabase.collectArgExprs(callNode);
        int actualArity = argExprs.size();

        List<StanSignatureDatabase.Signature> sigs = db.getSignatures(fnName);
        if (sigs.isEmpty()) {
            // Bare distribution names (e.g. "poisson", "normal") are lexed as BUILTIN_FUNCTION
            // so they highlight correctly in tilde statements, but they are not callable functions.
            if (StanSignatureDatabase.DISTRIBUTION_BASE_NAMES.contains(fnName)) {
                holder.registerProblem(nameToken.getPsi(),
                        "'" + fnName + "' is a distribution name, not a callable function; " +
                        "use '" + fnName + "_lpdf', '" + fnName + "_lpmf', or a tilde statement",
                        ProblemHighlightType.GENERIC_ERROR);
            }
            return;
        }

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
}
