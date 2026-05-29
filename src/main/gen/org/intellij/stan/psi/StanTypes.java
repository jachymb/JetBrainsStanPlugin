// This is a generated file. Not intended for manual editing.
package org.intellij.stan.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.intellij.stan.psi.impl.*;

public interface StanTypes {

  IElementType ADD_EXPR = new StanElementType("ADD_EXPR");
  IElementType AND_EXPR = new StanElementType("AND_EXPR");
  IElementType ARG_DECL = new StanElementType("ARG_DECL");
  IElementType ARG_DECL_LIST = new StanElementType("ARG_DECL_LIST");
  IElementType ARRAY_EXPR = new StanElementType("ARRAY_EXPR");
  IElementType ARR_DIMS = new StanElementType("ARR_DIMS");
  IElementType ASSIGNMENT_OP = new StanElementType("ASSIGNMENT_OP");
  IElementType ASSIGNMENT_STMT = new StanElementType("ASSIGNMENT_STMT");
  IElementType ATOMIC_STATEMENT = new StanElementType("ATOMIC_STATEMENT");
  IElementType BLOCK_STMT = new StanElementType("BLOCK_STMT");
  IElementType BREAK_STMT = new StanElementType("BREAK_STMT");
  IElementType COND_DIST_EXPR = new StanElementType("COND_DIST_EXPR");
  IElementType CONSTR_EXPRESSION = new StanElementType("CONSTR_EXPRESSION");
  IElementType CONTINUE_STMT = new StanElementType("CONTINUE_STMT");
  IElementType DATA_BLOCK = new StanElementType("DATA_BLOCK");
  IElementType DECLARED_VAR = new StanElementType("DECLARED_VAR");
  IElementType DECLARED_VAR_EXTRA = new StanElementType("DECLARED_VAR_EXTRA");
  IElementType DECL_IDENTIFIER = new StanElementType("DECL_IDENTIFIER");
  IElementType DECL_IDENTIFIER_AFTER_COMMA = new StanElementType("DECL_IDENTIFIER_AFTER_COMMA");
  IElementType EMPTY_ROW_VECTOR_EXPR = new StanElementType("EMPTY_ROW_VECTOR_EXPR");
  IElementType EMPTY_STMT = new StanElementType("EMPTY_STMT");
  IElementType EQ_EXPR = new StanElementType("EQ_EXPR");
  IElementType EXPRESSION = new StanElementType("EXPRESSION");
  IElementType FATAL_ERROR_STMT = new StanElementType("FATAL_ERROR_STMT");
  IElementType FOR_EACH_STMT = new StanElementType("FOR_EACH_STMT");
  IElementType FOR_RANGE_STMT = new StanElementType("FOR_RANGE_STMT");
  IElementType FUNCTION_BLOCK = new StanElementType("FUNCTION_BLOCK");
  IElementType FUNCTION_DEF = new StanElementType("FUNCTION_DEF");
  IElementType FUN_CALL_EXPR = new StanElementType("FUN_CALL_EXPR");
  IElementType FUN_CALL_STMT = new StanElementType("FUN_CALL_STMT");
  IElementType GENERATED_QUANTITIES_BLOCK = new StanElementType("GENERATED_QUANTITIES_BLOCK");
  IElementType IDENT = new StanElementType("IDENT");
  IElementType IF_ELSE_STMT = new StanElementType("IF_ELSE_STMT");
  IElementType IF_STMT = new StanElementType("IF_STMT");
  IElementType IMAG_LITERAL_EXPR = new StanElementType("IMAG_LITERAL_EXPR");
  IElementType INDEX_EXPR = new StanElementType("INDEX_EXPR");
  IElementType INDEX_ITEM = new StanElementType("INDEX_ITEM");
  IElementType INDEX_LIST = new StanElementType("INDEX_LIST");
  IElementType INT_LITERAL_EXPR = new StanElementType("INT_LITERAL_EXPR");
  IElementType JACOBIAN_PLUS_ASSIGN_STMT = new StanElementType("JACOBIAN_PLUS_ASSIGN_STMT");
  IElementType LDIV_EXPR = new StanElementType("LDIV_EXPR");
  IElementType MODEL_BLOCK = new StanElementType("MODEL_BLOCK");
  IElementType MUL_EXPR = new StanElementType("MUL_EXPR");
  IElementType NESTED_STATEMENT = new StanElementType("NESTED_STATEMENT");
  IElementType NO_ASSIGN_VAR = new StanElementType("NO_ASSIGN_VAR");
  IElementType NO_ASSIGN_VAR_EXTRA = new StanElementType("NO_ASSIGN_VAR_EXTRA");
  IElementType OFFSET_MULT = new StanElementType("OFFSET_MULT");
  IElementType OR_EXPR = new StanElementType("OR_EXPR");
  IElementType PARAMETERS_BLOCK = new StanElementType("PARAMETERS_BLOCK");
  IElementType PAREN_EXPR = new StanElementType("PAREN_EXPR");
  IElementType POSTFIX_EXPR = new StanElementType("POSTFIX_EXPR");
  IElementType POW_EXPR = new StanElementType("POW_EXPR");
  IElementType PRIMARY_EXPR = new StanElementType("PRIMARY_EXPR");
  IElementType PRINTABLE = new StanElementType("PRINTABLE");
  IElementType PRINTABLES = new StanElementType("PRINTABLES");
  IElementType PRINT_STMT = new StanElementType("PRINT_STMT");
  IElementType PROFILE_STMT = new StanElementType("PROFILE_STMT");
  IElementType RANGE = new StanElementType("RANGE");
  IElementType REAL_LITERAL_EXPR = new StanElementType("REAL_LITERAL_EXPR");
  IElementType REJECT_STMT = new StanElementType("REJECT_STMT");
  IElementType REL_EXPR = new StanElementType("REL_EXPR");
  IElementType RESERVED_WORD = new StanElementType("RESERVED_WORD");
  IElementType RETURN_STMT = new StanElementType("RETURN_STMT");
  IElementType RETURN_TYPE = new StanElementType("RETURN_TYPE");
  IElementType ROW_VECTOR_EXPR = new StanElementType("ROW_VECTOR_EXPR");
  IElementType SIZED_BASIC_TYPE = new StanElementType("SIZED_BASIC_TYPE");
  IElementType STATEMENT = new StanElementType("STATEMENT");
  IElementType STRING_LITERAL = new StanElementType("STRING_LITERAL");
  IElementType TARGET_CALL_EXPR = new StanElementType("TARGET_CALL_EXPR");
  IElementType TARGET_PLUS_ASSIGN_STMT = new StanElementType("TARGET_PLUS_ASSIGN_STMT");
  IElementType TILDE_STMT = new StanElementType("TILDE_STMT");
  IElementType TOP_DECLARED_VAR = new StanElementType("TOP_DECLARED_VAR");
  IElementType TOP_DECLARED_VAR_EXTRA = new StanElementType("TOP_DECLARED_VAR_EXTRA");
  IElementType TOP_HIGHER_TYPE = new StanElementType("TOP_HIGHER_TYPE");
  IElementType TOP_TUPLE_TYPE = new StanElementType("TOP_TUPLE_TYPE");
  IElementType TOP_VARDECL_OR_STATEMENT = new StanElementType("TOP_VARDECL_OR_STATEMENT");
  IElementType TOP_VAR_DECL = new StanElementType("TOP_VAR_DECL");
  IElementType TOP_VAR_DECL_NO_ASSIGN = new StanElementType("TOP_VAR_DECL_NO_ASSIGN");
  IElementType TOP_VAR_TYPE = new StanElementType("TOP_VAR_TYPE");
  IElementType TRANSFORMED_DATA_BLOCK = new StanElementType("TRANSFORMED_DATA_BLOCK");
  IElementType TRANSFORMED_PARAMETERS_BLOCK = new StanElementType("TRANSFORMED_PARAMETERS_BLOCK");
  IElementType TRUNCATION = new StanElementType("TRUNCATION");
  IElementType TUPLE_EXPR = new StanElementType("TUPLE_EXPR");
  IElementType TYPE_CONSTRAINT = new StanElementType("TYPE_CONSTRAINT");
  IElementType UNARY_EXPR = new StanElementType("UNARY_EXPR");
  IElementType UNSIZED_BASIC_TYPE = new StanElementType("UNSIZED_BASIC_TYPE");
  IElementType UNSIZED_DIMS = new StanElementType("UNSIZED_DIMS");
  IElementType UNSIZED_TUPLE_TYPE = new StanElementType("UNSIZED_TUPLE_TYPE");
  IElementType UNSIZED_TYPE = new StanElementType("UNSIZED_TYPE");
  IElementType VARDECL_OR_STATEMENT = new StanElementType("VARDECL_OR_STATEMENT");
  IElementType VARIABLE_EXPR = new StanElementType("VARIABLE_EXPR");
  IElementType VAR_DECL = new StanElementType("VAR_DECL");
  IElementType VAR_TUPLE_TYPE = new StanElementType("VAR_TUPLE_TYPE");
  IElementType VAR_TYPE = new StanElementType("VAR_TYPE");
  IElementType WHILE_STMT = new StanElementType("WHILE_STMT");

