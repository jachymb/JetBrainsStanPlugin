package org.intellij.stan;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class StanBracketHandler extends TypedHandlerDelegate {

    @Override
    public @NotNull Result charTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        if (!(file.getLanguage() instanceof StanLanguage)) return Result.CONTINUE;

        if (c == '{') {
            handleBrace(editor);
            return Result.STOP;
        } else if (c == '[') {
            handlePair(editor, ']');
            return Result.STOP;
        } else if (c == '(') {
            handlePair(editor, ')');
            return Result.STOP;
        } else if (c == '<' && inConstraintBlock(file, editor.getCaretModel().getOffset())) {
            handlePair(editor, '>');
            return Result.STOP;
        }

        return Result.CONTINUE;
    }

    private static boolean inConstraintBlock(PsiFile file, int caretOffset) {
        // Look up the element just before '<' (caretOffset - 1 is '<', caretOffset - 2 is before it)
        int lookupOffset = Math.max(0, caretOffset - 2);
        PsiElement element = file.findElementAt(lookupOffset);
        while (element != null) {
            IElementType type = element.getNode().getElementType();
            if (type == StanElementTypes.DATA_BLOCK || type == StanElementTypes.PARAMETERS_BLOCK) {
                return true;
            }
            element = element.getParent();
        }
        return false;
    }

    // If the IDE already inserted '}', expand to newline+indent between the pair.
    // Otherwise insert '}\n<indent>' and place the cursor on the inner line.
    private void handleBrace(Editor editor) {
        Document doc = editor.getDocument();
        int caretOffset = editor.getCaretModel().getOffset();

        int lineStart = doc.getLineStartOffset(doc.getLineNumber(caretOffset - 1));
        String lineText = doc.getText().substring(lineStart, caretOffset - 1);
        String indent = leadingWhitespace(lineText);
        String innerIndent = indent + "    ";

        boolean alreadyClosed = nextChar(doc, caretOffset) == '}';

        if (alreadyClosed) {
            // IDE already put '}' there; insert the two newlines between the pair
            String insertion = "\n" + innerIndent + "\n" + indent;
            EditorModificationUtil.insertStringAtCaret(editor, insertion, false, false);
        } else {
            String insertion = "\n" + innerIndent + "\n" + indent + "}";
            EditorModificationUtil.insertStringAtCaret(editor, insertion, false, false);
        }

        editor.getCaretModel().moveToOffset(caretOffset + 1 + innerIndent.length());
    }

    // If the IDE already inserted the closing char, leave the cursor where it is.
    // Otherwise insert the closing char and keep the cursor between the pair.
    private void handlePair(Editor editor, char closing) {
        Document doc = editor.getDocument();
        int caretOffset = editor.getCaretModel().getOffset();

        if (nextChar(doc, caretOffset) != closing) {
            EditorModificationUtil.insertStringAtCaret(editor, String.valueOf(closing), false, false);
        }
        // caret stays between the pair in both cases
    }

    private static char nextChar(Document doc, int offset) {
        if (offset >= doc.getTextLength()) return 0;
        return doc.getCharsSequence().charAt(offset);
    }

    private static String leadingWhitespace(String line) {
        int i = 0;
        while (i < line.length() && (line.charAt(i) == ' ' || line.charAt(i) == '\t')) i++;
        return line.substring(0, i);
    }
}
