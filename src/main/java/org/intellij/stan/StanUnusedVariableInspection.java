package org.intellij.stan;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class StanUnusedVariableInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitFile(@NotNull PsiFile file) {
                analyzeFile(file, holder);
            }
        };
    }

    private void analyzeFile(PsiFile file, ProblemsHolder holder) {
        Set<String> usedNames = new HashSet<>();
        collectUsedNames(file.getNode(), usedNames);
        checkDeclarations(file.getNode(), usedNames, holder);
    }

    /** Collect every identifier that appears inside a VARIABLE_EXPR node (a read or write reference). */
    private void collectUsedNames(ASTNode node, Set<String> usedNames) {
        if (node.getElementType() == StanElementTypes.VARIABLE_EXPR) {
            ASTNode child = node.getFirstChildNode();
            if (child != null && child.getElementType() == StanTokenTypes.IDENTIFIER) {
                usedNames.add(child.getText());
            }
            // don't recurse — VARIABLE_EXPR is a leaf-like node
            return;
        }
        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            collectUsedNames(child, usedNames);
        }
    }

    /** Walk DECLARED_VAR nodes and flag any whose name never appears in usedNames. */
    private void checkDeclarations(ASTNode node, Set<String> usedNames, ProblemsHolder holder) {
        if (node.getElementType() == StanElementTypes.DECLARED_VAR) {
            ASTNode nameNode = node.getFirstChildNode();
            if (nameNode != null && nameNode.getElementType() == StanTokenTypes.IDENTIFIER) {
                String name = nameNode.getText();
                if (!usedNames.contains(name)) {
                    PsiElement psiName = nameNode.getPsi();
                    holder.registerProblem(psiName,
                            "Variable '" + name + "' is never used",
                            ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                }
            }
            // still recurse — DECLARED_VAR can have an initializer with nested decls (tuples)
        }
        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            checkDeclarations(child, usedNames, holder);
        }
    }
}
