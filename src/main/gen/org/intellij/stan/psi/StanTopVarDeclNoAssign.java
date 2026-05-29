// This is a generated file. Not intended for manual editing.
package org.intellij.stan.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StanTopVarDeclNoAssign extends PsiElement {

  @Nullable
  StanArrDims getArrDims();

  @Nullable
  StanNoAssignVar getNoAssignVar();

  @NotNull
  List<StanNoAssignVarExtra> getNoAssignVarExtraList();

  @Nullable
  StanTopTupleType getTopTupleType();

  @Nullable
  StanTopVarType getTopVarType();

}
