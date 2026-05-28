package org.intellij.stan;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

public interface StanTokenTypes {

    // ---- Literals ----
    IElementType INT_LITERAL    = new StanTokenType("INT_LITERAL");
    IElementType REAL_LITERAL   = new StanTokenType("REAL_LITERAL");
    IElementType IMAG_LITERAL   = new StanTokenType("IMAG_LITERAL");
    IElementType STRING_LITERAL = new StanTokenType("STRING_LITERAL");

    // ---- Identifiers ----
    IElementType IDENTIFIER       = new StanTokenType("IDENTIFIER");
    IElementType BUILTIN_FUNCTION = new StanTokenType("BUILTIN_FUNCTION");
    IElementType RESERVED         = new StanTokenType("RESERVED");

    // ---- Control-flow keywords ----
    IElementType IF_KW          = new StanTokenType("if");
    IElementType ELSE_KW        = new StanTokenType("else");
    IElementType FOR_KW         = new StanTokenType("for");
    IElementType WHILE_KW       = new StanTokenType("while");
    IElementType IN_KW          = new StanTokenType("in");
    IElementType BREAK_KW       = new StanTokenType("break");
    IElementType CONTINUE_KW    = new StanTokenType("continue");
    IElementType RETURN_KW      = new StanTokenType("return");
    IElementType PRINT_KW       = new StanTokenType("print");
    IElementType REJECT_KW      = new StanTokenType("reject");
    IElementType FATAL_ERROR_KW = new StanTokenType("fatal_error");
    IElementType PROFILE_KW     = new StanTokenType("profile");
    IElementType TARGET_KW      = new StanTokenType("target");
    IElementType JACOBIAN_KW    = new StanTokenType("jacobian");

    // ---- Block-name keywords ----
    IElementType FUNCTIONS_KW   = new StanTokenType("functions");
    IElementType DATA_KW        = new StanTokenType("data");
    IElementType PARAMETERS_KW  = new StanTokenType("parameters");
    IElementType TRANSFORMED_KW = new StanTokenType("transformed");
    IElementType MODEL_KW       = new StanTokenType("model");
    IElementType GENERATED_KW   = new StanTokenType("generated");
    IElementType QUANTITIES_KW  = new StanTokenType("quantities");

    // ---- Primitive type keywords ----
    IElementType INT_KW         = new StanTokenType("int");
    IElementType REAL_KW        = new StanTokenType("real");
    IElementType COMPLEX_KW     = new StanTokenType("complex");
    IElementType VECTOR_KW      = new StanTokenType("vector");
    IElementType ROW_VECTOR_KW  = new StanTokenType("row_vector");
    IElementType MATRIX_KW      = new StanTokenType("matrix");
    IElementType COMPLEX_VECTOR_KW     = new StanTokenType("complex_vector");
    IElementType COMPLEX_ROW_VECTOR_KW = new StanTokenType("complex_row_vector");
    IElementType COMPLEX_MATRIX_KW     = new StanTokenType("complex_matrix");
    IElementType ARRAY_KW       = new StanTokenType("array");
    IElementType TUPLE_KW       = new StanTokenType("tuple");
    IElementType VOID_KW        = new StanTokenType("void");

    // ---- Constrained type keywords ----
    IElementType ORDERED_KW              = new StanTokenType("ordered");
    IElementType POSITIVE_ORDERED_KW     = new StanTokenType("positive_ordered");
    IElementType SIMPLEX_KW              = new StanTokenType("simplex");
    IElementType UNIT_VECTOR_KW          = new StanTokenType("unit_vector");
    IElementType SUM_TO_ZERO_VECTOR_KW   = new StanTokenType("sum_to_zero_vector");
    IElementType SUM_TO_ZERO_MATRIX_KW   = new StanTokenType("sum_to_zero_matrix");
    IElementType CHOLESKY_FACTOR_CORR_KW = new StanTokenType("cholesky_factor_corr");
    IElementType CHOLESKY_FACTOR_COV_KW  = new StanTokenType("cholesky_factor_cov");
    IElementType CORR_MATRIX_KW          = new StanTokenType("corr_matrix");
    IElementType COV_MATRIX_KW           = new StanTokenType("cov_matrix");
    IElementType COLUMN_STOCHASTIC_MATRIX_KW = new StanTokenType("column_stochastic_matrix");
    IElementType ROW_STOCHASTIC_MATRIX_KW    = new StanTokenType("row_stochastic_matrix");

