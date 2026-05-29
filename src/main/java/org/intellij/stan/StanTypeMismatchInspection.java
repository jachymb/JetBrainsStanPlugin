package org.intellij.stan;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.intellij.stan.psi.StanTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class StanTypeMismatchInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitFile(@NotNull PsiFile file) {
                Map<String, String> typeMap = StanSignatureDatabase.buildTypeMap(file.getNode());
                checkMismatches(file.getNode(), typeMap, holder);
            }
        };
    }

    private void checkMismatches(ASTNode node, Map<String, String> typeMap, ProblemsHolder holder) {
        IElementType t = node.getElementType();

        if (t == StanTypes.VAR_DECL) {
            checkVarDeclInits(node, typeMap, holder);
        } else if (t == StanTypes.ASSIGNMENT_STMT) {
            checkAssignmentStmt(node, typeMap, holder);
            return;
        }

        for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext())
            checkMismatches(c, typeMap, holder);
    }

    // ── var_decl ──────────────────────────────────────────────────────────────

    private void checkVarDeclInits(ASTNode varDecl, Map<String, String> typeMap, ProblemsHolder holder) {
        // var_decl ::= var_type declared_var (COMMA declared_var_extra)* SEMICOLON
        ASTNode typeNode = varDecl.getFirstChildNode(); // var_type
        if (typeNode == null) return;
        String lhsType = StanSignatureDatabase.typeNodeToString(typeNode);
        if (lhsType == null) return;

        for (ASTNode child = varDecl.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            IElementType ct = child.getElementType();
            if (ct != StanTypes.DECLARED_VAR && ct != StanTypes.DECLARED_VAR_EXTRA) continue;

            // Find the initializer: composite child after the ASSIGN token.
            boolean seenAssign = false;
            ASTNode initExpr = null;
            for (ASTNode dc = child.getFirstChildNode(); dc != null; dc = dc.getTreeNext()) {
                if (!seenAssign && dc.getElementType() == StanTypes.ASSIGN) {
                    seenAssign = true;
                } else if (seenAssign && dc.getFirstChildNode() != null) {
                    initExpr = dc;
                    break;
                }
            }
            if (initExpr == null) continue;

            String rhsType = StanSignatureDatabase.inferExprType(initExpr, typeMap);
            if (!reportIfMismatch(lhsType, rhsType, initExpr, holder)) break;
        }
    }

    // ── assignment_stmt ───────────────────────────────────────────────────────

    private void checkAssignmentStmt(ASTNode stmt, Map<String, String> typeMap, ProblemsHolder holder) {
        // assignment_stmt ::= expression assignment_op expression SEMICOLON
        // We need the lvalue name (simple variable) and the rhs expression.
        String lhsName = null;
        boolean lhsComplex = false; // subscripted, tuple, etc.
        ASTNode rhsExpr = null;
        boolean seenOp = false;

        for (ASTNode child = stmt.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            IElementType ct = child.getElementType();
            if (!seenOp) {
                if (ct == StanTypes.ASSIGNMENT_OP) {
                    seenOp = true;
                } else if (ct == StanTypes.VARIABLE_EXPR && lhsName == null) {
                    lhsName = child.getText(); // variable_expr text = the identifier name
                } else if (ct == StanTypes.INDEX_EXPR || ct == StanTypes.POSTFIX_EXPR
                        || ct == StanTypes.TUPLE_EXPR) {
                    lhsComplex = true;
                }
            } else if (rhsExpr == null && child.getFirstChildNode() != null) {
                rhsExpr = child;
            }
        }

        if (lhsName == null || rhsExpr == null || lhsComplex) return;

        String lhsType = typeMap.get(lhsName);
        if (lhsType == null) return;

        String rhsType = StanSignatureDatabase.inferExprType(rhsExpr, typeMap);
        reportIfMismatch(lhsType, rhsType, rhsExpr, holder);
    }

    private boolean reportIfMismatch(@NotNull String lhsType, @Nullable String rhsType,
                                     @NotNull ASTNode rhsNode, @NotNull ProblemsHolder holder) {
        if (rhsType == null) return false;
        if (StanSignatureDatabase.isCompatible(lhsType, rhsType)) return false;
        if (!isScalar(lhsType) && !isScalar(rhsType)) return false;

        holder.registerProblem(rhsNode.getPsi(),
                "Type mismatch: cannot assign " + rhsType + " to " + lhsType,
                ProblemHighlightType.GENERIC_ERROR);
        return true;
    }

    private static boolean isScalar(String t) {
        return "int".equals(t) || "real".equals(t) || "complex".equals(t);
    }
}
