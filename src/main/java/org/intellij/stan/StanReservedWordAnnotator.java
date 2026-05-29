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

/**
 * Flags two categories of reserved-word misuse:
 * 1. Stan keywords used as variable or function names (grammar accepts them
 *    via the {@code reserved_word} alternative of {@code decl_identifier}; we
 *    report the error here with better IDE context).
 * 2. C++ reserved words used as identifiers (forbidden by stanc3).
 */
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
        IElementType t = node.getElementType();

        // decl_identifier ::= ident | reserved_word
        // When the reserved_word alternative is chosen, the first child is a keyword token.
        if (t == StanTypes.DECL_IDENTIFIER || t == StanTypes.DECL_IDENTIFIER_AFTER_COMMA) {
            ASTNode child = node.getFirstChildNode();
            // ident → first child is IDENTIFIER or TRUNCATE token; reserved_word → keyword token
            if (child != null && child.getElementType() == StanTypes.RESERVED_WORD) {
                // The reserved_word node wraps the actual keyword token.
                ASTNode kw = child.getFirstChildNode();
                if (kw != null) {
                    PsiElement psi = kw.getPsi();
                    holder.registerProblem(psi,
                            "'" + kw.getText() + "' is a Stan keyword and cannot be used as an identifier",
                            ProblemHighlightType.GENERIC_ERROR);
                }
            }
            return;
        }

        // C++ reserved words: they arrive as plain IDENTIFIER tokens.
        if (t == StanTypes.IDENTIFIER) {
            String name = node.getText();
            if (StanSyntaxUtil.CPP_RESERVED.contains(name)) {
                PsiElement psi = node.getPsi();
                holder.registerProblem(psi,
                        "'" + name + "' is a C++ reserved word and cannot be used as a Stan identifier",
                        ProblemHighlightType.GENERIC_ERROR);
            }
            return;
        }

        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext())
            checkReservedWords(child, holder);
    }
}