    // ---- Constraint sub-keywords ----
    IElementType LOWER_KW      = new StanTokenType("lower");
    IElementType UPPER_KW      = new StanTokenType("upper");
    IElementType OFFSET_KW     = new StanTokenType("offset");
    IElementType MULTIPLIER_KW = new StanTokenType("multiplier");

    // ---- Arithmetic operators ----
    IElementType PLUS    = new StanTokenType("PLUS");    // +
    IElementType MINUS   = new StanTokenType("MINUS");   // -
    IElementType TIMES   = new StanTokenType("TIMES");   // *
    IElementType DIVIDE  = new StanTokenType("DIVIDE");  // /
    IElementType MODULO  = new StanTokenType("MODULO");  // %
    IElementType IDIVIDE = new StanTokenType("IDIVIDE"); // %/%
    IElementType LDIVIDE = new StanTokenType("LDIVIDE"); // backslash
    IElementType ELT_TIMES  = new StanTokenType("ELT_TIMES");  // .*
    IElementType ELT_DIVIDE = new StanTokenType("ELT_DIVIDE"); // ./
    IElementType POW     = new StanTokenType("POW");     // ^
    IElementType ELT_POW = new StanTokenType("ELT_POW"); // .^

    // ---- Logical / comparison operators ----
    IElementType OR      = new StanTokenType("OR");      // ||
    IElementType AND     = new StanTokenType("AND");     // &&
    IElementType EQUALS  = new StanTokenType("EQUALS");  // ==
    IElementType NEQUALS = new StanTokenType("NEQUALS"); // !=
    IElementType LESS    = new StanTokenType("LESS");    // <
    IElementType LEQ     = new StanTokenType("LEQ");     // <=
    IElementType GREATER = new StanTokenType("GREATER"); // >
    IElementType GEQ     = new StanTokenType("GEQ");     // >=

    // ---- Unary / postfix operators ----
    IElementType BANG      = new StanTokenType("BANG");      // !
    IElementType TRANSPOSE = new StanTokenType("TRANSPOSE"); // '

    // ---- Assignment operators ----
    IElementType ASSIGN          = new StanTokenType("ASSIGN");           // =
    IElementType PLUS_ASSIGN     = new StanTokenType("PLUS_ASSIGN");      // +=
    IElementType MINUS_ASSIGN    = new StanTokenType("MINUS_ASSIGN");     // -=
    IElementType TIMES_ASSIGN    = new StanTokenType("TIMES_ASSIGN");     // *=
    IElementType DIVIDE_ASSIGN   = new StanTokenType("DIVIDE_ASSIGN");    // /=
    IElementType ELT_TIMES_ASSIGN  = new StanTokenType("ELT_TIMES_ASSIGN");  // .*=
    IElementType ELT_DIVIDE_ASSIGN = new StanTokenType("ELT_DIVIDE_ASSIGN"); // ./=
    IElementType ARROW           = new StanTokenType("ARROW");            // <- (deprecated)

    // ---- Other special operators ----
    IElementType TILDE    = new StanTokenType("TILDE");    // ~
    IElementType BAR      = new StanTokenType("BAR");      // |
    IElementType COLON    = new StanTokenType("COLON");    // :
    IElementType QUESTION = new StanTokenType("QUESTION"); // ?
    IElementType DOT      = new StanTokenType("DOT");      // .  (tuple projection)

