package org.intellij.stan;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class StanConsecutiveUnaryInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitFile(@NotNull PsiFile file) {
                checkNode(file.getNode(), holder);
            }
        };
    }

    private void checkNode(ASTNode node, ProblemsHolder holder) {
        if (node.getElementType() == StanElementTypes.PREFIX_OP_EXPR) {
            ASTNode opToken = node.getFirstChildNode();
            ASTNode operand = opToken != null ? opToken.getTreeNext() : null;
            if (operand != null && operand.getElementType() == StanElementTypes.PREFIX_OP_EXPR) {
                holder.registerProblem(node.getPsi(),
                        "Consecutive unary operators '" + opToken.getText() + operand.getFirstChildNode().getText()
                                + "' — simplify or check for a typo",
                        ProblemHighlightType.WARNING);
                // recurse into the inner PREFIX_OP_EXPR's operand, not the inner node itself,
                // so we don't double-report a chain like --+x
                ASTNode innerOperand = operand.getFirstChildNode();
                if (innerOperand != null) innerOperand = innerOperand.getTreeNext();
                if (innerOperand != null) checkNode(innerOperand, holder);
                return;
            }
        }
        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            checkNode(child, holder);
        }
    }
}
