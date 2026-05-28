package org.intellij.stan;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class StanUndeclaredVariableInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitFile(@NotNull PsiFile file) {
                Set<String> declared = new HashSet<>();
                collectDeclarations(file.getNode(), declared);
                checkReferences(file.getNode(), declared, holder);
            }
        };
    }

    // -------------------------------------------------------------------------
    // Pass 1 — collect every declared name
    // -------------------------------------------------------------------------

    private void collectDeclarations(ASTNode node, Set<String> declared) {
        IElementType t = node.getElementType();

        if (t == StanElementTypes.DECLARED_VAR) {
            ASTNode first = node.getFirstChildNode();
            if (first != null && first.getElementType() == StanTokenTypes.IDENTIFIER) {
                declared.add(first.getText());
            }

        } else if (t == StanElementTypes.FOR_RANGE_STMT || t == StanElementTypes.FOR_EACH_STMT) {
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                if (c.getElementType() == StanTokenTypes.IDENTIFIER) {
                    declared.add(c.getText());
                    break;
                }
            }

        } else if (t == StanElementTypes.ARG_DECL) {
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                if (c.getElementType() == StanTokenTypes.IDENTIFIER) {
                    declared.add(c.getText());
                }
            }
            return;

        } else if (t == StanElementTypes.FUN_DEF) {
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                if (c.getElementType() == StanTokenTypes.IDENTIFIER) {
                    declared.add(c.getText());
                    break;
                }
            }
        }

        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            collectDeclarations(child, declared);
        }
    }

    // -------------------------------------------------------------------------
    // Pass 2 — flag every undeclared variable reference
    // -------------------------------------------------------------------------

    private void checkReferences(ASTNode node, Set<String> declared, ProblemsHolder holder) {
        IElementType t = node.getElementType();

        if (t == StanElementTypes.VARIABLE_EXPR) {
            // Identifier used as a value in an expression
            ASTNode nameNode = node.getFirstChildNode();
            if (nameNode != null && nameNode.getElementType() == StanTokenTypes.IDENTIFIER) {
                flagIfUndeclared(nameNode, declared, holder);
            }
            return;
        }

        if (t == StanElementTypes.ASSIGNMENT_STMT) {
            // The lvalue is NOT wrapped in VARIABLE_EXPR — tryParseLvalue() emits bare tokens.
            // Walk children: before the assignment op → lvalue region; after → RHS expression.
            boolean seenOp = false;
            for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
                IElementType ct = child.getElementType();
                if (!seenOp && StanTokenTypes.ASSIGNMENT_OPS.contains(ct)) {
                    seenOp = true;
                } else if (!seenOp) {
                    if (ct == StanTokenTypes.IDENTIFIER) {
                        flagIfUndeclared(child, declared, holder);
                    } else if (ct == StanElementTypes.TUPLE_DECL_PACK) {
                        checkTupleLvalue(child, declared, holder);
                    } else if (ct == StanElementTypes.INDEX_LIST) {
                        // Subscript indices in the lvalue: z[i] — check i
                        checkReferences(child, declared, holder);
                    }
                } else {
                    checkReferences(child, declared, holder);
                }
            }
            return;
        }

        if (t == StanElementTypes.TUPLE_DECL_PACK) {
            checkTupleLvalue(node, declared, holder);
            return;
        }

        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            checkReferences(child, declared, holder);
        }
    }

    /** Check bare IDENTIFIER tokens inside a tuple lvalue like (a, b[i]) = ... */
    private void checkTupleLvalue(ASTNode tupleNode, Set<String> declared, ProblemsHolder holder) {
        for (ASTNode child = tupleNode.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            IElementType ct = child.getElementType();
            if (ct == StanTokenTypes.IDENTIFIER) {
                flagIfUndeclared(child, declared, holder);
            } else if (ct == StanElementTypes.TUPLE_DECL_PACK) {
                checkTupleLvalue(child, declared, holder);
            } else if (ct == StanElementTypes.INDEX_LIST) {
                checkReferences(child, declared, holder);
            }
        }
    }

    private void flagIfUndeclared(ASTNode identNode, Set<String> declared, ProblemsHolder holder) {
        String name = identNode.getText();
        if (!declared.contains(name)) {
            holder.registerProblem(identNode.getPsi(),
                    "Variable '" + name + "' is not declared",
                    ProblemHighlightType.GENERIC_ERROR);
        }
    }
}
