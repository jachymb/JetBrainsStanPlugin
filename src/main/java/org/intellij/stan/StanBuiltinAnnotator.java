package org.intellij.stan;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.intellij.stan.psi.StanTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Colors the name of known built-in functions with the BUILTIN_FUNCTION colour.
 * Runs after lexing and PSI construction; checks the signatures database rather
 * than relying on a BUILTIN_FUNCTION lexer token.
 *
 * The JFlex lexer emits IDENTIFIER for all identifiers (builtins included), so
 * annotation is the only place where we can distinguish them without touching
 * the lexer.  We annotate the {@code ident} rule node (wrapping the IDENTIFIER
 * token) that is the first child of a function-call-position parent node.
 */
public class StanBuiltinAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        IElementType type = element.getNode().getElementType();

        // Only visit ident nodes (the wrapper rule for IDENTIFIER | TRUNCATE).
        if (type != StanTypes.IDENT) return;

        String name = element.getText();
        if (!StanSignatureDatabase.getInstance().hasFunction(name)) return;

        // Only colour the function-name ident, not identifiers that appear as arguments.
        PsiElement parent = element.getParent();
        if (!isFunctionCallNode(parent)) return;

        // Verify this ident is the first child of the call node (the function name).
        if (parent.getNode().getFirstChildNode() != element.getNode()) return;

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
              .textAttributes(StanSyntaxHighlighter.BUILTIN_FUNCTION)
              .create();
    }

    private static boolean isFunctionCallNode(PsiElement e) {
        if (e == null) return false;
        IElementType t = e.getNode().getElementType();
        return t == StanTypes.FUN_CALL_EXPR
            || t == StanTypes.COND_DIST_EXPR
            || t == StanTypes.TILDE_STMT
            || t == StanTypes.FUN_CALL_STMT;
    }
}
