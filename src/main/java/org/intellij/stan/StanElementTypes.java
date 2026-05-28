package org.intellij.stan;

import com.intellij.psi.tree.IElementType;

/**
 * IElementType constants for every non-terminal node in the Stan AST.
 * These are the types used by the parser (StanParser) to label PsiBuilder.Marker regions.
 * Token types live in StanTokenTypes; these cover grammar productions only.
 */
public interface StanElementTypes {

    // ---- Top-level program structure ----
    IElementType PROGRAM                    = new StanTokenType("PROGRAM");
    IElementType FUNCTIONS_BLOCK            = new StanTokenType("FUNCTIONS_BLOCK");
    IElementType DATA_BLOCK                 = new StanTokenType("DATA_BLOCK");
    IElementType TRANSFORMED_DATA_BLOCK     = new StanTokenType("TRANSFORMED_DATA_BLOCK");
    IElementType PARAMETERS_BLOCK           = new StanTokenType("PARAMETERS_BLOCK");
    IElementType TRANSFORMED_PARAMETERS_BLOCK = new StanTokenType("TRANSFORMED_PARAMETERS_BLOCK");
    IElementType MODEL_BLOCK                = new StanTokenType("MODEL_BLOCK");
    IElementType GENERATED_QUANTITIES_BLOCK = new StanTokenType("GENERATED_QUANTITIES_BLOCK");

    // ---- Function definition ----
    IElementType FUN_DEF    = new StanTokenType("FUN_DEF");
    IElementType ARG_DECL   = new StanTokenType("ARG_DECL");   // single formal argument
    IElementType PARAM_LIST = new StanTokenType("PARAM_LIST"); // (arg, arg, ...)

    // ---- Variable declarations ----
    // VarDecl covers the full declaration statement: type constraints variables ;
    IElementType VAR_DECL         = new StanTokenType("VAR_DECL");
    IElementType DECLARED_VAR     = new StanTokenType("DECLARED_VAR");  // name [= init]
    IElementType TUPLE_DECL_PACK  = new StanTokenType("TUPLE_DECL_PACK"); // (lv, lv) on lhs

    // ---- Statements ----
    IElementType ASSIGNMENT_STMT          = new StanTokenType("ASSIGNMENT_STMT");
    IElementType FUN_CALL_STMT            = new StanTokenType("FUN_CALL_STMT");
    IElementType TARGET_PLUS_ASSIGN_STMT  = new StanTokenType("TARGET_PLUS_ASSIGN_STMT");
    IElementType JACOBIAN_PLUS_ASSIGN_STMT = new StanTokenType("JACOBIAN_PLUS_ASSIGN_STMT");
    IElementType TILDE_STMT               = new StanTokenType("TILDE_STMT");
    IElementType BREAK_STMT               = new StanTokenType("BREAK_STMT");
    IElementType CONTINUE_STMT            = new StanTokenType("CONTINUE_STMT");
    IElementType RETURN_STMT              = new StanTokenType("RETURN_STMT");
    IElementType PRINT_STMT               = new StanTokenType("PRINT_STMT");
    IElementType REJECT_STMT              = new StanTokenType("REJECT_STMT");
    IElementType FATAL_ERROR_STMT         = new StanTokenType("FATAL_ERROR_STMT");
    IElementType SKIP_STMT                = new StanTokenType("SKIP_STMT");
    IElementType IF_STMT                  = new StanTokenType("IF_STMT");
    IElementType WHILE_STMT               = new StanTokenType("WHILE_STMT");
    IElementType FOR_RANGE_STMT           = new StanTokenType("FOR_RANGE_STMT"); // for i in lo:hi
    IElementType FOR_EACH_STMT            = new StanTokenType("FOR_EACH_STMT");  // for x in expr
    IElementType PROFILE_STMT             = new StanTokenType("PROFILE_STMT");
    IElementType BLOCK_STMT               = new StanTokenType("BLOCK_STMT");

    // ---- Expressions ----
    IElementType TERNARY_IF_EXPR      = new StanTokenType("TERNARY_IF_EXPR");
    IElementType BINARY_OP_EXPR       = new StanTokenType("BINARY_OP_EXPR");
    IElementType PREFIX_OP_EXPR       = new StanTokenType("PREFIX_OP_EXPR");
    IElementType POSTFIX_OP_EXPR      = new StanTokenType("POSTFIX_OP_EXPR");  // transpose '
    IElementType VARIABLE_EXPR        = new StanTokenType("VARIABLE_EXPR");
    IElementType INT_LITERAL_EXPR     = new StanTokenType("INT_LITERAL_EXPR");
    IElementType REAL_LITERAL_EXPR    = new StanTokenType("REAL_LITERAL_EXPR");
    IElementType IMAG_LITERAL_EXPR    = new StanTokenType("IMAG_LITERAL_EXPR");
    IElementType FUN_CALL_EXPR        = new StanTokenType("FUN_CALL_EXPR");
    IElementType COND_DIST_EXPR       = new StanTokenType("COND_DIST_EXPR");  // f(x | mu, sigma)
    IElementType TARGET_EXPR          = new StanTokenType("TARGET_EXPR");      // target()
    IElementType ARRAY_EXPR           = new StanTokenType("ARRAY_EXPR");       // {e1, e2, ...}
    IElementType ROW_VECTOR_EXPR      = new StanTokenType("ROW_VECTOR_EXPR");  // [e1, e2, ...]
    IElementType PAREN_EXPR           = new StanTokenType("PAREN_EXPR");
    IElementType INDEXED_EXPR         = new StanTokenType("INDEXED_EXPR");     // e[idx, ...]
    IElementType TUPLE_PROJECTION_EXPR = new StanTokenType("TUPLE_PROJECTION_EXPR"); // e.N
    IElementType TUPLE_EXPR           = new StanTokenType("TUPLE_EXPR");       // (e1, e2, ...)
    IElementType ARG_LIST             = new StanTokenType("ARG_LIST");

