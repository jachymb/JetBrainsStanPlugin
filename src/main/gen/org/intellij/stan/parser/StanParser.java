// This is a generated file. Not intended for manual editing.
package org.intellij.stan.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static org.intellij.stan.psi.StanTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class StanParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType root_, PsiBuilder builder_) {
    parseLight(root_, builder_);
    return builder_.getTreeBuilt();
  }

  public void parseLight(IElementType root_, PsiBuilder builder_) {
    boolean result_;
    builder_ = adapt_builder_(root_, builder_, this, null);
    Marker marker_ = enter_section_(builder_, 0, _COLLAPSE_, null);
    result_ = parse_root_(root_, builder_);
    exit_section_(builder_, 0, marker_, root_, result_, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType root_, PsiBuilder builder_) {
    return parse_root_(root_, builder_, 0);
  }

  static boolean parse_root_(IElementType root_, PsiBuilder builder_, int level_) {
    return program(builder_, level_ + 1);
  }

  /* ********************************************************** */
  // add_expr PLUS  mul_expr
  //            | add_expr MINUS mul_expr
  //            | mul_expr
  public static boolean add_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "add_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ADD_EXPR, "<add expr>");
    result_ = add_expr_0(builder_, level_ + 1);
    if (!result_) result_ = add_expr_1(builder_, level_ + 1);
    if (!result_) result_ = mul_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // add_expr PLUS  mul_expr
  private static boolean add_expr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "add_expr_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = add_expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PLUS);
    result_ = result_ && mul_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // add_expr MINUS mul_expr
  private static boolean add_expr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "add_expr_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = add_expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, MINUS);
    result_ = result_ && mul_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // and_expr AND eq_expr
  //            | eq_expr
  public static boolean and_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "and_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, AND_EXPR, "<and expr>");
    result_ = and_expr_0(builder_, level_ + 1);
    if (!result_) result_ = eq_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // and_expr AND eq_expr
  private static boolean and_expr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "and_expr_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = and_expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, AND);
    result_ = result_ && eq_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // DATABLOCK? unsized_type decl_identifier
  public static boolean arg_decl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "arg_decl")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ARG_DECL, "<arg decl>");
    result_ = arg_decl_0(builder_, level_ + 1);
    result_ = result_ && unsized_type(builder_, level_ + 1);
    result_ = result_ && decl_identifier(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // DATABLOCK?
  private static boolean arg_decl_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "arg_decl_0")) return false;
    consumeToken(builder_, DATABLOCK);
    return true;
  }

  /* ********************************************************** */
  // arg_decl (COMMA arg_decl)*
  public static boolean arg_decl_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "arg_decl_list")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ARG_DECL_LIST, "<arg decl list>");
    result_ = arg_decl(builder_, level_ + 1);
    result_ = result_ && arg_decl_list_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (COMMA arg_decl)*
  private static boolean arg_decl_list_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "arg_decl_list_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!arg_decl_list_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "arg_decl_list_1", pos_)) break;
    }
    return true;
  }

  // COMMA arg_decl
  private static boolean arg_decl_list_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "arg_decl_list_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && arg_decl(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ARRAY LBRACK expression (COMMA expression)* RBRACK
  public static boolean arr_dims(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "arr_dims")) return false;
    if (!nextTokenIs(builder_, ARRAY)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, ARRAY, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && arr_dims_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, ARR_DIMS, result_);
    return result_;
  }

  // (COMMA expression)*
  private static boolean arr_dims_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "arr_dims_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!arr_dims_3_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "arr_dims_3", pos_)) break;
    }
    return true;
  }

  // COMMA expression
  private static boolean arr_dims_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "arr_dims_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // LBRACE expression (COMMA expression)* RBRACE
  public static boolean array_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "array_expr")) return false;
    if (!nextTokenIs(builder_, LBRACE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LBRACE);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && array_expr_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, ARRAY_EXPR, result_);
    return result_;
  }

  // (COMMA expression)*
  private static boolean array_expr_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "array_expr_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!array_expr_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "array_expr_2", pos_)) break;
    }
    return true;
  }

  // COMMA expression
  private static boolean array_expr_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "array_expr_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ASSIGN | PLUSASSIGN | MINUSASSIGN | TIMESASSIGN | DIVIDEASSIGN
  //                 | ELTTIMESASSIGN | ELTDIVIDEASSIGN
  public static boolean assignment_op(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "assignment_op")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ASSIGNMENT_OP, "<assignment op>");
    result_ = consumeToken(builder_, ASSIGN);
    if (!result_) result_ = consumeToken(builder_, PLUSASSIGN);
    if (!result_) result_ = consumeToken(builder_, MINUSASSIGN);
    if (!result_) result_ = consumeToken(builder_, TIMESASSIGN);
    if (!result_) result_ = consumeToken(builder_, DIVIDEASSIGN);
    if (!result_) result_ = consumeToken(builder_, ELTTIMESASSIGN);
    if (!result_) result_ = consumeToken(builder_, ELTDIVIDEASSIGN);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // expression assignment_op expression SEMICOLON
  public static boolean assignment_stmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "assignment_stmt")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ASSIGNMENT_STMT, "<assignment stmt>");
    result_ = expression(builder_, level_ + 1);
    result_ = result_ && assignment_op(builder_, level_ + 1);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // assignment_stmt
  //   | tilde_stmt
  //   | fun_call_stmt
  //   | target_plus_assign_stmt
  //   | jacobian_plus_assign_stmt
  //   | break_stmt
  //   | continue_stmt
  //   | print_stmt
  //   | reject_stmt
  //   | fatal_error_stmt
  //   | return_stmt
  //   | empty_stmt
  public static boolean atomic_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "atomic_statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ATOMIC_STATEMENT, "<atomic statement>");
    result_ = assignment_stmt(builder_, level_ + 1);
    if (!result_) result_ = tilde_stmt(builder_, level_ + 1);
    if (!result_) result_ = fun_call_stmt(builder_, level_ + 1);
    if (!result_) result_ = target_plus_assign_stmt(builder_, level_ + 1);
    if (!result_) result_ = jacobian_plus_assign_stmt(builder_, level_ + 1);
    if (!result_) result_ = break_stmt(builder_, level_ + 1);
    if (!result_) result_ = continue_stmt(builder_, level_ + 1);
    if (!result_) result_ = print_stmt(builder_, level_ + 1);
    if (!result_) result_ = reject_stmt(builder_, level_ + 1);
    if (!result_) result_ = fatal_error_stmt(builder_, level_ + 1);
    if (!result_) result_ = return_stmt(builder_, level_ + 1);
    if (!result_) result_ = empty_stmt(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // LBRACE vardecl_or_statement* RBRACE
  public static boolean block_stmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "block_stmt")) return false;
    if (!nextTokenIs(builder_, LBRACE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LBRACE);
    result_ = result_ && block_stmt_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, BLOCK_STMT, result_);
    return result_;
  }

  // vardecl_or_statement*
  private static boolean block_stmt_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "block_stmt_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!vardecl_or_statement(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "block_stmt_1", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // BREAK    SEMICOLON
  public static boolean break_stmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "break_stmt")) return false;
    if (!nextTokenIs(builder_, BREAK)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, BREAK, SEMICOLON);
    exit_section_(builder_, marker_, BREAK_STMT, result_);
    return result_;
  }

  /* ********************************************************** */
  // ident LPAREN expression BAR (expression (COMMA expression)*)? RPAREN
  public static boolean cond_dist_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cond_dist_expr")) return false;
    if (!nextTokenIs(builder_, "<cond dist expr>", IDENTIFIER, TRUNCATE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, COND_DIST_EXPR, "<cond dist expr>");
    result_ = ident(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LPAREN);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, BAR);
    result_ = result_ && cond_dist_expr_4(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (expression (COMMA expression)*)?
  private static boolean cond_dist_expr_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cond_dist_expr_4")) return false;
    cond_dist_expr_4_0(builder_, level_ + 1);
    return true;
  }

  // expression (COMMA expression)*
  private static boolean cond_dist_expr_4_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cond_dist_expr_4_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = expression(builder_, level_ + 1);
    result_ = result_ && cond_dist_expr_4_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA expression)*
  private static boolean cond_dist_expr_4_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cond_dist_expr_4_0_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!cond_dist_expr_4_0_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "cond_dist_expr_4_0_1", pos_)) break;
    }
    return true;
  }

  // COMMA expression
  private static boolean cond_dist_expr_4_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cond_dist_expr_4_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // expression
  public static boolean constr_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "constr_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CONSTR_EXPRESSION, "<constr expression>");
    result_ = expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // CONTINUE SEMICOLON
  public static boolean continue_stmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "continue_stmt")) return false;
    if (!nextTokenIs(builder_, CONTINUE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, CONTINUE, SEMICOLON);
    exit_section_(builder_, marker_, CONTINUE_STMT, result_);
    return result_;
  }

  /* ********************************************************** */
  // DATABLOCK                 LBRACE top_var_decl_no_assign*    RBRACE
  public static boolean data_block(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "data_block")) return false;
    if (!nextTokenIs(builder_, DATABLOCK)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, DATABLOCK, LBRACE);
    result_ = result_ && data_block_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, DATA_BLOCK, result_);
    return result_;
  }

  // top_var_decl_no_assign*
  private static boolean data_block_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "data_block_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!top_var_decl_no_assign(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "data_block_2", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // ident | reserved_word
  public static boolean decl_identifier(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "decl_identifier")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DECL_IDENTIFIER, "<decl identifier>");
    result_ = ident(builder_, level_ + 1);
    if (!result_) result_ = reserved_word(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // ident | reserved_word
  public static boolean decl_identifier_after_comma(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "decl_identifier_after_comma")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DECL_IDENTIFIER_AFTER_COMMA, "<decl identifier after comma>");
    result_ = ident(builder_, level_ + 1);
    if (!result_) result_ = reserved_word(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // decl_identifier (ASSIGN expression)?
  public static boolean declared_var(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "declared_var")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DECLARED_VAR, "<declared var>");
    result_ = decl_identifier(builder_, level_ + 1);
    result_ = result_ && declared_var_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (ASSIGN expression)?
  private static boolean declared_var_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "declared_var_1")) return false;
    declared_var_1_0(builder_, level_ + 1);
    return true;
  }

  // ASSIGN expression
  private static boolean declared_var_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "declared_var_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ASSIGN);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // decl_identifier_after_comma (ASSIGN expression)?
  public static boolean declared_var_extra(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "declared_var_extra")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DECLARED_VAR_EXTRA, "<declared var extra>");
    result_ = decl_identifier_after_comma(builder_, level_ + 1);
    result_ = result_ && declared_var_extra_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (ASSIGN expression)?
  private static boolean declared_var_extra_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "declared_var_extra_1")) return false;
    declared_var_extra_1_0(builder_, level_ + 1);
    return true;
  }

  // ASSIGN expression
  private static boolean declared_var_extra_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "declared_var_extra_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ASSIGN);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // LBRACK RBRACK
  public static boolean empty_row_vector_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "empty_row_vector_expr")) return false;
    if (!nextTokenIs(builder_, LBRACK)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, LBRACK, RBRACK);
    exit_section_(builder_, marker_, EMPTY_ROW_VECTOR_EXPR, result_);
    return result_;
  }

  /* ********************************************************** */
  // SEMICOLON
  public static boolean empty_stmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "empty_stmt")) return false;
    if (!nextTokenIs(builder_, SEMICOLON)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, EMPTY_STMT, result_);
    return result_;
  }

  /* ********************************************************** */
  // eq_expr EQUALS  rel_expr
  //           | eq_expr NEQUALS rel_expr
  //           | rel_expr
  public static boolean eq_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "eq_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EQ_EXPR, "<eq expr>");
    result_ = eq_expr_0(builder_, level_ + 1);
    if (!result_) result_ = eq_expr_1(builder_, level_ + 1);
    if (!result_) result_ = rel_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // eq_expr EQUALS  rel_expr
  private static boolean eq_expr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "eq_expr_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = eq_expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, EQUALS);
    result_ = result_ && rel_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // eq_expr NEQUALS rel_expr
  private static boolean eq_expr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "eq_expr_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = eq_expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, NEQUALS);
    result_ = result_ && rel_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // or_expr QMARK expression COLON expression
  //              | or_expr
  public static boolean expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPRESSION, "<expression>");
    result_ = expression_0(builder_, level_ + 1);
    if (!result_) result_ = or_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // or_expr QMARK expression COLON expression
  private static boolean expression_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expression_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = or_expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, QMARK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // FATAL_ERROR LPAREN printables RPAREN SEMICOLON
  public static boolean fatal_error_stmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "fatal_error_stmt")) return false;
    if (!nextTokenIs(builder_, FATAL_ERROR)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, FATAL_ERROR, LPAREN);
    result_ = result_ && printables(builder_, level_ + 1);
    result_ = result_ && consumeTokens(builder_, 0, RPAREN, SEMICOLON);
    exit_section_(builder_, marker_, FATAL_ERROR_STMT, result_);
    return result_;
  }

  /* ********************************************************** */
  // FOR LPAREN ident IN expression RPAREN vardecl_or_statement
  public static boolean for_each_stmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "for_each_stmt")) return false;
    if (!nextTokenIs(builder_, FOR)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, FOR, LPAREN);
    result_ = result_ && ident(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, IN);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    result_ = result_ && vardecl_or_statement(builder_, level_ + 1);
    exit_section_(builder_, marker_, FOR_EACH_STMT, result_);
    return result_;
  }

  /* ********************************************************** */
  // FOR LPAREN ident IN expression COLON expression RPAREN vardecl_or_statement
  public static boolean for_range_stmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "for_range_stmt")) return false;
    if (!nextTokenIs(builder_, FOR)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, FOR, LPAREN);
    result_ = result_ && ident(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, IN);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    result_ = result_ && vardecl_or_statement(builder_, level_ + 1);
    exit_section_(builder_, marker_, FOR_RANGE_STMT, result_);
    return result_;
  }

  /* ********************************************************** */
  // ident LPAREN (expression (COMMA expression)*)? RPAREN
  public static boolean fun_call_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "fun_call_expr")) return false;
    if (!nextTokenIs(builder_, "<fun call expr>", IDENTIFIER, TRUNCATE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FUN_CALL_EXPR, "<fun call expr>");
    result_ = ident(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LPAREN);
    result_ = result_ && fun_call_expr_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (expression (COMMA expression)*)?
  private static boolean fun_call_expr_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "fun_call_expr_2")) return false;
    fun_call_expr_2_0(builder_, level_ + 1);
    return true;
  }

  // expression (COMMA expression)*
  private static boolean fun_call_expr_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "fun_call_expr_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = expression(builder_, level_ + 1);
    result_ = result_ && fun_call_expr_2_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA expression)*
  private static boolean fun_call_expr_2_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "fun_call_expr_2_0_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!fun_call_expr_2_0_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "fun_call_expr_2_0_1", pos_)) break;
    }
    return true;
  }

  // COMMA expression
  private static boolean fun_call_expr_2_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "fun_call_expr_2_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ident LPAREN (expression (COMMA expression)*)? RPAREN SEMICOLON
  public static boolean fun_call_stmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "fun_call_stmt")) return false;
    if (!nextTokenIs(builder_, "<fun call stmt>", IDENTIFIER, TRUNCATE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FUN_CALL_STMT, "<fun call stmt>");
    result_ = ident(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LPAREN);
    result_ = result_ && fun_call_stmt_2(builder_, level_ + 1);
    result_ = result_ && consumeTokens(builder_, 0, RPAREN, SEMICOLON);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (expression (COMMA expression)*)?
  private static boolean fun_call_stmt_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "fun_call_stmt_2")) return false;
    fun_call_stmt_2_0(builder_, level_ + 1);
    return true;
  }

  // expression (COMMA expression)*
  private static boolean fun_call_stmt_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "fun_call_stmt_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = expression(builder_, level_ + 1);
    result_ = result_ && fun_call_stmt_2_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA expression)*
  private static boolean fun_call_stmt_2_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "fun_call_stmt_2_0_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!fun_call_stmt_2_0_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "fun_call_stmt_2_0_1", pos_)) break;
    }
    return true;
  }

  // COMMA expression
  private static boolean fun_call_stmt_2_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "fun_call_stmt_2_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // FUNCTIONBLOCK             LBRACE function_def*              RBRACE
  public static boolean function_block(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "function_block")) return false;
    if (!nextTokenIs(builder_, FUNCTIONBLOCK)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, FUNCTIONBLOCK, LBRACE);
    result_ = result_ && function_block_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, FUNCTION_BLOCK, result_);
    return result_;
  }

  // function_def*
  private static boolean function_block_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "function_block_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!function_def(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "function_block_2", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // return_type decl_identifier LPAREN arg_decl_list? RPAREN statement
  public static boolean function_def(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "function_def")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FUNCTION_DEF, "<function def>");
    result_ = return_type(builder_, level_ + 1);
    result_ = result_ && decl_identifier(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LPAREN);
    result_ = result_ && function_def_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    result_ = result_ && statement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // arg_decl_list?
  private static boolean function_def_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "function_def_3")) return false;
    arg_decl_list(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // GENERATEDQUANTITIESBLOCK  LBRACE top_vardecl_or_statement*  RBRACE
  public static boolean generated_quantities_block(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "generated_quantities_block")) return false;
    if (!nextTokenIs(builder_, GENERATEDQUANTITIESBLOCK)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, GENERATEDQUANTITIESBLOCK, LBRACE);
    result_ = result_ && generated_quantities_block_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, GENERATED_QUANTITIES_BLOCK, result_);
    return result_;
  }

  // top_vardecl_or_statement*
  private static boolean generated_quantities_block_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "generated_quantities_block_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!top_vardecl_or_statement(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "generated_quantities_block_2", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER | TRUNCATE
  public static boolean ident(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ident")) return false;
    if (!nextTokenIs(builder_, "<ident>", IDENTIFIER, TRUNCATE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, IDENT, "<ident>");
    result_ = consumeToken(builder_, IDENTIFIER);
    if (!result_) result_ = consumeToken(builder_, TRUNCATE);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // IF LPAREN expression RPAREN vardecl_or_statement ELSE vardecl_or_statement
  public static boolean if_else_stmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "if_else_stmt")) return false;
    if (!nextTokenIs(builder_, IF)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, IF, LPAREN);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    result_ = result_ && vardecl_or_statement(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ELSE);
    result_ = result_ && vardecl_or_statement(builder_, level_ + 1);
    exit_section_(builder_, marker_, IF_ELSE_STMT, result_);
    return result_;
  }

  /* ********************************************************** */
  // IF LPAREN expression RPAREN vardecl_or_statement
  public static boolean if_stmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "if_stmt")) return false;
    if (!nextTokenIs(builder_, IF)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, IF, LPAREN);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    result_ = result_ && vardecl_or_statement(builder_, level_ + 1);
    exit_section_(builder_, marker_, IF_STMT, result_);
    return result_;
  }

  /* ********************************************************** */
  // IMAGNUMERAL
  public static boolean imag_literal_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "imag_literal_expr")) return false;
    if (!nextTokenIs(builder_, IMAGNUMERAL)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IMAGNUMERAL);
    exit_section_(builder_, marker_, IMAG_LITERAL_EXPR, result_);
    return result_;
  }

  /* ********************************************************** */
  // index_expr LBRACK index_list RBRACK
  //              | primary_expr
  public static boolean index_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "index_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, INDEX_EXPR, "<index expr>");
    result_ = index_expr_0(builder_, level_ + 1);
    if (!result_) result_ = primary_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // index_expr LBRACK index_list RBRACK
  private static boolean index_expr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "index_expr_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = index_expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LBRACK);
    result_ = result_ && index_list(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // expression COLON expression
  //              | expression COLON
  //              | COLON expression
  //              | COLON
  //              | expression
  public static boolean index_item(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "index_item")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, INDEX_ITEM, "<index item>");
    result_ = index_item_0(builder_, level_ + 1);
    if (!result_) result_ = index_item_1(builder_, level_ + 1);
    if (!result_) result_ = index_item_2(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, COLON);
    if (!result_) result_ = expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // expression COLON expression
  private static boolean index_item_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "index_item_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // expression COLON
  private static boolean index_item_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "index_item_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // COLON expression
  private static boolean index_item_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "index_item_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COLON);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // index_item? (COMMA index_item?)*
  public static boolean index_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "index_list")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, INDEX_LIST, "<index list>");
    result_ = index_list_0(builder_, level_ + 1);
    result_ = result_ && index_list_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // index_item?
  private static boolean index_list_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "index_list_0")) return false;
    index_item(builder_, level_ + 1);
    return true;
  }

  // (COMMA index_item?)*
  private static boolean index_list_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "index_list_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!index_list_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "index_list_1", pos_)) break;
    }
    return true;
  }

  // COMMA index_item?
  private static boolean index_list_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "index_list_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && index_list_1_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // index_item?
  private static boolean index_list_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "index_list_1_0_1")) return false;
    index_item(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // INTNUMERAL
  public static boolean int_literal_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "int_literal_expr")) return false;
    if (!nextTokenIs(builder_, INTNUMERAL)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, INTNUMERAL);
    exit_section_(builder_, marker_, INT_LITERAL_EXPR, result_);
    return result_;
  }

  /* ********************************************************** */
  // JACOBIAN PLUSASSIGN expression SEMICOLON
  public static boolean jacobian_plus_assign_stmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "jacobian_plus_assign_stmt")) return false;
    if (!nextTokenIs(builder_, JACOBIAN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, JACOBIAN, PLUSASSIGN);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, JACOBIAN_PLUS_ASSIGN_STMT, result_);
    return result_;
  }

  /* ********************************************************** */
  // ldiv_expr LDIVIDE unary_expr
  //             | ldiv_expr IDIVIDE unary_expr
  //             | unary_expr
  public static boolean ldiv_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ldiv_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LDIV_EXPR, "<ldiv expr>");
    result_ = ldiv_expr_0(builder_, level_ + 1);
    if (!result_) result_ = ldiv_expr_1(builder_, level_ + 1);
    if (!result_) result_ = unary_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ldiv_expr LDIVIDE unary_expr
  private static boolean ldiv_expr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ldiv_expr_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = ldiv_expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LDIVIDE);
    result_ = result_ && unary_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ldiv_expr IDIVIDE unary_expr
  private static boolean ldiv_expr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ldiv_expr_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = ldiv_expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, IDIVIDE);
    result_ = result_ && unary_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // MODELBLOCK                LBRACE vardecl_or_statement*      RBRACE
  public static boolean model_block(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "model_block")) return false;
    if (!nextTokenIs(builder_, MODELBLOCK)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, MODELBLOCK, LBRACE);
    result_ = result_ && model_block_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, MODEL_BLOCK, result_);
    return result_;
  }

  // vardecl_or_statement*
  private static boolean model_block_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "model_block_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!vardecl_or_statement(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "model_block_2", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // mul_expr TIMES     ldiv_expr
  //            | mul_expr ELTTIMES  ldiv_expr
  //            | mul_expr DIVIDE    ldiv_expr
  //            | mul_expr ELTDIVIDE ldiv_expr
  //            | mul_expr MODULO    ldiv_expr
  //            | ldiv_expr
  public static boolean mul_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mul_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MUL_EXPR, "<mul expr>");
    result_ = mul_expr_0(builder_, level_ + 1);
    if (!result_) result_ = mul_expr_1(builder_, level_ + 1);
    if (!result_) result_ = mul_expr_2(builder_, level_ + 1);
    if (!result_) result_ = mul_expr_3(builder_, level_ + 1);
    if (!result_) result_ = mul_expr_4(builder_, level_ + 1);
    if (!result_) result_ = ldiv_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // mul_expr TIMES     ldiv_expr
  private static boolean mul_expr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mul_expr_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = mul_expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, TIMES);
    result_ = result_ && ldiv_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // mul_expr ELTTIMES  ldiv_expr
  private static boolean mul_expr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mul_expr_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = mul_expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ELTTIMES);
    result_ = result_ && ldiv_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // mul_expr DIVIDE    ldiv_expr
  private static boolean mul_expr_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mul_expr_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = mul_expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, DIVIDE);
    result_ = result_ && ldiv_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // mul_expr ELTDIVIDE ldiv_expr
  private static boolean mul_expr_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mul_expr_3")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = mul_expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ELTDIVIDE);
    result_ = result_ && ldiv_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // mul_expr MODULO    ldiv_expr
  private static boolean mul_expr_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mul_expr_4")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = mul_expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, MODULO);
    result_ = result_ && ldiv_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // if_else_stmt
  //   | if_stmt
  //   | while_stmt
  //   | for_range_stmt
  //   | for_each_stmt
  //   | profile_stmt
  //   | block_stmt
  public static boolean nested_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "nested_statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, NESTED_STATEMENT, "<nested statement>");
    result_ = if_else_stmt(builder_, level_ + 1);
    if (!result_) result_ = if_stmt(builder_, level_ + 1);
    if (!result_) result_ = while_stmt(builder_, level_ + 1);
    if (!result_) result_ = for_range_stmt(builder_, level_ + 1);
    if (!result_) result_ = for_each_stmt(builder_, level_ + 1);
    if (!result_) result_ = profile_stmt(builder_, level_ + 1);
    if (!result_) result_ = block_stmt(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // decl_identifier
  public static boolean no_assign_var(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "no_assign_var")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, NO_ASSIGN_VAR, "<no assign var>");
    result_ = decl_identifier(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // decl_identifier_after_comma
  public static boolean no_assign_var_extra(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "no_assign_var_extra")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, NO_ASSIGN_VAR_EXTRA, "<no assign var extra>");
    result_ = decl_identifier_after_comma(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // OFFSET     ASSIGN constr_expression COMMA MULTIPLIER ASSIGN constr_expression
  //               | MULTIPLIER ASSIGN constr_expression COMMA OFFSET     ASSIGN constr_expression
  //               | OFFSET     ASSIGN constr_expression
  //               | MULTIPLIER ASSIGN constr_expression
  public static boolean offset_mult(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "offset_mult")) return false;
    if (!nextTokenIs(builder_, "<offset mult>", MULTIPLIER, OFFSET)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, OFFSET_MULT, "<offset mult>");
    result_ = offset_mult_0(builder_, level_ + 1);
    if (!result_) result_ = offset_mult_1(builder_, level_ + 1);
    if (!result_) result_ = offset_mult_2(builder_, level_ + 1);
    if (!result_) result_ = offset_mult_3(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // OFFSET     ASSIGN constr_expression COMMA MULTIPLIER ASSIGN constr_expression
  private static boolean offset_mult_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "offset_mult_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, OFFSET, ASSIGN);
    result_ = result_ && constr_expression(builder_, level_ + 1);
    result_ = result_ && consumeTokens(builder_, 0, COMMA, MULTIPLIER, ASSIGN);
    result_ = result_ && constr_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // MULTIPLIER ASSIGN constr_expression COMMA OFFSET     ASSIGN constr_expression
  private static boolean offset_mult_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "offset_mult_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, MULTIPLIER, ASSIGN);
    result_ = result_ && constr_expression(builder_, level_ + 1);
    result_ = result_ && consumeTokens(builder_, 0, COMMA, OFFSET, ASSIGN);
    result_ = result_ && constr_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // OFFSET     ASSIGN constr_expression
  private static boolean offset_mult_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "offset_mult_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, OFFSET, ASSIGN);
    result_ = result_ && constr_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // MULTIPLIER ASSIGN constr_expression
  private static boolean offset_mult_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "offset_mult_3")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, MULTIPLIER, ASSIGN);
    result_ = result_ && constr_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // or_expr OR and_expr
  //           | and_expr
  public static boolean or_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "or_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, OR_EXPR, "<or expr>");
    result_ = or_expr_0(builder_, level_ + 1);
    if (!result_) result_ = and_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // or_expr OR and_expr
  private static boolean or_expr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "or_expr_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = or_expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, OR);
    result_ = result_ && and_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // PARAMETERSBLOCK           LBRACE top_var_decl_no_assign*    RBRACE
  public static boolean parameters_block(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameters_block")) return false;
    if (!nextTokenIs(builder_, PARAMETERSBLOCK)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, PARAMETERSBLOCK, LBRACE);
    result_ = result_ && parameters_block_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, PARAMETERS_BLOCK, result_);
    return result_;
  }

  // top_var_decl_no_assign*
  private static boolean parameters_block_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameters_block_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!top_var_decl_no_assign(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "parameters_block_2", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // LPAREN expression RPAREN
  public static boolean paren_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "paren_expr")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, PAREN_EXPR, result_);
    return result_;
  }

  /* ********************************************************** */
  // postfix_expr TRANSPOSE
  //                | postfix_expr DOTNUMERAL
  //                | index_expr
  public static boolean postfix_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "postfix_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, POSTFIX_EXPR, "<postfix expr>");
    result_ = postfix_expr_0(builder_, level_ + 1);
    if (!result_) result_ = postfix_expr_1(builder_, level_ + 1);
    if (!result_) result_ = index_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // postfix_expr TRANSPOSE
  private static boolean postfix_expr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "postfix_expr_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = postfix_expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, TRANSPOSE);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // postfix_expr DOTNUMERAL
  private static boolean postfix_expr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "postfix_expr_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = postfix_expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, DOTNUMERAL);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // postfix_expr (HAT | ELTPOW) pow_expr
  //            | postfix_expr
  public static boolean pow_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pow_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, POW_EXPR, "<pow expr>");
    result_ = pow_expr_0(builder_, level_ + 1);
    if (!result_) result_ = postfix_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // postfix_expr (HAT | ELTPOW) pow_expr
  private static boolean pow_expr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pow_expr_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = postfix_expr(builder_, level_ + 1);
    result_ = result_ && pow_expr_0_1(builder_, level_ + 1);
    result_ = result_ && pow_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // HAT | ELTPOW
  private static boolean pow_expr_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pow_expr_0_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, HAT);
    if (!result_) result_ = consumeToken(builder_, ELTPOW);
    return result_;
  }

  /* ********************************************************** */
  // paren_expr
  //   | tuple_expr
  //   | array_expr
  //   | row_vector_expr
  //   | empty_row_vector_expr
  //   | target_call_expr
  //   | cond_dist_expr
  //   | fun_call_expr
  //   | int_literal_expr
  //   | real_literal_expr
  //   | imag_literal_expr
  //   | DOTNUMERAL
  //   | variable_expr
  public static boolean primary_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "primary_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PRIMARY_EXPR, "<primary expr>");
    result_ = paren_expr(builder_, level_ + 1);
    if (!result_) result_ = tuple_expr(builder_, level_ + 1);
    if (!result_) result_ = array_expr(builder_, level_ + 1);
    if (!result_) result_ = row_vector_expr(builder_, level_ + 1);
    if (!result_) result_ = empty_row_vector_expr(builder_, level_ + 1);
    if (!result_) result_ = target_call_expr(builder_, level_ + 1);
    if (!result_) result_ = cond_dist_expr(builder_, level_ + 1);
    if (!result_) result_ = fun_call_expr(builder_, level_ + 1);
    if (!result_) result_ = int_literal_expr(builder_, level_ + 1);
    if (!result_) result_ = real_literal_expr(builder_, level_ + 1);
    if (!result_) result_ = imag_literal_expr(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, DOTNUMERAL);
    if (!result_) result_ = variable_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // PRINT      LPAREN printables RPAREN SEMICOLON
  public static boolean print_stmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "print_stmt")) return false;
    if (!nextTokenIs(builder_, PRINT)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, PRINT, LPAREN);
    result_ = result_ && printables(builder_, level_ + 1);
    result_ = result_ && consumeTokens(builder_, 0, RPAREN, SEMICOLON);
    exit_section_(builder_, marker_, PRINT_STMT, result_);
    return result_;
  }

  /* ********************************************************** */
  // expression | string_literal
  public static boolean printable(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "printable")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PRINTABLE, "<printable>");
    result_ = expression(builder_, level_ + 1);
    if (!result_) result_ = string_literal(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // printable (COMMA printable)*
  public static boolean printables(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "printables")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PRINTABLES, "<printables>");
    result_ = printable(builder_, level_ + 1);
    result_ = result_ && printables_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (COMMA printable)*
  private static boolean printables_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "printables_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!printables_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "printables_1", pos_)) break;
    }
    return true;
  }

  // COMMA printable
  private static boolean printables_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "printables_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && printable(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // PROFILE LPAREN string_literal RPAREN LBRACE vardecl_or_statement* RBRACE
  public static boolean profile_stmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "profile_stmt")) return false;
    if (!nextTokenIs(builder_, PROFILE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, PROFILE, LPAREN);
    result_ = result_ && string_literal(builder_, level_ + 1);
    result_ = result_ && consumeTokens(builder_, 0, RPAREN, LBRACE);
    result_ = result_ && profile_stmt_5(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, PROFILE_STMT, result_);
    return result_;
  }

  // vardecl_or_statement*
  private static boolean profile_stmt_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "profile_stmt_5")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!vardecl_or_statement(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "profile_stmt_5", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // function_block?
  //             data_block?
  //             transformed_data_block?
  //             parameters_block?
  //             transformed_parameters_block?
  //             model_block?
  //             generated_quantities_block?
  static boolean program(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "program")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = program_0(builder_, level_ + 1);
    result_ = result_ && program_1(builder_, level_ + 1);
    result_ = result_ && program_2(builder_, level_ + 1);
    result_ = result_ && program_3(builder_, level_ + 1);
    result_ = result_ && program_4(builder_, level_ + 1);
    result_ = result_ && program_5(builder_, level_ + 1);
    result_ = result_ && program_6(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // function_block?
  private static boolean program_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "program_0")) return false;
    function_block(builder_, level_ + 1);
    return true;
  }

  // data_block?
  private static boolean program_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "program_1")) return false;
    data_block(builder_, level_ + 1);
    return true;
  }

  // transformed_data_block?
  private static boolean program_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "program_2")) return false;
    transformed_data_block(builder_, level_ + 1);
    return true;
  }

  // parameters_block?
  private static boolean program_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "program_3")) return false;
    parameters_block(builder_, level_ + 1);
    return true;
  }

  // transformed_parameters_block?
  private static boolean program_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "program_4")) return false;
    transformed_parameters_block(builder_, level_ + 1);
    return true;
  }

  // model_block?
  private static boolean program_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "program_5")) return false;
    model_block(builder_, level_ + 1);
    return true;
  }

  // generated_quantities_block?
  private static boolean program_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "program_6")) return false;
    generated_quantities_block(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // LOWER  ASSIGN constr_expression COMMA UPPER ASSIGN constr_expression
  //         | UPPER  ASSIGN constr_expression COMMA LOWER ASSIGN constr_expression
  //         | LOWER  ASSIGN constr_expression
  //         | UPPER  ASSIGN constr_expression
  public static boolean range(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "range")) return false;
    if (!nextTokenIs(builder_, "<range>", LOWER, UPPER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, RANGE, "<range>");
    result_ = range_0(builder_, level_ + 1);
    if (!result_) result_ = range_1(builder_, level_ + 1);
    if (!result_) result_ = range_2(builder_, level_ + 1);
    if (!result_) result_ = range_3(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // LOWER  ASSIGN constr_expression COMMA UPPER ASSIGN constr_expression
  private static boolean range_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "range_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, LOWER, ASSIGN);
    result_ = result_ && constr_expression(builder_, level_ + 1);
    result_ = result_ && consumeTokens(builder_, 0, COMMA, UPPER, ASSIGN);
    result_ = result_ && constr_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // UPPER  ASSIGN constr_expression COMMA LOWER ASSIGN constr_expression
  private static boolean range_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "range_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, UPPER, ASSIGN);
    result_ = result_ && constr_expression(builder_, level_ + 1);
    result_ = result_ && consumeTokens(builder_, 0, COMMA, LOWER, ASSIGN);
    result_ = result_ && constr_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // LOWER  ASSIGN constr_expression
  private static boolean range_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "range_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, LOWER, ASSIGN);
    result_ = result_ && constr_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // UPPER  ASSIGN constr_expression
  private static boolean range_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "range_3")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, UPPER, ASSIGN);
    result_ = result_ && constr_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // REALNUMERAL
  public static boolean real_literal_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "real_literal_expr")) return false;
    if (!nextTokenIs(builder_, REALNUMERAL)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, REALNUMERAL);
    exit_section_(builder_, marker_, REAL_LITERAL_EXPR, result_);
    return result_;
  }

  /* ********************************************************** */
  // REJECT     LPAREN printables RPAREN SEMICOLON
  public static boolean reject_stmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "reject_stmt")) return false;
    if (!nextTokenIs(builder_, REJECT)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, REJECT, LPAREN);
    result_ = result_ && printables(builder_, level_ + 1);
    result_ = result_ && consumeTokens(builder_, 0, RPAREN, SEMICOLON);
    exit_section_(builder_, marker_, REJECT_STMT, result_);
    return result_;
  }

  /* ********************************************************** */
  // rel_expr LABRACK  add_expr
  //            | rel_expr LEQ      add_expr
  //            | rel_expr RABRACK  add_expr
  //            | rel_expr GEQ      add_expr
  //            | add_expr
  public static boolean rel_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rel_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, REL_EXPR, "<rel expr>");
    result_ = rel_expr_0(builder_, level_ + 1);
    if (!result_) result_ = rel_expr_1(builder_, level_ + 1);
    if (!result_) result_ = rel_expr_2(builder_, level_ + 1);
    if (!result_) result_ = rel_expr_3(builder_, level_ + 1);
    if (!result_) result_ = add_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // rel_expr LABRACK  add_expr
  private static boolean rel_expr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rel_expr_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = rel_expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LABRACK);
    result_ = result_ && add_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // rel_expr LEQ      add_expr
  private static boolean rel_expr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rel_expr_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = rel_expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LEQ);
    result_ = result_ && add_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // rel_expr RABRACK  add_expr
  private static boolean rel_expr_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rel_expr_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = rel_expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RABRACK);
    result_ = result_ && add_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // rel_expr GEQ      add_expr
  private static boolean rel_expr_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rel_expr_3")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = rel_expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, GEQ);
    result_ = result_ && add_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // FUNCTIONBLOCK | DATABLOCK | PARAMETERSBLOCK | MODELBLOCK
  //   | RETURN | IF | ELSE | WHILE | FOR | IN | BREAK | CONTINUE | PROFILE
  //   | VOID | INT | REAL | COMPLEX | VECTOR | ROWVECTOR | MATRIX
  //   | COMPLEXVECTOR | COMPLEXROWVECTOR | COMPLEXMATRIX
  //   | ORDERED | POSITIVEORDERED | SIMPLEX | UNITVECTOR
  //   | SUMTOZEROVEC | SUMTOZEROMAT | CHOLESKYFACTORCORR | CHOLESKYFACTORCOV
  //   | CORRMATRIX | COVMATRIX | STOCHASTICCOLUMNMATRIX | STOCHASTICROWMATRIX
  //   | PRINT | REJECT | FATAL_ERROR | TARGET | JACOBIAN
  //   | TUPLE | OFFSET | MULTIPLIER | LOWER | UPPER | ARRAY
  public static boolean reserved_word(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "reserved_word")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, RESERVED_WORD, "<reserved word>");
    result_ = consumeToken(builder_, FUNCTIONBLOCK);
    if (!result_) result_ = consumeToken(builder_, DATABLOCK);
    if (!result_) result_ = consumeToken(builder_, PARAMETERSBLOCK);
    if (!result_) result_ = consumeToken(builder_, MODELBLOCK);
    if (!result_) result_ = consumeToken(builder_, RETURN);
    if (!result_) result_ = consumeToken(builder_, IF);
    if (!result_) result_ = consumeToken(builder_, ELSE);
    if (!result_) result_ = consumeToken(builder_, WHILE);
    if (!result_) result_ = consumeToken(builder_, FOR);
    if (!result_) result_ = consumeToken(builder_, IN);
    if (!result_) result_ = consumeToken(builder_, BREAK);
    if (!result_) result_ = consumeToken(builder_, CONTINUE);
    if (!result_) result_ = consumeToken(builder_, PROFILE);
    if (!result_) result_ = consumeToken(builder_, VOID);
    if (!result_) result_ = consumeToken(builder_, INT);
    if (!result_) result_ = consumeToken(builder_, REAL);
    if (!result_) result_ = consumeToken(builder_, COMPLEX);
    if (!result_) result_ = consumeToken(builder_, VECTOR);
    if (!result_) result_ = consumeToken(builder_, ROWVECTOR);
    if (!result_) result_ = consumeToken(builder_, MATRIX);
    if (!result_) result_ = consumeToken(builder_, COMPLEXVECTOR);
    if (!result_) result_ = consumeToken(builder_, COMPLEXROWVECTOR);
    if (!result_) result_ = consumeToken(builder_, COMPLEXMATRIX);
    if (!result_) result_ = consumeToken(builder_, ORDERED);
    if (!result_) result_ = consumeToken(builder_, POSITIVEORDERED);
    if (!result_) result_ = consumeToken(builder_, SIMPLEX);
    if (!result_) result_ = consumeToken(builder_, UNITVECTOR);
    if (!result_) result_ = consumeToken(builder_, SUMTOZEROVEC);
    if (!result_) result_ = consumeToken(builder_, SUMTOZEROMAT);
    if (!result_) result_ = consumeToken(builder_, CHOLESKYFACTORCORR);
    if (!result_) result_ = consumeToken(builder_, CHOLESKYFACTORCOV);
    if (!result_) result_ = consumeToken(builder_, CORRMATRIX);
    if (!result_) result_ = consumeToken(builder_, COVMATRIX);
    if (!result_) result_ = consumeToken(builder_, STOCHASTICCOLUMNMATRIX);
    if (!result_) result_ = consumeToken(builder_, STOCHASTICROWMATRIX);
    if (!result_) result_ = consumeToken(builder_, PRINT);
    if (!result_) result_ = consumeToken(builder_, REJECT);
    if (!result_) result_ = consumeToken(builder_, FATAL_ERROR);
    if (!result_) result_ = consumeToken(builder_, TARGET);
    if (!result_) result_ = consumeToken(builder_, JACOBIAN);
    if (!result_) result_ = consumeToken(builder_, TUPLE);
    if (!result_) result_ = consumeToken(builder_, OFFSET);
    if (!result_) result_ = consumeToken(builder_, MULTIPLIER);
    if (!result_) result_ = consumeToken(builder_, LOWER);
    if (!result_) result_ = consumeToken(builder_, UPPER);
    if (!result_) result_ = consumeToken(builder_, ARRAY);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // RETURN expression? SEMICOLON
  public static boolean return_stmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "return_stmt")) return false;
    if (!nextTokenIs(builder_, RETURN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, RETURN);
    result_ = result_ && return_stmt_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, RETURN_STMT, result_);
    return result_;
  }

  // expression?
  private static boolean return_stmt_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "return_stmt_1")) return false;
    expression(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // VOID | unsized_type
  public static boolean return_type(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "return_type")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, RETURN_TYPE, "<return type>");
    result_ = consumeToken(builder_, VOID);
    if (!result_) result_ = unsized_type(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // LBRACK expression (COMMA expression)* RBRACK
  public static boolean row_vector_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "row_vector_expr")) return false;
    if (!nextTokenIs(builder_, LBRACK)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && row_vector_expr_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, ROW_VECTOR_EXPR, result_);
    return result_;
  }

  // (COMMA expression)*
  private static boolean row_vector_expr_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "row_vector_expr_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!row_vector_expr_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "row_vector_expr_2", pos_)) break;
    }
    return true;
  }

  // COMMA expression
  private static boolean row_vector_expr_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "row_vector_expr_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // INT
  //   | REAL
  //   | COMPLEX
  //   | VECTOR           LBRACK expression RBRACK
  //   | ROWVECTOR        LBRACK expression RBRACK
  //   | MATRIX           LBRACK expression COMMA expression RBRACK
  //   | COMPLEXVECTOR    LBRACK expression RBRACK
  //   | COMPLEXROWVECTOR LBRACK expression RBRACK
  //   | COMPLEXMATRIX    LBRACK expression COMMA expression RBRACK
  public static boolean sized_basic_type(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "sized_basic_type")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SIZED_BASIC_TYPE, "<sized basic type>");
    result_ = consumeToken(builder_, INT);
    if (!result_) result_ = consumeToken(builder_, REAL);
    if (!result_) result_ = consumeToken(builder_, COMPLEX);
    if (!result_) result_ = sized_basic_type_3(builder_, level_ + 1);
    if (!result_) result_ = sized_basic_type_4(builder_, level_ + 1);
    if (!result_) result_ = sized_basic_type_5(builder_, level_ + 1);
    if (!result_) result_ = sized_basic_type_6(builder_, level_ + 1);
    if (!result_) result_ = sized_basic_type_7(builder_, level_ + 1);
    if (!result_) result_ = sized_basic_type_8(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // VECTOR           LBRACK expression RBRACK
  private static boolean sized_basic_type_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "sized_basic_type_3")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, VECTOR, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ROWVECTOR        LBRACK expression RBRACK
  private static boolean sized_basic_type_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "sized_basic_type_4")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, ROWVECTOR, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // MATRIX           LBRACK expression COMMA expression RBRACK
  private static boolean sized_basic_type_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "sized_basic_type_5")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, MATRIX, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COMMA);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // COMPLEXVECTOR    LBRACK expression RBRACK
  private static boolean sized_basic_type_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "sized_basic_type_6")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, COMPLEXVECTOR, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // COMPLEXROWVECTOR LBRACK expression RBRACK
  private static boolean sized_basic_type_7(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "sized_basic_type_7")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, COMPLEXROWVECTOR, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // COMPLEXMATRIX    LBRACK expression COMMA expression RBRACK
  private static boolean sized_basic_type_8(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "sized_basic_type_8")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, COMPLEXMATRIX, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COMMA);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // atomic_statement | nested_statement
  public static boolean statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STATEMENT, "<statement>");
    result_ = atomic_statement(builder_, level_ + 1);
    if (!result_) result_ = nested_statement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // STRINGLITERAL
  public static boolean string_literal(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "string_literal")) return false;
    if (!nextTokenIs(builder_, STRINGLITERAL)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, STRINGLITERAL);
    exit_section_(builder_, marker_, STRING_LITERAL, result_);
    return result_;
  }

  /* ********************************************************** */
  // TARGET LPAREN RPAREN
  public static boolean target_call_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "target_call_expr")) return false;
    if (!nextTokenIs(builder_, TARGET)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, TARGET, LPAREN, RPAREN);
    exit_section_(builder_, marker_, TARGET_CALL_EXPR, result_);
    return result_;
  }

  /* ********************************************************** */
  // TARGET   PLUSASSIGN expression SEMICOLON
  public static boolean target_plus_assign_stmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "target_plus_assign_stmt")) return false;
    if (!nextTokenIs(builder_, TARGET)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, TARGET, PLUSASSIGN);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, TARGET_PLUS_ASSIGN_STMT, result_);
    return result_;
  }

  /* ********************************************************** */
  // expression TILDE ident LPAREN (expression (COMMA expression)*)? RPAREN truncation? SEMICOLON
  public static boolean tilde_stmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "tilde_stmt")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TILDE_STMT, "<tilde stmt>");
    result_ = expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, TILDE);
    result_ = result_ && ident(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LPAREN);
    result_ = result_ && tilde_stmt_4(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    result_ = result_ && tilde_stmt_6(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (expression (COMMA expression)*)?
  private static boolean tilde_stmt_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "tilde_stmt_4")) return false;
    tilde_stmt_4_0(builder_, level_ + 1);
    return true;
  }

  // expression (COMMA expression)*
  private static boolean tilde_stmt_4_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "tilde_stmt_4_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = expression(builder_, level_ + 1);
    result_ = result_ && tilde_stmt_4_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA expression)*
  private static boolean tilde_stmt_4_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "tilde_stmt_4_0_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!tilde_stmt_4_0_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "tilde_stmt_4_0_1", pos_)) break;
    }
    return true;
  }

  // COMMA expression
  private static boolean tilde_stmt_4_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "tilde_stmt_4_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // truncation?
  private static boolean tilde_stmt_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "tilde_stmt_6")) return false;
    truncation(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // decl_identifier (ASSIGN expression)?
  public static boolean top_declared_var(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_declared_var")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TOP_DECLARED_VAR, "<top declared var>");
    result_ = decl_identifier(builder_, level_ + 1);
    result_ = result_ && top_declared_var_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (ASSIGN expression)?
  private static boolean top_declared_var_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_declared_var_1")) return false;
    top_declared_var_1_0(builder_, level_ + 1);
    return true;
  }

  // ASSIGN expression
  private static boolean top_declared_var_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_declared_var_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ASSIGN);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // decl_identifier_after_comma (ASSIGN expression)?
  public static boolean top_declared_var_extra(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_declared_var_extra")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TOP_DECLARED_VAR_EXTRA, "<top declared var extra>");
    result_ = decl_identifier_after_comma(builder_, level_ + 1);
    result_ = result_ && top_declared_var_extra_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (ASSIGN expression)?
  private static boolean top_declared_var_extra_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_declared_var_extra_1")) return false;
    top_declared_var_extra_1_0(builder_, level_ + 1);
    return true;
  }

  // ASSIGN expression
  private static boolean top_declared_var_extra_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_declared_var_extra_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ASSIGN);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // arr_dims top_var_type
  //                   | arr_dims top_tuple_type
  //                   | top_var_type
  //                   | top_tuple_type
  public static boolean top_higher_type(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_higher_type")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TOP_HIGHER_TYPE, "<top higher type>");
    result_ = top_higher_type_0(builder_, level_ + 1);
    if (!result_) result_ = top_higher_type_1(builder_, level_ + 1);
    if (!result_) result_ = top_var_type(builder_, level_ + 1);
    if (!result_) result_ = top_tuple_type(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // arr_dims top_var_type
  private static boolean top_higher_type_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_higher_type_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = arr_dims(builder_, level_ + 1);
    result_ = result_ && top_var_type(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // arr_dims top_tuple_type
  private static boolean top_higher_type_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_higher_type_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = arr_dims(builder_, level_ + 1);
    result_ = result_ && top_tuple_type(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // TUPLE LPAREN top_higher_type COMMA top_higher_type (COMMA top_higher_type)* RPAREN
  public static boolean top_tuple_type(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_tuple_type")) return false;
    if (!nextTokenIs(builder_, TUPLE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, TUPLE, LPAREN);
    result_ = result_ && top_higher_type(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COMMA);
    result_ = result_ && top_higher_type(builder_, level_ + 1);
    result_ = result_ && top_tuple_type_5(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, TOP_TUPLE_TYPE, result_);
    return result_;
  }

  // (COMMA top_higher_type)*
  private static boolean top_tuple_type_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_tuple_type_5")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!top_tuple_type_5_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "top_tuple_type_5", pos_)) break;
    }
    return true;
  }

  // COMMA top_higher_type
  private static boolean top_tuple_type_5_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_tuple_type_5_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && top_higher_type(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // top_var_type top_declared_var (COMMA top_declared_var_extra)* SEMICOLON
  //                | arr_dims top_var_type top_declared_var (COMMA top_declared_var_extra)* SEMICOLON
  //                | arr_dims top_tuple_type top_declared_var (COMMA top_declared_var_extra)* SEMICOLON
  //                | top_tuple_type top_declared_var (COMMA top_declared_var_extra)* SEMICOLON
  public static boolean top_var_decl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TOP_VAR_DECL, "<top var decl>");
    result_ = top_var_decl_0(builder_, level_ + 1);
    if (!result_) result_ = top_var_decl_1(builder_, level_ + 1);
    if (!result_) result_ = top_var_decl_2(builder_, level_ + 1);
    if (!result_) result_ = top_var_decl_3(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // top_var_type top_declared_var (COMMA top_declared_var_extra)* SEMICOLON
  private static boolean top_var_decl_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = top_var_type(builder_, level_ + 1);
    result_ = result_ && top_declared_var(builder_, level_ + 1);
    result_ = result_ && top_var_decl_0_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA top_declared_var_extra)*
  private static boolean top_var_decl_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_0_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!top_var_decl_0_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "top_var_decl_0_2", pos_)) break;
    }
    return true;
  }

  // COMMA top_declared_var_extra
  private static boolean top_var_decl_0_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_0_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && top_declared_var_extra(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // arr_dims top_var_type top_declared_var (COMMA top_declared_var_extra)* SEMICOLON
  private static boolean top_var_decl_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = arr_dims(builder_, level_ + 1);
    result_ = result_ && top_var_type(builder_, level_ + 1);
    result_ = result_ && top_declared_var(builder_, level_ + 1);
    result_ = result_ && top_var_decl_1_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA top_declared_var_extra)*
  private static boolean top_var_decl_1_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_1_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!top_var_decl_1_3_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "top_var_decl_1_3", pos_)) break;
    }
    return true;
  }

  // COMMA top_declared_var_extra
  private static boolean top_var_decl_1_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_1_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && top_declared_var_extra(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // arr_dims top_tuple_type top_declared_var (COMMA top_declared_var_extra)* SEMICOLON
  private static boolean top_var_decl_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = arr_dims(builder_, level_ + 1);
    result_ = result_ && top_tuple_type(builder_, level_ + 1);
    result_ = result_ && top_declared_var(builder_, level_ + 1);
    result_ = result_ && top_var_decl_2_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA top_declared_var_extra)*
  private static boolean top_var_decl_2_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_2_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!top_var_decl_2_3_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "top_var_decl_2_3", pos_)) break;
    }
    return true;
  }

  // COMMA top_declared_var_extra
  private static boolean top_var_decl_2_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_2_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && top_declared_var_extra(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // top_tuple_type top_declared_var (COMMA top_declared_var_extra)* SEMICOLON
  private static boolean top_var_decl_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_3")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = top_tuple_type(builder_, level_ + 1);
    result_ = result_ && top_declared_var(builder_, level_ + 1);
    result_ = result_ && top_var_decl_3_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA top_declared_var_extra)*
  private static boolean top_var_decl_3_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_3_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!top_var_decl_3_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "top_var_decl_3_2", pos_)) break;
    }
    return true;
  }

  // COMMA top_declared_var_extra
  private static boolean top_var_decl_3_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_3_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && top_declared_var_extra(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // top_var_type no_assign_var (COMMA no_assign_var_extra)* SEMICOLON
  //                           | arr_dims top_var_type no_assign_var (COMMA no_assign_var_extra)* SEMICOLON
  //                           | arr_dims top_tuple_type no_assign_var (COMMA no_assign_var_extra)* SEMICOLON
  //                           | top_tuple_type no_assign_var (COMMA no_assign_var_extra)* SEMICOLON
  //                           | SEMICOLON
  public static boolean top_var_decl_no_assign(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_no_assign")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TOP_VAR_DECL_NO_ASSIGN, "<top var decl no assign>");
    result_ = top_var_decl_no_assign_0(builder_, level_ + 1);
    if (!result_) result_ = top_var_decl_no_assign_1(builder_, level_ + 1);
    if (!result_) result_ = top_var_decl_no_assign_2(builder_, level_ + 1);
    if (!result_) result_ = top_var_decl_no_assign_3(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // top_var_type no_assign_var (COMMA no_assign_var_extra)* SEMICOLON
  private static boolean top_var_decl_no_assign_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_no_assign_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = top_var_type(builder_, level_ + 1);
    result_ = result_ && no_assign_var(builder_, level_ + 1);
    result_ = result_ && top_var_decl_no_assign_0_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA no_assign_var_extra)*
  private static boolean top_var_decl_no_assign_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_no_assign_0_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!top_var_decl_no_assign_0_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "top_var_decl_no_assign_0_2", pos_)) break;
    }
    return true;
  }

  // COMMA no_assign_var_extra
  private static boolean top_var_decl_no_assign_0_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_no_assign_0_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && no_assign_var_extra(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // arr_dims top_var_type no_assign_var (COMMA no_assign_var_extra)* SEMICOLON
  private static boolean top_var_decl_no_assign_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_no_assign_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = arr_dims(builder_, level_ + 1);
    result_ = result_ && top_var_type(builder_, level_ + 1);
    result_ = result_ && no_assign_var(builder_, level_ + 1);
    result_ = result_ && top_var_decl_no_assign_1_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA no_assign_var_extra)*
  private static boolean top_var_decl_no_assign_1_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_no_assign_1_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!top_var_decl_no_assign_1_3_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "top_var_decl_no_assign_1_3", pos_)) break;
    }
    return true;
  }

  // COMMA no_assign_var_extra
  private static boolean top_var_decl_no_assign_1_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_no_assign_1_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && no_assign_var_extra(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // arr_dims top_tuple_type no_assign_var (COMMA no_assign_var_extra)* SEMICOLON
  private static boolean top_var_decl_no_assign_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_no_assign_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = arr_dims(builder_, level_ + 1);
    result_ = result_ && top_tuple_type(builder_, level_ + 1);
    result_ = result_ && no_assign_var(builder_, level_ + 1);
    result_ = result_ && top_var_decl_no_assign_2_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA no_assign_var_extra)*
  private static boolean top_var_decl_no_assign_2_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_no_assign_2_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!top_var_decl_no_assign_2_3_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "top_var_decl_no_assign_2_3", pos_)) break;
    }
    return true;
  }

  // COMMA no_assign_var_extra
  private static boolean top_var_decl_no_assign_2_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_no_assign_2_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && no_assign_var_extra(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // top_tuple_type no_assign_var (COMMA no_assign_var_extra)* SEMICOLON
  private static boolean top_var_decl_no_assign_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_no_assign_3")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = top_tuple_type(builder_, level_ + 1);
    result_ = result_ && no_assign_var(builder_, level_ + 1);
    result_ = result_ && top_var_decl_no_assign_3_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA no_assign_var_extra)*
  private static boolean top_var_decl_no_assign_3_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_no_assign_3_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!top_var_decl_no_assign_3_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "top_var_decl_no_assign_3_2", pos_)) break;
    }
    return true;
  }

  // COMMA no_assign_var_extra
  private static boolean top_var_decl_no_assign_3_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_decl_no_assign_3_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && no_assign_var_extra(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // INT                   (LABRACK range RABRACK)?
  //   | REAL                  type_constraint?
  //   | COMPLEX               type_constraint?
  //   | VECTOR                type_constraint? LBRACK expression RBRACK
  //   | ROWVECTOR             type_constraint? LBRACK expression RBRACK
  //   | MATRIX                type_constraint? LBRACK expression COMMA expression RBRACK
  //   | COMPLEXVECTOR         type_constraint? LBRACK expression RBRACK
  //   | COMPLEXROWVECTOR      type_constraint? LBRACK expression RBRACK
  //   | COMPLEXMATRIX         type_constraint? LBRACK expression COMMA expression RBRACK
  //   | ORDERED               LBRACK expression RBRACK
  //   | POSITIVEORDERED       LBRACK expression RBRACK
  //   | SIMPLEX               LBRACK expression RBRACK
  //   | UNITVECTOR            LBRACK expression RBRACK
  //   | SUMTOZEROVEC          LBRACK expression RBRACK
  //   | CHOLESKYFACTORCORR    LBRACK expression RBRACK
  //   | CHOLESKYFACTORCOV     LBRACK expression (COMMA expression)? RBRACK
  //   | CORRMATRIX            LBRACK expression RBRACK
  //   | COVMATRIX             LBRACK expression RBRACK
  //   | SUMTOZEROMAT          LBRACK expression COMMA expression RBRACK
  //   | STOCHASTICCOLUMNMATRIX LBRACK expression COMMA expression RBRACK
  //   | STOCHASTICROWMATRIX   LBRACK expression COMMA expression RBRACK
  public static boolean top_var_type(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TOP_VAR_TYPE, "<top var type>");
    result_ = top_var_type_0(builder_, level_ + 1);
    if (!result_) result_ = top_var_type_1(builder_, level_ + 1);
    if (!result_) result_ = top_var_type_2(builder_, level_ + 1);
    if (!result_) result_ = top_var_type_3(builder_, level_ + 1);
    if (!result_) result_ = top_var_type_4(builder_, level_ + 1);
    if (!result_) result_ = top_var_type_5(builder_, level_ + 1);
    if (!result_) result_ = top_var_type_6(builder_, level_ + 1);
    if (!result_) result_ = top_var_type_7(builder_, level_ + 1);
    if (!result_) result_ = top_var_type_8(builder_, level_ + 1);
    if (!result_) result_ = top_var_type_9(builder_, level_ + 1);
    if (!result_) result_ = top_var_type_10(builder_, level_ + 1);
    if (!result_) result_ = top_var_type_11(builder_, level_ + 1);
    if (!result_) result_ = top_var_type_12(builder_, level_ + 1);
    if (!result_) result_ = top_var_type_13(builder_, level_ + 1);
    if (!result_) result_ = top_var_type_14(builder_, level_ + 1);
    if (!result_) result_ = top_var_type_15(builder_, level_ + 1);
    if (!result_) result_ = top_var_type_16(builder_, level_ + 1);
    if (!result_) result_ = top_var_type_17(builder_, level_ + 1);
    if (!result_) result_ = top_var_type_18(builder_, level_ + 1);
    if (!result_) result_ = top_var_type_19(builder_, level_ + 1);
    if (!result_) result_ = top_var_type_20(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // INT                   (LABRACK range RABRACK)?
  private static boolean top_var_type_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, INT);
    result_ = result_ && top_var_type_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (LABRACK range RABRACK)?
  private static boolean top_var_type_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_0_1")) return false;
    top_var_type_0_1_0(builder_, level_ + 1);
    return true;
  }

  // LABRACK range RABRACK
  private static boolean top_var_type_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LABRACK);
    result_ = result_ && range(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RABRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // REAL                  type_constraint?
  private static boolean top_var_type_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, REAL);
    result_ = result_ && top_var_type_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // type_constraint?
  private static boolean top_var_type_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_1_1")) return false;
    type_constraint(builder_, level_ + 1);
    return true;
  }

  // COMPLEX               type_constraint?
  private static boolean top_var_type_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMPLEX);
    result_ = result_ && top_var_type_2_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // type_constraint?
  private static boolean top_var_type_2_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_2_1")) return false;
    type_constraint(builder_, level_ + 1);
    return true;
  }

  // VECTOR                type_constraint? LBRACK expression RBRACK
  private static boolean top_var_type_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_3")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, VECTOR);
    result_ = result_ && top_var_type_3_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // type_constraint?
  private static boolean top_var_type_3_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_3_1")) return false;
    type_constraint(builder_, level_ + 1);
    return true;
  }

  // ROWVECTOR             type_constraint? LBRACK expression RBRACK
  private static boolean top_var_type_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_4")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ROWVECTOR);
    result_ = result_ && top_var_type_4_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // type_constraint?
  private static boolean top_var_type_4_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_4_1")) return false;
    type_constraint(builder_, level_ + 1);
    return true;
  }

  // MATRIX                type_constraint? LBRACK expression COMMA expression RBRACK
  private static boolean top_var_type_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_5")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MATRIX);
    result_ = result_ && top_var_type_5_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COMMA);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // type_constraint?
  private static boolean top_var_type_5_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_5_1")) return false;
    type_constraint(builder_, level_ + 1);
    return true;
  }

  // COMPLEXVECTOR         type_constraint? LBRACK expression RBRACK
  private static boolean top_var_type_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_6")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMPLEXVECTOR);
    result_ = result_ && top_var_type_6_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // type_constraint?
  private static boolean top_var_type_6_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_6_1")) return false;
    type_constraint(builder_, level_ + 1);
    return true;
  }

  // COMPLEXROWVECTOR      type_constraint? LBRACK expression RBRACK
  private static boolean top_var_type_7(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_7")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMPLEXROWVECTOR);
    result_ = result_ && top_var_type_7_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // type_constraint?
  private static boolean top_var_type_7_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_7_1")) return false;
    type_constraint(builder_, level_ + 1);
    return true;
  }

  // COMPLEXMATRIX         type_constraint? LBRACK expression COMMA expression RBRACK
  private static boolean top_var_type_8(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_8")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMPLEXMATRIX);
    result_ = result_ && top_var_type_8_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COMMA);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // type_constraint?
  private static boolean top_var_type_8_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_8_1")) return false;
    type_constraint(builder_, level_ + 1);
    return true;
  }

  // ORDERED               LBRACK expression RBRACK
  private static boolean top_var_type_9(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_9")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, ORDERED, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // POSITIVEORDERED       LBRACK expression RBRACK
  private static boolean top_var_type_10(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_10")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, POSITIVEORDERED, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // SIMPLEX               LBRACK expression RBRACK
  private static boolean top_var_type_11(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_11")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, SIMPLEX, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // UNITVECTOR            LBRACK expression RBRACK
  private static boolean top_var_type_12(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_12")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, UNITVECTOR, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // SUMTOZEROVEC          LBRACK expression RBRACK
  private static boolean top_var_type_13(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_13")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, SUMTOZEROVEC, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // CHOLESKYFACTORCORR    LBRACK expression RBRACK
  private static boolean top_var_type_14(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_14")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, CHOLESKYFACTORCORR, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // CHOLESKYFACTORCOV     LBRACK expression (COMMA expression)? RBRACK
  private static boolean top_var_type_15(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_15")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, CHOLESKYFACTORCOV, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && top_var_type_15_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA expression)?
  private static boolean top_var_type_15_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_15_3")) return false;
    top_var_type_15_3_0(builder_, level_ + 1);
    return true;
  }

  // COMMA expression
  private static boolean top_var_type_15_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_15_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // CORRMATRIX            LBRACK expression RBRACK
  private static boolean top_var_type_16(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_16")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, CORRMATRIX, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // COVMATRIX             LBRACK expression RBRACK
  private static boolean top_var_type_17(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_17")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, COVMATRIX, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // SUMTOZEROMAT          LBRACK expression COMMA expression RBRACK
  private static boolean top_var_type_18(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_18")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, SUMTOZEROMAT, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COMMA);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // STOCHASTICCOLUMNMATRIX LBRACK expression COMMA expression RBRACK
  private static boolean top_var_type_19(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_19")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, STOCHASTICCOLUMNMATRIX, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COMMA);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // STOCHASTICROWMATRIX   LBRACK expression COMMA expression RBRACK
  private static boolean top_var_type_20(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_var_type_20")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, STOCHASTICROWMATRIX, LBRACK);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COMMA);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // statement | top_var_decl
  public static boolean top_vardecl_or_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_vardecl_or_statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TOP_VARDECL_OR_STATEMENT, "<top vardecl or statement>");
    result_ = statement(builder_, level_ + 1);
    if (!result_) result_ = top_var_decl(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // TRANSFORMEDDATABLOCK      LBRACE top_vardecl_or_statement*  RBRACE
  public static boolean transformed_data_block(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "transformed_data_block")) return false;
    if (!nextTokenIs(builder_, TRANSFORMEDDATABLOCK)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, TRANSFORMEDDATABLOCK, LBRACE);
    result_ = result_ && transformed_data_block_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, TRANSFORMED_DATA_BLOCK, result_);
    return result_;
  }

  // top_vardecl_or_statement*
  private static boolean transformed_data_block_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "transformed_data_block_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!top_vardecl_or_statement(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "transformed_data_block_2", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // TRANSFORMEDPARAMETERSBLOCK LBRACE top_vardecl_or_statement* RBRACE
  public static boolean transformed_parameters_block(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "transformed_parameters_block")) return false;
    if (!nextTokenIs(builder_, TRANSFORMEDPARAMETERSBLOCK)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, TRANSFORMEDPARAMETERSBLOCK, LBRACE);
    result_ = result_ && transformed_parameters_block_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, TRANSFORMED_PARAMETERS_BLOCK, result_);
    return result_;
  }

  // top_vardecl_or_statement*
  private static boolean transformed_parameters_block_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "transformed_parameters_block_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!top_vardecl_or_statement(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "transformed_parameters_block_2", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // TRUNCATE LBRACK expression? COMMA expression? RBRACK
  public static boolean truncation(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "truncation")) return false;
    if (!nextTokenIs(builder_, TRUNCATE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, TRUNCATE, LBRACK);
    result_ = result_ && truncation_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COMMA);
    result_ = result_ && truncation_4(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, TRUNCATION, result_);
    return result_;
  }

  // expression?
  private static boolean truncation_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "truncation_2")) return false;
    expression(builder_, level_ + 1);
    return true;
  }

  // expression?
  private static boolean truncation_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "truncation_4")) return false;
    expression(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // LPAREN expression COMMA expression (COMMA expression)* RPAREN
  public static boolean tuple_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "tuple_expr")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COMMA);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && tuple_expr_4(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, TUPLE_EXPR, result_);
    return result_;
  }

  // (COMMA expression)*
  private static boolean tuple_expr_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "tuple_expr_4")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!tuple_expr_4_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "tuple_expr_4", pos_)) break;
    }
    return true;
  }

  // COMMA expression
  private static boolean tuple_expr_4_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "tuple_expr_4_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // LABRACK range      RABRACK
  //                   | LABRACK offset_mult RABRACK
  public static boolean type_constraint(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "type_constraint")) return false;
    if (!nextTokenIs(builder_, LABRACK)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = type_constraint_0(builder_, level_ + 1);
    if (!result_) result_ = type_constraint_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, TYPE_CONSTRAINT, result_);
    return result_;
  }

  // LABRACK range      RABRACK
  private static boolean type_constraint_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "type_constraint_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LABRACK);
    result_ = result_ && range(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RABRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // LABRACK offset_mult RABRACK
  private static boolean type_constraint_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "type_constraint_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LABRACK);
    result_ = result_ && offset_mult(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RABRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // BANG  unary_expr
  //              | MINUS unary_expr
  //              | PLUS  unary_expr
  //              | pow_expr
  public static boolean unary_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unary_expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, UNARY_EXPR, "<unary expr>");
    result_ = unary_expr_0(builder_, level_ + 1);
    if (!result_) result_ = unary_expr_1(builder_, level_ + 1);
    if (!result_) result_ = unary_expr_2(builder_, level_ + 1);
    if (!result_) result_ = pow_expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // BANG  unary_expr
  private static boolean unary_expr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unary_expr_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, BANG);
    result_ = result_ && unary_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // MINUS unary_expr
  private static boolean unary_expr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unary_expr_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MINUS);
    result_ = result_ && unary_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // PLUS  unary_expr
  private static boolean unary_expr_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unary_expr_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PLUS);
    result_ = result_ && unary_expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // INT | REAL | COMPLEX | VECTOR | ROWVECTOR | MATRIX
  //                      | COMPLEXVECTOR | COMPLEXROWVECTOR | COMPLEXMATRIX
  public static boolean unsized_basic_type(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unsized_basic_type")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, UNSIZED_BASIC_TYPE, "<unsized basic type>");
    result_ = consumeToken(builder_, INT);
    if (!result_) result_ = consumeToken(builder_, REAL);
    if (!result_) result_ = consumeToken(builder_, COMPLEX);
    if (!result_) result_ = consumeToken(builder_, VECTOR);
    if (!result_) result_ = consumeToken(builder_, ROWVECTOR);
    if (!result_) result_ = consumeToken(builder_, MATRIX);
    if (!result_) result_ = consumeToken(builder_, COMPLEXVECTOR);
    if (!result_) result_ = consumeToken(builder_, COMPLEXROWVECTOR);
    if (!result_) result_ = consumeToken(builder_, COMPLEXMATRIX);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // LBRACK COMMA* RBRACK
  public static boolean unsized_dims(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unsized_dims")) return false;
    if (!nextTokenIs(builder_, LBRACK)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LBRACK);
    result_ = result_ && unsized_dims_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, UNSIZED_DIMS, result_);
    return result_;
  }

  // COMMA*
  private static boolean unsized_dims_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unsized_dims_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, COMMA)) break;
      if (!empty_element_parsed_guard_(builder_, "unsized_dims_1", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // TUPLE LPAREN unsized_type COMMA unsized_type (COMMA unsized_type)* RPAREN
  public static boolean unsized_tuple_type(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unsized_tuple_type")) return false;
    if (!nextTokenIs(builder_, TUPLE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, TUPLE, LPAREN);
    result_ = result_ && unsized_type(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COMMA);
    result_ = result_ && unsized_type(builder_, level_ + 1);
    result_ = result_ && unsized_tuple_type_5(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, UNSIZED_TUPLE_TYPE, result_);
    return result_;
  }

  // (COMMA unsized_type)*
  private static boolean unsized_tuple_type_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unsized_tuple_type_5")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!unsized_tuple_type_5_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "unsized_tuple_type_5", pos_)) break;
    }
    return true;
  }

  // COMMA unsized_type
  private static boolean unsized_tuple_type_5_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unsized_tuple_type_5_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && unsized_type(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ARRAY unsized_dims unsized_basic_type
  //                | ARRAY unsized_dims unsized_tuple_type
  //                | unsized_basic_type
  //                | unsized_tuple_type
  public static boolean unsized_type(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unsized_type")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, UNSIZED_TYPE, "<unsized type>");
    result_ = unsized_type_0(builder_, level_ + 1);
    if (!result_) result_ = unsized_type_1(builder_, level_ + 1);
    if (!result_) result_ = unsized_basic_type(builder_, level_ + 1);
    if (!result_) result_ = unsized_tuple_type(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ARRAY unsized_dims unsized_basic_type
  private static boolean unsized_type_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unsized_type_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ARRAY);
    result_ = result_ && unsized_dims(builder_, level_ + 1);
    result_ = result_ && unsized_basic_type(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ARRAY unsized_dims unsized_tuple_type
  private static boolean unsized_type_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unsized_type_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ARRAY);
    result_ = result_ && unsized_dims(builder_, level_ + 1);
    result_ = result_ && unsized_tuple_type(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // var_type declared_var (COMMA declared_var_extra)* SEMICOLON
  public static boolean var_decl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "var_decl")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, VAR_DECL, "<var decl>");
    result_ = var_type(builder_, level_ + 1);
    result_ = result_ && declared_var(builder_, level_ + 1);
    result_ = result_ && var_decl_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (COMMA declared_var_extra)*
  private static boolean var_decl_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "var_decl_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!var_decl_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "var_decl_2", pos_)) break;
    }
    return true;
  }

  // COMMA declared_var_extra
  private static boolean var_decl_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "var_decl_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && declared_var_extra(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // TUPLE LPAREN var_type COMMA var_type (COMMA var_type)* RPAREN
  public static boolean var_tuple_type(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "var_tuple_type")) return false;
    if (!nextTokenIs(builder_, TUPLE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, TUPLE, LPAREN);
    result_ = result_ && var_type(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COMMA);
    result_ = result_ && var_type(builder_, level_ + 1);
    result_ = result_ && var_tuple_type_5(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, VAR_TUPLE_TYPE, result_);
    return result_;
  }

  // (COMMA var_type)*
  private static boolean var_tuple_type_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "var_tuple_type_5")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!var_tuple_type_5_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "var_tuple_type_5", pos_)) break;
    }
    return true;
  }

  // COMMA var_type
  private static boolean var_tuple_type_5_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "var_tuple_type_5_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && var_type(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // arr_dims var_tuple_type
  //            | arr_dims sized_basic_type
  //            | var_tuple_type
  //            | sized_basic_type
  public static boolean var_type(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "var_type")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, VAR_TYPE, "<var type>");
    result_ = var_type_0(builder_, level_ + 1);
    if (!result_) result_ = var_type_1(builder_, level_ + 1);
    if (!result_) result_ = var_tuple_type(builder_, level_ + 1);
    if (!result_) result_ = sized_basic_type(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // arr_dims var_tuple_type
  private static boolean var_type_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "var_type_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = arr_dims(builder_, level_ + 1);
    result_ = result_ && var_tuple_type(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // arr_dims sized_basic_type
  private static boolean var_type_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "var_type_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = arr_dims(builder_, level_ + 1);
    result_ = result_ && sized_basic_type(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // statement | var_decl
  public static boolean vardecl_or_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "vardecl_or_statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, VARDECL_OR_STATEMENT, "<vardecl or statement>");
    result_ = statement(builder_, level_ + 1);
    if (!result_) result_ = var_decl(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // ident
  public static boolean variable_expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "variable_expr")) return false;
    if (!nextTokenIs(builder_, "<variable expr>", IDENTIFIER, TRUNCATE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, VARIABLE_EXPR, "<variable expr>");
    result_ = ident(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // WHILE LPAREN expression RPAREN vardecl_or_statement
  public static boolean while_stmt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "while_stmt")) return false;
    if (!nextTokenIs(builder_, WHILE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, WHILE, LPAREN);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    result_ = result_ && vardecl_or_statement(builder_, level_ + 1);
    exit_section_(builder_, marker_, WHILE_STMT, result_);
    return result_;
  }

}
