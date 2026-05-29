package org.intellij.stan;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * _rng functions may only be called from:
 *   - the transformed data block
 *   - the generated quantities block
 *   - a user-defined function whose name ends in _rng
 *
 * Mirrors stanc3's random-number-generator context check.
 */
public class StanRngCallContextInspection extends LocalInspectionTool {

    private static final Set<IElementType> ALLOWED_BLOCKS = Set.of(
            StanElementTypes.TRANSFORMED_DATA_BLOCK,
            StanElementTypes.GENERATED_QUANTITIES_BLOCK
    );

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitFile(@NotNull PsiFile file) {
                check(file.getNode(), null, false, holder);
            }
        };
    }

    /**
     * @param enclosingBlock  element type of the nearest ancestor top-level block, or null
     * @param inRngFunction   true when directly inside a FUN_DEF whose name ends with _rng
     */
    private void check(ASTNode node, IElementType enclosingBlock, boolean inRngFunction,
                       ProblemsHolder holder) {
        IElementType t = node.getElementType();

        // On entering any top-level block, update enclosingBlock and reset inRngFunction.
        if (t == StanElementTypes.FUNCTIONS_BLOCK
                || t == StanElementTypes.DATA_BLOCK
                || t == StanElementTypes.TRANSFORMED_DATA_BLOCK
                || t == StanElementTypes.PARAMETERS_BLOCK
                || t == StanElementTypes.TRANSFORMED_PARAMETERS_BLOCK
                || t == StanElementTypes.MODEL_BLOCK
                || t == StanElementTypes.GENERATED_QUANTITIES_BLOCK) {
            recurse(node, t, false, holder);
            return;
        }

        // On entering a function definition, determine whether it is an _rng function.
        if (t == StanElementTypes.FUN_DEF) {
            boolean isRng = false;
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                if (c.getElementType() == StanTokenTypes.LPAREN) break;
                IElementType ct = c.getElementType();
                if (ct == StanTokenTypes.IDENTIFIER || ct == StanTokenTypes.BUILTIN_FUNCTION) {
                    isRng = c.getText().endsWith("_rng");
                    break;
                }
            }
            recurse(node, enclosingBlock, isRng, holder);
            return;
        }

        // Check function calls whose name ends in _rng.
        if (t == StanElementTypes.FUN_CALL_EXPR || t == StanElementTypes.COND_DIST_EXPR) {
            ASTNode nameToken = node.getFirstChildNode();
            if (nameToken != null) {
                IElementType nt = nameToken.getElementType();
                if ((nt == StanTokenTypes.BUILTIN_FUNCTION || nt == StanTokenTypes.IDENTIFIER)
                        && nameToken.getText().endsWith("_rng")) {
                    boolean allowed = inRngFunction || ALLOWED_BLOCKS.contains(enclosingBlock);
                    if (!allowed) {
                        holder.registerProblem(nameToken.getPsi(),
                                "'" + nameToken.getText() + "' may only be called from "
                                        + "transformed data, generated quantities, or an _rng function"
                                        + " (called from " + blockDisplayName(enclosingBlock) + ")",
                                ProblemHighlightType.GENERIC_ERROR);
                    }
                }
            }
            // Always recurse: args may themselves contain further calls.
        }

        recurse(node, enclosingBlock, inRngFunction, holder);
    }

    private void recurse(ASTNode node, IElementType enclosingBlock, boolean inRngFunction,
                         ProblemsHolder holder) {
        for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext())
            check(c, enclosingBlock, inRngFunction, holder);
    }

    private static String blockDisplayName(IElementType block) {
        if (block == null) return "unknown context";
        if (block == StanElementTypes.DATA_BLOCK) return "data block";
        if (block == StanElementTypes.PARAMETERS_BLOCK) return "parameters block";
        if (block == StanElementTypes.TRANSFORMED_PARAMETERS_BLOCK) return "transformed parameters block";
        if (block == StanElementTypes.MODEL_BLOCK) return "model block";
        if (block == StanElementTypes.FUNCTIONS_BLOCK) return "functions block";
        return "this block";
    }
}