    // ---- Indices ----
    IElementType INDEX_LIST    = new StanTokenType("INDEX_LIST");
    IElementType ALL_INDEX     = new StanTokenType("ALL_INDEX");        // :
    IElementType SINGLE_INDEX  = new StanTokenType("SINGLE_INDEX");     // e
    IElementType UPFROM_INDEX  = new StanTokenType("UPFROM_INDEX");     // e:
    IElementType DOWNFROM_INDEX = new StanTokenType("DOWNFROM_INDEX");  // :e
    IElementType BETWEEN_INDEX = new StanTokenType("BETWEEN_INDEX");    // e:e

    // ---- Truncation ----
    IElementType TRUNCATION = new StanTokenType("TRUNCATION"); // T[lo?, hi?]

    // ---- Types (sized) ----
    IElementType INT_TYPE                    = new StanTokenType("INT_TYPE");
    IElementType REAL_TYPE                   = new StanTokenType("REAL_TYPE");
    IElementType COMPLEX_TYPE                = new StanTokenType("COMPLEX_TYPE");
    IElementType VECTOR_TYPE                 = new StanTokenType("VECTOR_TYPE");
    IElementType ROW_VECTOR_TYPE             = new StanTokenType("ROW_VECTOR_TYPE");
    IElementType MATRIX_TYPE                 = new StanTokenType("MATRIX_TYPE");
    IElementType COMPLEX_VECTOR_TYPE         = new StanTokenType("COMPLEX_VECTOR_TYPE");
    IElementType COMPLEX_ROW_VECTOR_TYPE     = new StanTokenType("COMPLEX_ROW_VECTOR_TYPE");
    IElementType COMPLEX_MATRIX_TYPE         = new StanTokenType("COMPLEX_MATRIX_TYPE");
    IElementType ARRAY_TYPE                  = new StanTokenType("ARRAY_TYPE");
    IElementType TUPLE_TYPE                  = new StanTokenType("TUPLE_TYPE");
    IElementType ORDERED_TYPE                = new StanTokenType("ORDERED_TYPE");
    IElementType POSITIVE_ORDERED_TYPE       = new StanTokenType("POSITIVE_ORDERED_TYPE");
    IElementType SIMPLEX_TYPE                = new StanTokenType("SIMPLEX_TYPE");
    IElementType UNIT_VECTOR_TYPE            = new StanTokenType("UNIT_VECTOR_TYPE");
    IElementType SUM_TO_ZERO_VECTOR_TYPE     = new StanTokenType("SUM_TO_ZERO_VECTOR_TYPE");
    IElementType SUM_TO_ZERO_MATRIX_TYPE     = new StanTokenType("SUM_TO_ZERO_MATRIX_TYPE");
    IElementType CHOLESKY_FACTOR_CORR_TYPE   = new StanTokenType("CHOLESKY_FACTOR_CORR_TYPE");
    IElementType CHOLESKY_FACTOR_COV_TYPE    = new StanTokenType("CHOLESKY_FACTOR_COV_TYPE");
    IElementType CORR_MATRIX_TYPE            = new StanTokenType("CORR_MATRIX_TYPE");
    IElementType COV_MATRIX_TYPE             = new StanTokenType("COV_MATRIX_TYPE");
    IElementType COLUMN_STOCHASTIC_MATRIX_TYPE = new StanTokenType("COLUMN_STOCHASTIC_MATRIX_TYPE");
    IElementType ROW_STOCHASTIC_MATRIX_TYPE  = new StanTokenType("ROW_STOCHASTIC_MATRIX_TYPE");

    // ---- Unsized types (used in function signatures) ----
    IElementType UNSIZED_ARRAY_TYPE = new StanTokenType("UNSIZED_ARRAY_TYPE");
    IElementType UNSIZED_TUPLE_TYPE = new StanTokenType("UNSIZED_TUPLE_TYPE");
    IElementType UNSIZED_DIMS       = new StanTokenType("UNSIZED_DIMS"); // [,,,] dimension markers

    // ---- Constraints ----
    IElementType RANGE_CONSTRAINT       = new StanTokenType("RANGE_CONSTRAINT");        // <lower=...,upper=...>
    IElementType OFFSET_MULT_CONSTRAINT = new StanTokenType("OFFSET_MULT_CONSTRAINT"); // <offset=...,multiplier=...>
}
