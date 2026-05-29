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

    // ── Pass 1: collect declared names ────────────────────────────────────────

    private void collectDeclarations(ASTNode node, Set<String> declared) {
        IElementType t = node.getElementType();

        // All declarator node types — getText() gives the declared name text.
        if (t == StanTypes.DECLARED_VAR || t == StanTypes.DECLARED_VAR_EXTRA
         || t == StanTypes.TOP_DECLARED_VAR || t == StanTypes.TOP_DECLARED_VAR_EXTRA
         || t == StanTypes.NO_ASSIGN_VAR || t == StanTypes.NO_ASSIGN_VAR_EXTRA) {
            ASTNode first = node.getFirstChildNode(); // decl_identifier or ident
            if (first != null) declared.add(first.getText());

        // for-range loop variable: for (i in lo:hi)
        } else if (t == StanTypes.FOR_RANGE_STMT) {
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                if (c.getElementType() == StanTypes.IDENT) { declared.add(c.getText()); break; }
            }

        // for-each loop variable: for (x in container)
        } else if (t == StanTypes.FOR_EACH_STMT) {
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                if (c.getElementType() == StanTypes.IDENT) { declared.add(c.getText()); break; }
            }

        // Function formal parameters: DATABLOCK? unsized_type decl_identifier
        } else if (t == StanTypes.ARG_DECL) {
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                if (c.getElementType() == StanTypes.DECL_IDENTIFIER) {
                    declared.add(c.getText());
                    break;
                }
            }
            return;

        // Function name
        } else if (t == StanTypes.FUNCTION_DEF) {
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                if (c.getElementType() == StanTypes.LPAREN) break;
                if (c.getElementType() == StanTypes.DECL_IDENTIFIER) {
                    declared.add(c.getText());
                    break;
                }
            }
        }

        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext())
            collectDeclarations(child, declared);
    }

    // ── Pass 2: flag undeclared references ────────────────────────────────────

    private void checkReferences(ASTNode node, Set<String> declared, ProblemsHolder holder) {
        IElementType t = node.getElementType();

        if (t == StanTypes.VARIABLE_EXPR) {
            // variable_expr ::= ident — get the leaf for highlighting.
            ASTNode identNode = node.getFirstChildNode(); // ident
            ASTNode leaf = StanSyntaxUtil.findLeaf(identNode);
            if (leaf != null && leaf.getElementType() == StanTypes.IDENTIFIER) {
                flagIfUndeclared(leaf, declared, holder);
            }
            return;
        }

        if (t == StanTypes.ASSIGNMENT_STMT) {
            boolean seenOp = false;
            for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
                IElementType ct = child.getElementType();
                if (!seenOp && ct == StanTypes.ASSIGNMENT_OP) {
                    seenOp = true;
                } else if (!seenOp) {
                    checkLvalueRefs(child, declared, holder);
                } else {
                    checkReferences(child, declared, holder);
                }
            }
            return;
        }

        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext())
            checkReferences(child, declared, holder);
    }

    /** Check lvalue sub-expressions for undeclared identifiers. */
    private void checkLvalueRefs(ASTNode node, Set<String> declared, ProblemsHolder holder) {
        IElementType t = node.getElementType();
        if (t == StanTypes.IDENT) {
            ASTNode leaf = StanSyntaxUtil.findLeaf(node);
            if (leaf != null && leaf.getElementType() == StanTypes.IDENTIFIER)
                flagIfUndeclared(leaf, declared, holder);
            return;
        }
        if (t == StanTypes.VARIABLE_EXPR || t == StanTypes.INDEX_EXPR || t == StanTypes.POSTFIX_EXPR) {
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext())
                checkLvalueRefs(c, declared, holder);
            return;
        }
        if (t == StanTypes.INDEX_LIST) { checkReferences(node, declared, holder); return; }
        if (t == StanTypes.TUPLE_EXPR || t == StanTypes.PAREN_EXPR) {
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext())
                checkLvalueRefs(c, declared, holder);
        }
    }

    private void flagIfUndeclared(ASTNode identLeaf, Set<String> declared, ProblemsHolder holder) {
        String name = identLeaf.getText();
        if (!declared.contains(name)) {
            holder.registerProblem(identLeaf.getPsi(),
                    "Variable '" + name + "' is not declared",
                    ProblemHighlightType.GENERIC_ERROR);
        }
    }
}