    // ---- Punctuation ----
    IElementType LBRACE   = new StanTokenType("LBRACE");   // {
    IElementType RBRACE   = new StanTokenType("RBRACE");   // }
    IElementType LBRACKET = new StanTokenType("LBRACKET"); // [
    IElementType RBRACKET = new StanTokenType("RBRACKET"); // ]
    IElementType LPAREN   = new StanTokenType("LPAREN");   // (
    IElementType RPAREN   = new StanTokenType("RPAREN");   // )
    IElementType SEMICOLON = new StanTokenType("SEMICOLON"); // ;
    IElementType COMMA    = new StanTokenType("COMMA");    // ,

    // ---- Whitespace / comments / errors ----
    IElementType LINE_COMMENT  = new StanTokenType("LINE_COMMENT");
    IElementType BLOCK_COMMENT = new StanTokenType("BLOCK_COMMENT");
    IElementType WHITE_SPACE   = TokenType.WHITE_SPACE;
    IElementType BAD_CHARACTER = TokenType.BAD_CHARACTER;

    // ---- Convenient token sets ----
    TokenSet KEYWORDS = TokenSet.create(
        IF_KW, ELSE_KW, FOR_KW, WHILE_KW, IN_KW, BREAK_KW, CONTINUE_KW,
        RETURN_KW, PRINT_KW, REJECT_KW, FATAL_ERROR_KW, PROFILE_KW,
        TARGET_KW, JACOBIAN_KW
    );
    TokenSet BLOCK_KEYWORDS = TokenSet.create(
        FUNCTIONS_KW, DATA_KW, PARAMETERS_KW, TRANSFORMED_KW,
        MODEL_KW, GENERATED_KW, QUANTITIES_KW
    );
    TokenSet TYPE_KEYWORDS = TokenSet.create(
        INT_KW, REAL_KW, COMPLEX_KW,
        VECTOR_KW, ROW_VECTOR_KW, MATRIX_KW,
        COMPLEX_VECTOR_KW, COMPLEX_ROW_VECTOR_KW, COMPLEX_MATRIX_KW,
        ARRAY_KW, TUPLE_KW, VOID_KW,
        ORDERED_KW, POSITIVE_ORDERED_KW, SIMPLEX_KW, UNIT_VECTOR_KW,
        SUM_TO_ZERO_VECTOR_KW, SUM_TO_ZERO_MATRIX_KW,
        CHOLESKY_FACTOR_CORR_KW, CHOLESKY_FACTOR_COV_KW,
        CORR_MATRIX_KW, COV_MATRIX_KW,
        COLUMN_STOCHASTIC_MATRIX_KW, ROW_STOCHASTIC_MATRIX_KW
    );
    TokenSet CONSTRAINT_KEYWORDS = TokenSet.create(LOWER_KW, UPPER_KW, OFFSET_KW, MULTIPLIER_KW);
    TokenSet ALL_OPERATORS = TokenSet.create(
        PLUS, MINUS, TIMES, DIVIDE, MODULO, IDIVIDE, LDIVIDE,
        ELT_TIMES, ELT_DIVIDE, POW, ELT_POW,
        OR, AND, EQUALS, NEQUALS, LESS, LEQ, GREATER, GEQ,
        BANG, TRANSPOSE,
        ASSIGN, PLUS_ASSIGN, MINUS_ASSIGN, TIMES_ASSIGN, DIVIDE_ASSIGN,
        ELT_TIMES_ASSIGN, ELT_DIVIDE_ASSIGN, ARROW,
        TILDE, BAR, COLON, QUESTION
    );
    TokenSet COMMENTS      = TokenSet.create(LINE_COMMENT, BLOCK_COMMENT);
    TokenSet NUMBER_LITERALS = TokenSet.create(INT_LITERAL, REAL_LITERAL, IMAG_LITERAL);
    TokenSet IDENTIFIERS   = TokenSet.create(IDENTIFIER, BUILTIN_FUNCTION);
    TokenSet ASSIGNMENT_OPS = TokenSet.create(
        ASSIGN, PLUS_ASSIGN, MINUS_ASSIGN, TIMES_ASSIGN, DIVIDE_ASSIGN,
        ELT_TIMES_ASSIGN, ELT_DIVIDE_ASSIGN, ARROW
    );
}
