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
        // unary_expr ::= BANG unary_expr | MINUS unary_expr | PLUS unary_expr | pow_expr
        // A "real" unary node is one whose first child is an operator token.
        if (node.getElementType() == StanTypes.UNARY_EXPR && isUnaryOp(node)) {
            ASTNode opToken = node.getFirstChildNode();
            ASTNode operand = opToken != null ? opToken.getTreeNext() : null;
            if (operand != null && operand.getElementType() == StanTypes.UNARY_EXPR
                    && isUnaryOp(operand)) {
                ASTNode innerOp = operand.getFirstChildNode();
                holder.registerProblem(node.getPsi(),
                        "Consecutive unary operators '"
                                + (opToken != null ? opToken.getText() : "")
                                + (innerOp != null ? innerOp.getText() : "")
                                + "' — simplify or check for a typo",
                        ProblemHighlightType.WARNING);
                // Recurse into the inner operand only, avoiding double-reporting.
                ASTNode innerOperand = innerOp != null ? innerOp.getTreeNext() : null;
                if (innerOperand != null) checkNode(innerOperand, holder);
                return;
            }
        }
        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext())
            checkNode(child, holder);
    }

    private static boolean isUnaryOp(ASTNode unaryExpr) {
        ASTNode first = unaryExpr.getFirstChildNode();
        if (first == null) return false;
        IElementType ft = first.getElementType();
        return ft == StanTypes.BANG || ft == StanTypes.MINUS || ft == StanTypes.PLUS;
    }
}
