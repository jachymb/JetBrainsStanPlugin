package org.intellij.stan;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

public interface StanTokenTypes {
    IElementType KEYWORD          = new StanTokenType("KEYWORD");
    IElementType BLOCK_KEYWORD    = new StanTokenType("BLOCK_KEYWORD");
    IElementType TYPE             = new StanTokenType("TYPE");
    IElementType BUILTIN_FUNCTION = new StanTokenType("BUILTIN_FUNCTION");
    IElementType IDENTIFIER       = new StanTokenType("IDENTIFIER");
    IElementType NUMBER           = new StanTokenType("NUMBER");
    IElementType STRING           = new StanTokenType("STRING");
    IElementType LINE_COMMENT     = new StanTokenType("LINE_COMMENT");
    IElementType BLOCK_COMMENT    = new StanTokenType("BLOCK_COMMENT");
    IElementType OPERATOR         = new StanTokenType("OPERATOR");
    IElementType LBRACE           = new StanTokenType("LBRACE");
    IElementType RBRACE           = new StanTokenType("RBRACE");
    IElementType LBRACKET         = new StanTokenType("LBRACKET");
    IElementType RBRACKET         = new StanTokenType("RBRACKET");
    IElementType LPAREN           = new StanTokenType("LPAREN");
    IElementType RPAREN           = new StanTokenType("RPAREN");
    IElementType SEMICOLON        = new StanTokenType("SEMICOLON");
    IElementType COMMA            = new StanTokenType("COMMA");
    IElementType WHITE_SPACE      = TokenType.WHITE_SPACE;
    IElementType BAD_CHARACTER    = TokenType.BAD_CHARACTER;
}
