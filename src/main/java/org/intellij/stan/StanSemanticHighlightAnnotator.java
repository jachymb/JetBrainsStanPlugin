package org.intellij.stan;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Overrides the lexer's BUILTIN_FUNCTION color when the name is shadowed by a
 * local variable, so it renders as a plain identifier rather than a builtin.
 */
public class StanSemanticHighlightAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (!(element instanceof LeafPsiElement leaf)) return;
        if (leaf.getElementType() != StanTokenTypes.BUILTIN_FUNCTION) return;

        if (StanSignatureDatabase.typeMapAt(leaf.getNode()).containsKey(leaf.getText())) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .textAttributes(DefaultLanguageHighlighterColors.IDENTIFIER)
                    .create();
        }
    }
}
