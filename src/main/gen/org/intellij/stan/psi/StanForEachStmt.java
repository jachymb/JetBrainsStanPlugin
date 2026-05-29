// This is a generated file. Not intended for manual editing.
package org.intellij.stan.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StanForEachStmt extends PsiElement {

  @NotNull
  StanExpression getExpression();

  @NotNull
  StanIdent getIdent();

  @NotNull
  StanVardeclOrStatement getVardeclOrStatement();

}
