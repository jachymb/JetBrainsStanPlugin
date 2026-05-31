package org.intellij.stan;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * Warns when a declared name (function parameter or variable) shadows a built-in function.
 * The warning appears at the declaration site only.
 *
 * The lexer already emits IDENTIFIER (not BUILTIN_FUNCTION) for any built-in name that is not
 * immediately followed by '(', which covers all declaration positions. This inspection therefore
 * simply checks whether any IDENTIFIER token whose text matches a known built-in appears in those
 * positions.
 */
public class StanShadowingInspection extends LocalInspectionTool {

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
        IElementType t = node.getElementType();

        if (t == StanElementTypes.ARG_DECL) {
            checkArgDecl(node, holder);
        } else if (t == StanElementTypes.DECLARED_VAR) {
            checkDeclaredVar(node, holder);
        }

        for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext())
            checkNode(c, holder);
    }

    private void checkArgDecl(ASTNode argDecl, ProblemsHolder holder) {
        ASTNode nameToken = StanSignatureDatabase.argDeclNameToken(argDecl);
        if (nameToken != null && nameToken.getElementType() == StanTokenTypes.IDENTIFIER)
            warnIfShadowsBuiltin(nameToken, holder);
    }

    /** The variable name in DECLARED_VAR is its first child token. */
    private void checkDeclaredVar(ASTNode declaredVar, ProblemsHolder holder) {
        ASTNode name = declaredVar.getFirstChildNode();
        if (name != null && name.getElementType() == StanTokenTypes.IDENTIFIER)
            warnIfShadowsBuiltin(name, holder);
    }

    private void warnIfShadowsBuiltin(ASTNode identNode, ProblemsHolder holder) {
        String name = identNode.getText();
        if (StanSignatureDatabase.getInstance().hasFunction(name))
            holder.registerProblem(identNode.getPsi(),
                    "'" + name + "' shadows a built-in function",
                    ProblemHighlightType.WARNING);
    }
}
