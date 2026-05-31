package org.intellij.stan;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Overrides the lexer's BUILTIN_FUNCTION color when the name is shadowed by a
 * local variable, so it renders as a plain identifier rather than a builtin.
 */
public class StanSemanticHighlightAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (!(element instanceof LeafPsiElement leaf)) return;
        if (leaf.getElementType() != StanTokenTypes.BUILTIN_FUNCTION) return;

        String name = leaf.getText();
        Map<String, String> typeMap = resolveTypeMap(leaf.getNode());

        if (typeMap.containsKey(name)) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .textAttributes(DefaultLanguageHighlighterColors.IDENTIFIER)
                    .create();
        }
    }

    /** Returns the param map of the enclosing FUN_DEF, or the file-level type map. */
    private static Map<String, String> resolveTypeMap(ASTNode node) {
        ASTNode enclosingFunDef = findAncestor(node, StanElementTypes.FUN_DEF);
        if (enclosingFunDef != null)
            return StanSignatureDatabase.buildFunctionParamMap(enclosingFunDef);
        ASTNode root = node;
        while (root.getTreeParent() != null) root = root.getTreeParent();
        return StanSignatureDatabase.buildTypeMap(root);
    }

    private static @Nullable ASTNode findAncestor(ASTNode node, com.intellij.psi.tree.IElementType type) {
        for (ASTNode n = node.getTreeParent(); n != null; n = n.getTreeParent()) {
            if (n.getElementType() == type) return n;
        }
        return null;
    }
}
