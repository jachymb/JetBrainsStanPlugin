// This is a generated file. Not intended for manual editing.
package org.intellij.stan.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StanPrimaryExpr extends PsiElement {

  @Nullable
  StanArrayExpr getArrayExpr();

  @Nullable
  StanCondDistExpr getCondDistExpr();

  @Nullable
  StanEmptyRowVectorExpr getEmptyRowVectorExpr();

  @Nullable
  StanFunCallExpr getFunCallExpr();

  @Nullable
  StanImagLiteralExpr getImagLiteralExpr();

  @Nullable
  StanIntLiteralExpr getIntLiteralExpr();

  @Nullable
  StanParenExpr getParenExpr();

  @Nullable
  StanRealLiteralExpr getRealLiteralExpr();

  @Nullable
  StanRowVectorExpr getRowVectorExpr();

  @Nullable
  StanTargetCallExpr getTargetCallExpr();

  @Nullable
  StanTupleExpr getTupleExpr();

  @Nullable
  StanVariableExpr getVariableExpr();

  @Nullable
  PsiElement getDotnumeral();

}
