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

public class StanVardeclOrStatementImpl extends ASTWrapperPsiElement implements StanVardeclOrStatement {

  public StanVardeclOrStatementImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StanVisitor visitor) {
    visitor.visitVardeclOrStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StanVisitor) accept((StanVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public StanStatement getStatement() {
    return findChildByClass(StanStatement.class);
  }

  @Override
  @Nullable
  public StanVarDecl getVarDecl() {
    return findChildByClass(StanVarDecl.class);
  }

}
