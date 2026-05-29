// This is a generated file. Not intended for manual editing.
package org.intellij.stan.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.intellij.stan.psi.StanTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import org.intellij.stan.psi.*;

public class StanAtomicStatementImpl extends ASTWrapperPsiElement implements StanAtomicStatement {

  public StanAtomicStatementImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StanVisitor visitor) {
    visitor.visitAtomicStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StanVisitor) accept((StanVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StanAssignmentStmt getAssignmentStmt() {
    return findChildByClass(StanAssignmentStmt.class);
  }

  @Override
  @Nullable
  public StanBreakStmt getBreakStmt() {
    return findChildByClass(StanBreakStmt.class);
  }

  @Override
  @Nullable
  public StanContinueStmt getContinueStmt() {
    return findChildByClass(StanContinueStmt.class);
  }

  @Override
  @Nullable
  public StanEmptyStmt getEmptyStmt() {
    return findChildByClass(StanEmptyStmt.class);
  }

  @Override
  @Nullable
  public StanFatalErrorStmt getFatalErrorStmt() {
    return findChildByClass(StanFatalErrorStmt.class);
  }

  @Override
  @Nullable
  public StanFunCallStmt getFunCallStmt() {
    return findChildByClass(StanFunCallStmt.class);
  }

  @Override
  @Nullable
  public StanJacobianPlusAssignStmt getJacobianPlusAssignStmt() {
    return findChildByClass(StanJacobianPlusAssignStmt.class);
  }

  @Override
  @Nullable
  public StanPrintStmt getPrintStmt() {
    return findChildByClass(StanPrintStmt.class);
  }

  @Override
  @Nullable
  public StanRejectStmt getRejectStmt() {
    return findChildByClass(StanRejectStmt.class);
  }

  @Override
  @Nullable
  public StanReturnStmt getReturnStmt() {
    return findChildByClass(StanReturnStmt.class);
  }

  @Override
  @Nullable
  public StanTargetPlusAssignStmt getTargetPlusAssignStmt() {
    return findChildByClass(StanTargetPlusAssignStmt.class);
  }

  @Override
  @Nullable
  public StanTildeStmt getTildeStmt() {
    return findChildByClass(StanTildeStmt.class);
  }

}
