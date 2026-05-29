package org.intellij.stan;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.intellij.stan.psi.StanTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class StanUnusedVariableInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitFile(@NotNull PsiFile file) {
                Set<String> usedNames = new HashSet<>();
                collectUsedNames(file.getNode(), usedNames);
                checkDeclarations(file.getNode(), usedNames, holder);
            }
        };
    }

    /** Collect every name from variable_expr nodes (value reads). */
    private void collectUsedNames(ASTNode node, Set<String> usedNames) {
        if (node.getElementType() == StanTypes.VARIABLE_EXPR) {
            // variable_expr ::= ident ::= IDENTIFIER | TRUNCATE
            // .getText() on the variable_expr gives the name text directly.
            usedNames.add(node.getText());
            return;
        }
        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext())
            collectUsedNames(child, usedNames);
    }

    /** Walk declarator nodes and flag those whose name never appears in usedNames. */
    private void checkDeclarations(ASTNode node, Set<String> usedNames, ProblemsHolder holder) {
        IElementType t = node.getElementType();

        if (t == StanTypes.DECLARED_VAR || t == StanTypes.DECLARED_VAR_EXTRA
         || t == StanTypes.TOP_DECLARED_VAR || t == StanTypes.TOP_DECLARED_VAR_EXTRA) {
            ASTNode nameLeaf = findIdentLeaf(node.getFirstChildNode());
            if (nameLeaf != null) {
                String name = nameLeaf.getText();
                if (!usedNames.contains(name)) {
                    PsiElement psiName = nameLeaf.getPsi();
                    holder.registerProblem(psiName,
                            "Variable '" + name + "' is never used",
                            ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                }
            }
        }

        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext())
            checkDeclarations(child, usedNames, holder);
    }

    /** Drill down to the first IDENTIFIER or TRUNCATE leaf inside a wrapper node. */
    private static @Nullable ASTNode findIdentLeaf(@Nullable ASTNode node) {
        while (node != null && node.getFirstChildNode() != null)
            node = node.getFirstChildNode();
        if (node == null) return null;
        IElementType t = node.getElementType();
        return (t == StanTypes.IDENTIFIER || t == StanTypes.TRUNCATE) ? node : null;
    }
}
