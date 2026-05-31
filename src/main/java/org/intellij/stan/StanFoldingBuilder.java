package org.intellij.stan;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StanFoldingBuilder extends FoldingBuilderEx {

    // Only these node types produce fold regions — they are real scoping blocks.
    // Expression nodes (ARRAY_EXPR, etc.) are intentionally excluded to avoid
    // registering hundreds of tiny fold regions that would slow down the EDT.
    private static final TokenSet FOLDABLE_TYPES = TokenSet.orSet(
        StanElementTypes.BLOCK_TYPES,
        TokenSet.create(
            StanElementTypes.FUN_DEF,
            StanElementTypes.BLOCK_STMT,
            StanElementTypes.PROFILE_STMT
        )
    );

    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
        if (quick) return FoldingDescriptor.EMPTY_ARRAY;
        List<FoldingDescriptor> descriptors = new ArrayList<>();
        collectFolds(root.getNode(), descriptors);
        return descriptors.toArray(FoldingDescriptor.EMPTY_ARRAY);
    }

    private void collectFolds(ASTNode node, List<FoldingDescriptor> descriptors) {
        IElementType type = node.getElementType();
        if (FOLDABLE_TYPES.contains(type)) {
            ASTNode lbrace = node.findChildByType(StanTokenTypes.LBRACE);
            ASTNode rbrace = node.findChildByType(StanTokenTypes.RBRACE);
            if (lbrace != null && rbrace != null) {
                TextRange range = new TextRange(lbrace.getStartOffset(), rbrace.getStartOffset() + rbrace.getTextLength());
                if (range.getLength() > 2) {
                    descriptors.add(new FoldingDescriptor(node, range));
                }
            }
        }
        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            collectFolds(child, descriptors);
        }
    }

    @Override
    public @Nullable String getPlaceholderText(@NotNull ASTNode node) {
        return "{...}";
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }
}
