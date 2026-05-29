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

public class StanTopVarDeclNoAssignImpl extends ASTWrapperPsiElement implements StanTopVarDeclNoAssign {

  public StanTopVarDeclNoAssignImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StanVisitor visitor) {
    visitor.visitTopVarDeclNoAssign(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StanVisitor) accept((StanVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StanArrDims getArrDims() {
    return findChildByClass(StanArrDims.class);
  }

  @Override
  @Nullable
  public StanNoAssignVar getNoAssignVar() {
    return findChildByClass(StanNoAssignVar.class);
  }

  @Override
  @NotNull
  public List<StanNoAssignVarExtra> getNoAssignVarExtraList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, StanNoAssignVarExtra.class);
  }

  @Override
  @Nullable
  public StanTopTupleType getTopTupleType() {
    return findChildByClass(StanTopTupleType.class);
  }

  @Override
  @Nullable
  public StanTopVarType getTopVarType() {
    return findChildByClass(StanTopVarType.class);
  }

}
