package org.intellij.stan;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class StanReservedWordAnnotator extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitFile(@NotNull PsiFile file) {
                checkReservedWords(file.getNode(), holder);
            }
        };
    }

    private void checkReservedWords(ASTNode node, ProblemsHolder holder) {
        if (node.getElementType() == StanTokenTypes.RESERVED) {
            PsiElement psi = node.getPsi();
            holder.registerProblem(psi,
                    "'" + node.getText() + "' is a C++ reserved word and cannot be used as a Stan identifier",
                    ProblemHighlightType.GENERIC_ERROR);
        }
        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            checkReservedWords(child, holder);
        }
    }
}
