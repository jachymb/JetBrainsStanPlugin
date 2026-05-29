// This is a generated file. Not intended for manual editing.
package org.intellij.stan.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StanTopVarDecl extends PsiElement {

  @Nullable
  StanArrDims getArrDims();

  @NotNull
  StanTopDeclaredVar getTopDeclaredVar();

  @NotNull
  List<StanTopDeclaredVarExtra> getTopDeclaredVarExtraList();

  @Nullable
  StanTopTupleType getTopTupleType();

  @Nullable
  StanTopVarType getTopVarType();

}