  IElementType AND = new StanTokenType("&&");
  IElementType ARRAY = new StanTokenType("array");
  IElementType ASSIGN = new StanTokenType("=");
  IElementType BANG = new StanTokenType("!");
  IElementType BAR = new StanTokenType("|");
  IElementType BLOCK_COMMENT = new StanTokenType("BLOCK_COMMENT");
  IElementType BREAK = new StanTokenType("break");
  IElementType CHOLESKYFACTORCORR = new StanTokenType("cholesky_factor_corr");
  IElementType CHOLESKYFACTORCOV = new StanTokenType("cholesky_factor_cov");
  IElementType COLON = new StanTokenType(":");
  IElementType COMMA = new StanTokenType(",");
  IElementType COMPLEX = new StanTokenType("complex");
  IElementType COMPLEXMATRIX = new StanTokenType("complex_matrix");
  IElementType COMPLEXROWVECTOR = new StanTokenType("complex_row_vector");
  IElementType COMPLEXVECTOR = new StanTokenType("complex_vector");
  IElementType CONTINUE = new StanTokenType("continue");
  IElementType CORRMATRIX = new StanTokenType("corr_matrix");
  IElementType COVMATRIX = new StanTokenType("cov_matrix");
  IElementType DATABLOCK = new StanTokenType("data");
  IElementType DIVIDE = new StanTokenType("/");
  IElementType DIVIDEASSIGN = new StanTokenType("/=");
  IElementType DOTNUMERAL = new StanTokenType("DOTNUMERAL");
  IElementType ELSE = new StanTokenType("else");
  IElementType ELTDIVIDE = new StanTokenType("./");
  IElementType ELTDIVIDEASSIGN = new StanTokenType("./=");
  IElementType ELTPOW = new StanTokenType(".^");
  IElementType ELTTIMES = new StanTokenType(".*");
  IElementType ELTTIMESASSIGN = new StanTokenType(".*=");
  IElementType EQUALS = new StanTokenType("==");
  IElementType FATAL_ERROR = new StanTokenType("fatal_error");
  IElementType FOR = new StanTokenType("for");
  IElementType FUNCTIONBLOCK = new StanTokenType("functions");
  IElementType GENERATEDQUANTITIESBLOCK = new StanTokenType("generated quantities");
  IElementType GEQ = new StanTokenType(">=");
  IElementType HAT = new StanTokenType("^");
  IElementType IDENTIFIER = new StanTokenType("IDENTIFIER");
  IElementType IDIVIDE = new StanTokenType("%/%");
  IElementType IF = new StanTokenType("if");
  IElementType IMAGNUMERAL = new StanTokenType("IMAGNUMERAL");
  IElementType IN = new StanTokenType("in");
  IElementType INT = new StanTokenType("int");
  IElementType INTNUMERAL = new StanTokenType("INTNUMERAL");
  IElementType JACOBIAN = new StanTokenType("jacobian");
  IElementType LABRACK = new StanTokenType("<");
  IElementType LBRACE = new StanTokenType("{");
  IElementType LBRACK = new StanTokenType("[");
  IElementType LDIVIDE = new StanTokenType("\\");
  IElementType LEQ = new StanTokenType("<=");
  IElementType LINE_COMMENT = new StanTokenType("LINE_COMMENT");
  IElementType LOWER = new StanTokenType("lower");
  IElementType LPAREN = new StanTokenType("(");
  IElementType MATRIX = new StanTokenType("matrix");
  IElementType MINUS = new StanTokenType("-");
  IElementType MINUSASSIGN = new StanTokenType("-=");
  IElementType MODELBLOCK = new StanTokenType("model");
  IElementType MODULO = new StanTokenType("%");
  IElementType MULTIPLIER = new StanTokenType("multiplier");
  IElementType NEQUALS = new StanTokenType("!=");
  IElementType OFFSET = new StanTokenType("offset");
  IElementType OR = new StanTokenType("||");
  IElementType ORDERED = new StanTokenType("ordered");
  IElementType PARAMETERSBLOCK = new StanTokenType("parameters");
  IElementType PLUS = new StanTokenType("+");
  IElementType PLUSASSIGN = new StanTokenType("+=");
  IElementType POSITIVEORDERED = new StanTokenType("positive_ordered");
  IElementType PRINT = new StanTokenType("print");
  IElementType PROFILE = new StanTokenType("profile");
  IElementType QMARK = new StanTokenType("?");
  IElementType RABRACK = new StanTokenType(">");
  IElementType RBRACE = new StanTokenType("}");
  IElementType RBRACK = new StanTokenType("]");
  IElementType REAL = new StanTokenType("real");
  IElementType REALNUMERAL = new StanTokenType("REALNUMERAL");
  IElementType REJECT = new StanTokenType("reject");
  IElementType RETURN = new StanTokenType("return");
  IElementType ROWVECTOR = new StanTokenType("row_vector");
  IElementType RPAREN = new StanTokenType(")");
  IElementType SEMICOLON = new StanTokenType(";");
  IElementType SIMPLEX = new StanTokenType("simplex");
  IElementType STOCHASTICCOLUMNMATRIX = new StanTokenType("column_stochastic_matrix");
  IElementType STOCHASTICROWMATRIX = new StanTokenType("row_stochastic_matrix");
  IElementType STRINGLITERAL = new StanTokenType("STRINGLITERAL");
  IElementType SUMTOZEROMAT = new StanTokenType("sum_to_zero_matrix");
  IElementType SUMTOZEROVEC = new StanTokenType("sum_to_zero_vector");
  IElementType TARGET = new StanTokenType("target");
  IElementType TILDE = new StanTokenType("~");
  IElementType TIMES = new StanTokenType("*");
  IElementType TIMESASSIGN = new StanTokenType("*=");
  IElementType TRANSFORMEDDATABLOCK = new StanTokenType("transformed data");
  IElementType TRANSFORMEDPARAMETERSBLOCK = new StanTokenType("transformed parameters");
  IElementType TRANSPOSE = new StanTokenType("'");
  IElementType TRUNCATE = new StanTokenType("T");
  IElementType TUPLE = new StanTokenType("tuple");
  IElementType UNITVECTOR = new StanTokenType("unit_vector");
  IElementType UPPER = new StanTokenType("upper");
  IElementType VECTOR = new StanTokenType("vector");
  IElementType VOID = new StanTokenType("void");
  IElementType WHILE = new StanTokenType("while");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ADD_EXPR) {
        return new StanAddExprImpl(node);
      }
      else if (type == AND_EXPR) {
        return new StanAndExprImpl(node);
      }
      else if (type == ARG_DECL) {
        return new StanArgDeclImpl(node);
      }
      else if (type == ARG_DECL_LIST) {
        return new StanArgDeclListImpl(node);
      }
      else if (type == ARRAY_EXPR) {
        return new StanArrayExprImpl(node);
      }
      else if (type == ARR_DIMS) {
        return new StanArrDimsImpl(node);
      }
      else if (type == ASSIGNMENT_OP) {
        return new StanAssignmentOpImpl(node);
      }
      else if (type == ASSIGNMENT_STMT) {
        return new StanAssignmentStmtImpl(node);
      }
      else if (type == ATOMIC_STATEMENT) {
        return new StanAtomicStatementImpl(node);
      }
      else if (type == BLOCK_STMT) {
        return new StanBlockStmtImpl(node);
      }
      else if (type == BREAK_STMT) {
        return new StanBreakStmtImpl(node);
      }
      else if (type == COND_DIST_EXPR) {
        return new StanCondDistExprImpl(node);
      }
      else if (type == CONSTR_EXPRESSION) {
        return new StanConstrExpressionImpl(node);
      }
      else if (type == CONTINUE_STMT) {
        return new StanContinueStmtImpl(node);
      }
      else if (type == DATA_BLOCK) {
        return new StanDataBlockImpl(node);
      }
      else if (type == DECLARED_VAR) {
        return new StanDeclaredVarImpl(node);
      }
      else if (type == DECLARED_VAR_EXTRA) {
        return new StanDeclaredVarExtraImpl(node);
      }
      else if (type == DECL_IDENTIFIER) {
        return new StanDeclIdentifierImpl(node);
      }
      else if (type == DECL_IDENTIFIER_AFTER_COMMA) {
        return new StanDeclIdentifierAfterCommaImpl(node);
      }
      else if (type == EMPTY_ROW_VECTOR_EXPR) {
        return new StanEmptyRowVectorExprImpl(node);
      }
      else if (type == EMPTY_STMT) {
        return new StanEmptyStmtImpl(node);
      }
      else if (type == EQ_EXPR) {
        return new StanEqExprImpl(node);
      }
      else if (type == EXPRESSION) {
        return new StanExpressionImpl(node);
      }
      else if (type == FATAL_ERROR_STMT) {
        return new StanFatalErrorStmtImpl(node);
      }
      else if (type == FOR_EACH_STMT) {
        return new StanForEachStmtImpl(node);
      }
      else if (type == FOR_RANGE_STMT) {
        return new StanForRangeStmtImpl(node);
      }
      else if (type == FUNCTION_BLOCK) {
        return new StanFunctionBlockImpl(node);
      }
      else if (type == FUNCTION_DEF) {
        return new StanFunctionDefImpl(node);
      }
      else if (type == FUN_CALL_EXPR) {
        return new StanFunCallExprImpl(node);
      }
      else if (type == FUN_CALL_STMT) {
        return new StanFunCallStmtImpl(node);
      }
      else if (type == GENERATED_QUANTITIES_BLOCK) {
        return new StanGeneratedQuantitiesBlockImpl(node);
      }
      else if (type == IDENT) {
        return new StanIdentImpl(node);
      }
      else if (type == IF_ELSE_STMT) {
        return new StanIfElseStmtImpl(node);
      }
      else if (type == IF_STMT) {
        return new StanIfStmtImpl(node);
      }
      else if (type == IMAG_LITERAL_EXPR) {
        return new StanImagLiteralExprImpl(node);
      }
      else if (type == INDEX_EXPR) {
        return new StanIndexExprImpl(node);
      }
      else if (type == INDEX_ITEM) {
        return new StanIndexItemImpl(node);
      }
      else if (type == INDEX_LIST) {
        return new StanIndexListImpl(node);
      }
      else if (type == INT_LITERAL_EXPR) {
        return new StanIntLiteralExprImpl(node);
      }
      else if (type == JACOBIAN_PLUS_ASSIGN_STMT) {
        return new StanJacobianPlusAssignStmtImpl(node);
      }
      else if (type == LDIV_EXPR) {
        return new StanLdivExprImpl(node);
      }
      else if (type == MODEL_BLOCK) {
        return new StanModelBlockImpl(node);
      }
      else if (type == MUL_EXPR) {
        return new StanMulExprImpl(node);
      }
      else if (type == NESTED_STATEMENT) {
        return new StanNestedStatementImpl(node);
      }
      else if (type == NO_ASSIGN_VAR) {
        return new StanNoAssignVarImpl(node);
      }
      else if (type == NO_ASSIGN_VAR_EXTRA) {
        return new StanNoAssignVarExtraImpl(node);
      }
      else if (type == OFFSET_MULT) {
        return new StanOffsetMultImpl(node);
      }
      else if (type == OR_EXPR) {
        return new StanOrExprImpl(node);
      }
      else if (type == PARAMETERS_BLOCK) {
        return new StanParametersBlockImpl(node);
      }
      else if (type == PAREN_EXPR) {
        return new StanParenExprImpl(node);
      }
      else if (type == POSTFIX_EXPR) {
        return new StanPostfixExprImpl(node);
      }
      else if (type == POW_EXPR) {
        return new StanPowExprImpl(node);
      }
      else if (type == PRIMARY_EXPR) {
        return new StanPrimaryExprImpl(node);
      }
      else if (type == PRINTABLE) {
        return new StanPrintableImpl(node);
      }
      else if (type == PRINTABLES) {
        return new StanPrintablesImpl(node);
      }
      else if (type == PRINT_STMT) {
        return new StanPrintStmtImpl(node);
      }
      else if (type == PROFILE_STMT) {
        return new StanProfileStmtImpl(node);
      }
      else if (type == RANGE) {
        return new StanRangeImpl(node);
      }
      else if (type == REAL_LITERAL_EXPR) {
        return new StanRealLiteralExprImpl(node);
      }
      else if (type == REJECT_STMT) {
        return new StanRejectStmtImpl(node);
      }
      else if (type == REL_EXPR) {
        return new StanRelExprImpl(node);
      }
      else if (type == RESERVED_WORD) {
        return new StanReservedWordImpl(node);
      }
      else if (type == RETURN_STMT) {
        return new StanReturnStmtImpl(node);
      }
      else if (type == RETURN_TYPE) {
        return new StanReturnTypeImpl(node);
      }
      else if (type == ROW_VECTOR_EXPR) {
        return new StanRowVectorExprImpl(node);
      }
      else if (type == SIZED_BASIC_TYPE) {
        return new StanSizedBasicTypeImpl(node);
      }
      else if (type == STATEMENT) {
        return new StanStatementImpl(node);
      }
      else if (type == STRING_LITERAL) {
        return new StanStringLiteralImpl(node);
      }
      else if (type == TARGET_CALL_EXPR) {
        return new StanTargetCallExprImpl(node);
      }
      else if (type == TARGET_PLUS_ASSIGN_STMT) {
        return new StanTargetPlusAssignStmtImpl(node);
      }
      else if (type == TILDE_STMT) {
        return new StanTildeStmtImpl(node);
      }
      else if (type == TOP_DECLARED_VAR) {
        return new StanTopDeclaredVarImpl(node);
      }
      else if (type == TOP_DECLARED_VAR_EXTRA) {
        return new StanTopDeclaredVarExtraImpl(node);
      }
      else if (type == TOP_HIGHER_TYPE) {
        return new StanTopHigherTypeImpl(node);
      }
      else if (type == TOP_TUPLE_TYPE) {
        return new StanTopTupleTypeImpl(node);
      }
      else if (type == TOP_VARDECL_OR_STATEMENT) {
        return new StanTopVardeclOrStatementImpl(node);
      }
      else if (type == TOP_VAR_DECL) {
        return new StanTopVarDeclImpl(node);
      }
      else if (type == TOP_VAR_DECL_NO_ASSIGN) {
        return new StanTopVarDeclNoAssignImpl(node);
      }
      else if (type == TOP_VAR_TYPE) {
        return new StanTopVarTypeImpl(node);
      }
      else if (type == TRANSFORMED_DATA_BLOCK) {
        return new StanTransformedDataBlockImpl(node);
      }
      else if (type == TRANSFORMED_PARAMETERS_BLOCK) {
        return new StanTransformedParametersBlockImpl(node);
      }
      else if (type == TRUNCATION) {
        return new StanTruncationImpl(node);
      }
      else if (type == TUPLE_EXPR) {
        return new StanTupleExprImpl(node);
      }
      else if (type == TYPE_CONSTRAINT) {
        return new StanTypeConstraintImpl(node);
      }
      else if (type == UNARY_EXPR) {
        return new StanUnaryExprImpl(node);
      }
      else if (type == UNSIZED_BASIC_TYPE) {
        return new StanUnsizedBasicTypeImpl(node);
      }
      else if (type == UNSIZED_DIMS) {
        return new StanUnsizedDimsImpl(node);
      }
      else if (type == UNSIZED_TUPLE_TYPE) {
        return new StanUnsizedTupleTypeImpl(node);
      }
      else if (type == UNSIZED_TYPE) {
        return new StanUnsizedTypeImpl(node);
      }
      else if (type == VARDECL_OR_STATEMENT) {
        return new StanVardeclOrStatementImpl(node);
      }
      else if (type == VARIABLE_EXPR) {
        return new StanVariableExprImpl(node);
      }
      else if (type == VAR_DECL) {
        return new StanVarDeclImpl(node);
      }
      else if (type == VAR_TUPLE_TYPE) {
        return new StanVarTupleTypeImpl(node);
      }
      else if (type == VAR_TYPE) {
        return new StanVarTypeImpl(node);
      }
      else if (type == WHILE_STMT) {
        return new StanWhileStmtImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
