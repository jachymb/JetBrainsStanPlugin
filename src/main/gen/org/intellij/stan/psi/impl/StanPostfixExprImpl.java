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

public class StanPostfixExprImpl extends ASTWrapperPsiElement implements StanPostfixExpr {

  public StanPostfixExprImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StanVisitor visitor) {
    visitor.visitPostfixExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StanVisitor) accept((StanVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StanIndexExpr getIndexExpr() {
    return findChildByClass(StanIndexExpr.class);
  }

  @Override
  @Nullable
  public StanPostfixExpr getPostfixExpr() {
    return findChildByClass(StanPostfixExpr.class);
  }

  @Override
  @Nullable
  public PsiElement getDotnumeral() {
    return findChildByType(DOTNUMERAL);
  }

}
