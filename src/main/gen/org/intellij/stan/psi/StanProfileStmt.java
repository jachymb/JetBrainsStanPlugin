// This is a generated file. Not intended for manual editing.
package org.intellij.stan.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StanProfileStmt extends PsiElement {

  @NotNull
  StanStringLiteral getStringLiteral();

  @NotNull
  List<StanVardeclOrStatement> getVardeclOrStatementList();

}
