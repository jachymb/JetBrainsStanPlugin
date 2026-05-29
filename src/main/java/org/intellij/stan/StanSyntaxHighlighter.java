package org.intellij.stan;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.intellij.stan.lexer.StanLexer;
import org.intellij.stan.psi.StanTypes;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class StanSyntaxHighlighter extends SyntaxHighlighterBase {

    public static final TextAttributesKey KEYWORD =
        createTextAttributesKey("STAN_KEYWORD",        DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey BLOCK_KEYWORD =
        createTextAttributesKey("STAN_BLOCK_KEYWORD",  DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey TYPE =
        createTextAttributesKey("STAN_TYPE",           DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey BUILTIN_FUNCTION =
        createTextAttributesKey("STAN_BUILTIN_FUNCTION", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL);
    public static final TextAttributesKey NUMBER =
        createTextAttributesKey("STAN_NUMBER",         DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey STRING =
        createTextAttributesKey("STAN_STRING",         DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey LINE_COMMENT =
        createTextAttributesKey("STAN_LINE_COMMENT",   DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey BLOCK_COMMENT =
        createTextAttributesKey("STAN_BLOCK_COMMENT",  DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey OPERATOR =
        createTextAttributesKey("STAN_OPERATOR",       DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey BRACES =
        createTextAttributesKey("STAN_BRACES",         DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey BRACKETS =
        createTextAttributesKey("STAN_BRACKETS",       DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey PARENTHESES =
        createTextAttributesKey("STAN_PARENTHESES",    DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey SEMICOLON =
        createTextAttributesKey("STAN_SEMICOLON",      DefaultLanguageHighlighterColors.SEMICOLON);
    public static final TextAttributesKey COMMA =
        createTextAttributesKey("STAN_COMMA",          DefaultLanguageHighlighterColors.COMMA);
    public static final TextAttributesKey RESERVED =
        createTextAttributesKey("STAN_RESERVED",       CodeInsightColors.WARNINGS_ATTRIBUTES);
    public static final TextAttributesKey BAD_CHARACTER =
        createTextAttributesKey("STAN_BAD_CHARACTER",  HighlighterColors.BAD_CHARACTER);

    // Control-flow keywords
    private static final TokenSet KEYWORD_TOKENS = TokenSet.create(
        StanTypes.RETURN, StanTypes.IF, StanTypes.ELSE, StanTypes.WHILE,
        StanTypes.FOR, StanTypes.IN, StanTypes.BREAK, StanTypes.CONTINUE,
        StanTypes.PROFILE, StanTypes.PRINT, StanTypes.REJECT, StanTypes.FATAL_ERROR,
        StanTypes.TARGET, StanTypes.JACOBIAN
    );

    // Block-name keywords (multi-word tokens from the flex lexer)
    private static final TokenSet BLOCK_KEYWORD_TOKENS = TokenSet.create(
        StanTypes.FUNCTIONBLOCK, StanTypes.DATABLOCK, StanTypes.PARAMETERSBLOCK,
        StanTypes.MODELBLOCK, StanTypes.TRANSFORMEDDATABLOCK,
        StanTypes.TRANSFORMEDPARAMETERSBLOCK, StanTypes.GENERATEDQUANTITIESBLOCK
    );

    // Primitive and constrained type keywords
    private static final TokenSet TYPE_TOKENS = TokenSet.create(
        StanTypes.VOID, StanTypes.INT, StanTypes.REAL, StanTypes.COMPLEX,
        StanTypes.VECTOR, StanTypes.ROWVECTOR, StanTypes.MATRIX,
        StanTypes.COMPLEXVECTOR, StanTypes.COMPLEXROWVECTOR, StanTypes.COMPLEXMATRIX,
        StanTypes.ARRAY, StanTypes.TUPLE,
        StanTypes.ORDERED, StanTypes.POSITIVEORDERED, StanTypes.SIMPLEX,
        StanTypes.UNITVECTOR, StanTypes.SUMTOZEROVEC, StanTypes.SUMTOZEROMAT,
        StanTypes.CHOLESKYFACTORCORR, StanTypes.CHOLESKYFACTORCOV,
        StanTypes.CORRMATRIX, StanTypes.COVMATRIX,
        StanTypes.STOCHASTICCOLUMNMATRIX, StanTypes.STOCHASTICROWMATRIX,
        // constraint sub-keywords also colour as type
        StanTypes.LOWER, StanTypes.UPPER, StanTypes.OFFSET, StanTypes.MULTIPLIER
    );

    // All operator tokens
    private static final TokenSet OPERATOR_TOKENS = TokenSet.create(
        StanTypes.PLUS, StanTypes.MINUS, StanTypes.TIMES, StanTypes.DIVIDE,
        StanTypes.MODULO, StanTypes.IDIVIDE, StanTypes.LDIVIDE,
        StanTypes.ELTTIMES, StanTypes.ELTDIVIDE, StanTypes.HAT, StanTypes.ELTPOW,
        StanTypes.OR, StanTypes.AND, StanTypes.EQUALS, StanTypes.NEQUALS,
        StanTypes.LABRACK, StanTypes.LEQ, StanTypes.RABRACK, StanTypes.GEQ,
        StanTypes.BANG, StanTypes.TRANSPOSE,
        StanTypes.ASSIGN, StanTypes.PLUSASSIGN, StanTypes.MINUSASSIGN,
        StanTypes.TIMESASSIGN, StanTypes.DIVIDEASSIGN,
        StanTypes.ELTTIMESASSIGN, StanTypes.ELTDIVIDEASSIGN,
        StanTypes.TILDE, StanTypes.BAR, StanTypes.COLON, StanTypes.QMARK,
        StanTypes.DOTNUMERAL  // tuple projection operator
    );

    private static final TextAttributesKey[] KEYWORD_KEYS       = {KEYWORD};
    private static final TextAttributesKey[] BLOCK_KEYWORD_KEYS = {BLOCK_KEYWORD};
    private static final TextAttributesKey[] TYPE_KEYS          = {TYPE};
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

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new StanLexer();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType t) {
        if (KEYWORD_TOKENS.contains(t))       return KEYWORD_KEYS;
        if (BLOCK_KEYWORD_TOKENS.contains(t)) return BLOCK_KEYWORD_KEYS;
        if (TYPE_TOKENS.contains(t))          return TYPE_KEYS;

        if (t == StanTypes.INTNUMERAL || t == StanTypes.REALNUMERAL || t == StanTypes.IMAGNUMERAL)
            return NUMBER_KEYS;
        if (t == StanTypes.STRINGLITERAL)     return STRING_KEYS;
        if (t == StanTypes.LINE_COMMENT)      return LINE_COMMENT_KEYS;
        if (t == StanTypes.BLOCK_COMMENT)     return BLOCK_COMMENT_KEYS;

        if (OPERATOR_TOKENS.contains(t))      return OPERATOR_KEYS;

        if (t == StanTypes.LBRACE || t == StanTypes.RBRACE)   return BRACES_KEYS;
        if (t == StanTypes.LBRACK || t == StanTypes.RBRACK)   return BRACKETS_KEYS;
        if (t == StanTypes.LPAREN || t == StanTypes.RPAREN)   return PARENS_KEYS;
        if (t == StanTypes.SEMICOLON)                          return SEMICOLON_KEYS;
        if (t == StanTypes.COMMA)                              return COMMA_KEYS;

        if (t == TokenType.BAD_CHARACTER)     return BAD_CHAR_KEYS;

        return EMPTY_KEYS;
    }
}
