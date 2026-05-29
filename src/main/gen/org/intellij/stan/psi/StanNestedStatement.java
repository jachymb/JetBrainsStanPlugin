// This is a generated file. Not intended for manual editing.
package org.intellij.stan.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StanNestedStatement extends PsiElement {

  @Nullable
  StanBlockStmt getBlockStmt();

  @Nullable
  StanForEachStmt getForEachStmt();

  @Nullable
  StanForRangeStmt getForRangeStmt();

  @Nullable
  StanIfElseStmt getIfElseStmt();

  @Nullable
  StanIfStmt getIfStmt();

  @Nullable
  StanProfileStmt getProfileStmt();

  @Nullable
  StanWhileStmt getWhileStmt();

}
