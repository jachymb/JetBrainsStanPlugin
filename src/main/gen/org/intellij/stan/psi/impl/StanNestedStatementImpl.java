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

public class StanNestedStatementImpl extends ASTWrapperPsiElement implements StanNestedStatement {

  public StanNestedStatementImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StanVisitor visitor) {
    visitor.visitNestedStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StanVisitor) accept((StanVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StanBlockStmt getBlockStmt() {
    return findChildByClass(StanBlockStmt.class);
  }

  @Override
  @Nullable
  public StanForEachStmt getForEachStmt() {
    return findChildByClass(StanForEachStmt.class);
  }

  @Override
  @Nullable
  public StanForRangeStmt getForRangeStmt() {
    return findChildByClass(StanForRangeStmt.class);
  }

  @Override
  @Nullable
  public StanIfElseStmt getIfElseStmt() {
    return findChildByClass(StanIfElseStmt.class);
  }

  @Override
  @Nullable
  public StanIfStmt getIfStmt() {
    return findChildByClass(StanIfStmt.class);
  }

  @Override
  @Nullable
  public StanProfileStmt getProfileStmt() {
    return findChildByClass(StanProfileStmt.class);
  }

  @Override
  @Nullable
  public StanWhileStmt getWhileStmt() {
    return findChildByClass(StanWhileStmt.class);
  }

}
