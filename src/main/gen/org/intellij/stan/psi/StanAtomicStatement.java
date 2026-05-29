// This is a generated file. Not intended for manual editing.
package org.intellij.stan.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface StanAtomicStatement extends PsiElement {

  @Nullable
  StanAssignmentStmt getAssignmentStmt();

  @Nullable
  StanBreakStmt getBreakStmt();

  @Nullable
  StanContinueStmt getContinueStmt();

  @Nullable
  StanEmptyStmt getEmptyStmt();

  @Nullable
  StanFatalErrorStmt getFatalErrorStmt();

  @Nullable
  StanFunCallStmt getFunCallStmt();

  @Nullable
  StanJacobianPlusAssignStmt getJacobianPlusAssignStmt();

  @Nullable
  StanPrintStmt getPrintStmt();

  @Nullable
  StanRejectStmt getRejectStmt();

  @Nullable
  StanReturnStmt getReturnStmt();

  @Nullable
  StanTargetPlusAssignStmt getTargetPlusAssignStmt();

  @Nullable
  StanTildeStmt getTildeStmt();

}
