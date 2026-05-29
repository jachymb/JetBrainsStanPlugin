package org.intellij.stan;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Checks initializer and assignment type compatibility against the full promotion
 * rules from stanc3's UnsizedType.common_type, delegating to StanSignatureDatabase
 * for type strings and the isCompatible predicate.
 */
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

        if (t == StanElementTypes.VAR_DECL) {
            checkVarDeclInits(node, typeMap, holder);

        } else if (t == StanElementTypes.ASSIGNMENT_STMT) {
            checkAssignmentStmt(node, typeMap, holder);
            return; // we handled everything inside
        }

        for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext())
            checkMismatches(c, typeMap, holder);
    }

    // ── Declaration initializers ──────────────────────────────────────────────

    private void checkVarDeclInits(ASTNode varDecl, Map<String, String> typeMap, ProblemsHolder holder) {
        ASTNode typeNode = varDecl.getFirstChildNode();
        if (typeNode == null) return;
        String lhsType = StanSignatureDatabase.typeNodeToString(typeNode);
        if (lhsType == null) return; // tuple / unsized — skip

        for (ASTNode child = varDecl.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            if (child.getElementType() != StanElementTypes.DECLARED_VAR) continue;

            // Locate: first composite node after ASSIGN inside DECLARED_VAR
            boolean seenAssign = false;
            ASTNode initExpr = null;
            for (ASTNode dc = child.getFirstChildNode(); dc != null; dc = dc.getTreeNext()) {
                if (!seenAssign && dc.getElementType() == StanTokenTypes.ASSIGN) {
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

    // ── Assignment statements ─────────────────────────────────────────────────

    private void checkAssignmentStmt(ASTNode stmt, Map<String, String> typeMap, ProblemsHolder holder) {
        String lhsName = null;
        boolean lhsSubscripted = false;
        ASTNode rhsExpr = null;
        boolean seenOp = false;

        for (ASTNode child = stmt.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            IElementType ct = child.getElementType();
            if (!seenOp) {
                if (StanTokenTypes.ASSIGNMENT_OPS.contains(ct)) {
                    seenOp = true;
                } else if (ct == StanTokenTypes.IDENTIFIER && lhsName == null) {
                    lhsName = child.getText();
                } else if (ct == StanElementTypes.INDEX_LIST || ct == StanTokenTypes.DOT) {
                    lhsSubscripted = true; // element type differs from declared type
                } else if (ct == StanElementTypes.TUPLE_DECL_PACK) {
                    return; // tuple lvalue — skip
                }
            } else if (rhsExpr == null && child.getFirstChildNode() != null) {
                rhsExpr = child;
            }
        }

        if (lhsName == null || rhsExpr == null || lhsSubscripted) return;

        String lhsType = typeMap.get(lhsName);
        if (lhsType == null) return; // undeclared — flagged by the undeclared-variable inspection

        String rhsType = StanSignatureDatabase.inferExprType(rhsExpr, typeMap);
        reportIfMismatch(lhsType, rhsType, rhsExpr, holder);
    }

    // ── Compatibility check ───────────────────────────────────────────────────

    /**
     * Reports a problem if rhsType is known and not compatible with lhsType.
     * Returns true when a problem was reported (callers can stop after the first).
     */
    private boolean reportIfMismatch(@NotNull String lhsType,
                                     @Nullable String rhsType,
                                     @NotNull ASTNode rhsNode,
                                     @NotNull ProblemsHolder holder) {
        if (rhsType == null) return false; // unknown rhs — conservative
        if (StanSignatureDatabase.isCompatible(lhsType, rhsType)) return false;

        // Extra gate: skip container↔container mismatches — they need full stanc analysis
        // (e.g. ordered vs vector are compatible in Stan but our type map collapses them).
        if (!StanSignatureDatabase.isScalarType(lhsType) && !StanSignatureDatabase.isScalarType(rhsType)) return false;

        holder.registerProblem(rhsNode.getPsi(),
                "Type mismatch: cannot assign " + rhsType + " to " + lhsType,
                ProblemHighlightType.GENERIC_ERROR);
        return true;
    }
}
