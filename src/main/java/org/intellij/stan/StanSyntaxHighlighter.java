
package org.intellij.stan;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class StanSyntaxHighlighter extends SyntaxHighlighterBase {

    public static final TextAttributesKey KEYWORD =
        createTextAttributesKey("STAN_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey BLOCK_KEYWORD =
        createTextAttributesKey("STAN_BLOCK_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey TYPE =
        createTextAttributesKey("STAN_TYPE", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey BUILTIN_FUNCTION =
        createTextAttributesKey("STAN_BUILTIN_FUNCTION", DefaultLanguageHighlighterColors.FUNCTION_CALL);
    public static final TextAttributesKey NUMBER =
        createTextAttributesKey("STAN_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey STRING =
        createTextAttributesKey("STAN_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey LINE_COMMENT =
        createTextAttributesKey("STAN_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey BLOCK_COMMENT =
        createTextAttributesKey("STAN_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey OPERATOR =
        createTextAttributesKey("STAN_OPERATOR", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey BRACES =
        createTextAttributesKey("STAN_BRACES", DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey BRACKETS =
        createTextAttributesKey("STAN_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey PARENTHESES =
        createTextAttributesKey("STAN_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey SEMICOLON =
        createTextAttributesKey("STAN_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON);
    public static final TextAttributesKey COMMA =
        createTextAttributesKey("STAN_COMMA", DefaultLanguageHighlighterColors.COMMA);
    public static final TextAttributesKey BAD_CHARACTER =
        createTextAttributesKey("STAN_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);

    private static final TextAttributesKey[] KEYWORD_KEYS       = {KEYWORD};
    private static final TextAttributesKey[] BLOCK_KEYWORD_KEYS = {BLOCK_KEYWORD};
    private static final TextAttributesKey[] TYPE_KEYS          = {TYPE};
    private static final TextAttributesKey[] BUILTIN_KEYS       = {BUILTIN_FUNCTION};
    private static final TextAttributesKey[] NUMBER_KEYS        = {NUMBER};
    private static final TextAttributesKey[] STRING_KEYS        = {STRING};
    private static final TextAttributesKey[] LINE_COMMENT_KEYS  = {LINE_COMMENT};
    private static final TextAttributesKey[] BLOCK_COMMENT_KEYS = {BLOCK_COMMENT};
    private static final TextAttributesKey[] OPERATOR_KEYS      = {OPERATOR};
    private static final TextAttributesKey[] BRACES_KEYS        = {BRACES};
    private static final TextAttributesKey[] BRACKETS_KEYS      = {BRACKETS};
    private static final TextAttributesKey[] PARENS_KEYS        = {PARENTHESES};
    private static final TextAttributesKey[] SEMICOLON_KEYS     = {SEMICOLON};
    private static final TextAttributesKey[] COMMA_KEYS         = {COMMA};
    private static final TextAttributesKey[] BAD_CHAR_KEYS      = {BAD_CHARACTER};
    private static final TextAttributesKey[] EMPTY_KEYS         = {};

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new StanLexer();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (tokenType.equals(StanTokenTypes.KEYWORD))          return KEYWORD_KEYS;
        if (tokenType.equals(StanTokenTypes.BLOCK_KEYWORD))    return BLOCK_KEYWORD_KEYS;
        if (tokenType.equals(StanTokenTypes.TYPE))             return TYPE_KEYS;
        if (tokenType.equals(StanTokenTypes.BUILTIN_FUNCTION)) return BUILTIN_KEYS;
        if (tokenType.equals(StanTokenTypes.NUMBER))           return NUMBER_KEYS;
        if (tokenType.equals(StanTokenTypes.STRING))           return STRING_KEYS;
        if (tokenType.equals(StanTokenTypes.LINE_COMMENT))     return LINE_COMMENT_KEYS;
        if (tokenType.equals(StanTokenTypes.BLOCK_COMMENT))    return BLOCK_COMMENT_KEYS;
        if (tokenType.equals(StanTokenTypes.OPERATOR))         return OPERATOR_KEYS;
        if (tokenType.equals(StanTokenTypes.LBRACE)
         || tokenType.equals(StanTokenTypes.RBRACE))           return BRACES_KEYS;
        if (tokenType.equals(StanTokenTypes.LBRACKET)
         || tokenType.equals(StanTokenTypes.RBRACKET))         return BRACKETS_KEYS;
        if (tokenType.equals(StanTokenTypes.LPAREN)
         || tokenType.equals(StanTokenTypes.RPAREN))           return PARENS_KEYS;
        if (tokenType.equals(StanTokenTypes.SEMICOLON))        return SEMICOLON_KEYS;
        if (tokenType.equals(StanTokenTypes.COMMA))            return COMMA_KEYS;
        if (tokenType.equals(TokenType.BAD_CHARACTER))         return BAD_CHAR_KEYS;
        return EMPTY_KEYS;
    }
}
