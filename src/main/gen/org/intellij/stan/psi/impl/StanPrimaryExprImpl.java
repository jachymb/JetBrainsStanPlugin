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

public class StanPrimaryExprImpl extends ASTWrapperPsiElement implements StanPrimaryExpr {

  public StanPrimaryExprImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StanVisitor visitor) {
    visitor.visitPrimaryExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StanVisitor) accept((StanVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StanArrayExpr getArrayExpr() {
    return findChildByClass(StanArrayExpr.class);
  }

  @Override
  @Nullable
  public StanCondDistExpr getCondDistExpr() {
    return findChildByClass(StanCondDistExpr.class);
  }

  @Override
  @Nullable
  public StanEmptyRowVectorExpr getEmptyRowVectorExpr() {
    return findChildByClass(StanEmptyRowVectorExpr.class);
  }

  @Override
  @Nullable
  public StanFunCallExpr getFunCallExpr() {
    return findChildByClass(StanFunCallExpr.class);
  }

  @Override
  @Nullable
  public StanImagLiteralExpr getImagLiteralExpr() {
    return findChildByClass(StanImagLiteralExpr.class);
  }

  @Override
  @Nullable
  public StanIntLiteralExpr getIntLiteralExpr() {
    return findChildByClass(StanIntLiteralExpr.class);
  }

  @Override
  @Nullable
  public StanParenExpr getParenExpr() {
    return findChildByClass(StanParenExpr.class);
  }

  @Override
  @Nullable
  public StanRealLiteralExpr getRealLiteralExpr() {
    return findChildByClass(StanRealLiteralExpr.class);
  }

  @Override
  @Nullable
  public StanRowVectorExpr getRowVectorExpr() {
    return findChildByClass(StanRowVectorExpr.class);
  }

  @Override
  @Nullable
  public StanTargetCallExpr getTargetCallExpr() {
    return findChildByClass(StanTargetCallExpr.class);
  }

  @Override
  @Nullable
  public StanTupleExpr getTupleExpr() {
    return findChildByClass(StanTupleExpr.class);
  }

  @Override
  @Nullable
  public StanVariableExpr getVariableExpr() {
    return findChildByClass(StanVariableExpr.class);
  }

  @Override
  @Nullable
  public PsiElement getDotnumeral() {
    return findChildByType(DOTNUMERAL);
  }

}
