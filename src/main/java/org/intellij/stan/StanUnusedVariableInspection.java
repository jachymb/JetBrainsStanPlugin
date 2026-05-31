package org.intellij.stan;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class StanUnusedVariableInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitFile(@NotNull PsiFile file) {
                analyzeFile(file, holder);
            }
        };
    }

    private void analyzeFile(PsiFile file, ProblemsHolder holder) {
        Set<String> usedNames = new HashSet<>();
        collectUsedNames(file.getNode(), usedNames);
        checkDeclarations(file.getNode(), usedNames, holder);
        checkFunctionParameters(file.getNode(), holder);
    }

    /** Collect every identifier that is read somewhere in the file. */
    private void collectUsedNames(ASTNode node, Set<String> usedNames) {
        IElementType t = node.getElementType();
        if (t == StanElementTypes.VARIABLE_EXPR) {
            ASTNode child = node.getFirstChildNode();
            if (child != null && child.getElementType() == StanTokenTypes.IDENTIFIER)
                usedNames.add(child.getText());
            return; // VARIABLE_EXPR is leaf-like — no further recursion needed
        }
        // Compound assignments (x += e) and indexed assignments (x[i] = e) both read the lvalue.
        if (t == StanElementTypes.ASSIGNMENT_STMT)
            markLvalueUsedIfReadingAssignment(node, usedNames);
        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext())
            collectUsedNames(child, usedNames);
    }

    /**
     * For {@code x += e} and {@code x[i] = e}, the lvalue variable {@code x} is also read.
     * Plain {@code x = e} and deprecated {@code x <- e} are pure writes — not counted here.
     */
    private static void markLvalueUsedIfReadingAssignment(ASTNode assignStmt, Set<String> usedNames) {
        ASTNode lvalueIdent = null;
        boolean hasIndex = false;
        for (ASTNode c = assignStmt.getFirstChildNode(); c != null; c = c.getTreeNext()) {
            IElementType ct = c.getElementType();
            if (StanTokenTypes.ASSIGNMENT_OPS.contains(ct)) {
                boolean isCompound = ct != StanTokenTypes.ASSIGN && ct != StanTokenTypes.ARROW;
                if ((isCompound || hasIndex) && lvalueIdent != null)
                    usedNames.add(lvalueIdent.getText());
                return;
            }
            if (ct == StanTokenTypes.IDENTIFIER && lvalueIdent == null) lvalueIdent = c;
            if (ct == StanElementTypes.INDEX_LIST) hasIndex = true;
        }
    }

    /** Walk DECLARED_VAR nodes and flag any whose name never appears in usedNames. */
    private void checkDeclarations(ASTNode node, Set<String> usedNames, ProblemsHolder holder) {
        if (node.getElementType() == StanElementTypes.DECLARED_VAR) {
            ASTNode nameNode = node.getFirstChildNode();
            if (nameNode != null && nameNode.getElementType() == StanTokenTypes.IDENTIFIER) {
                String name = nameNode.getText();
                // Top-level declarations in generated quantities are outputs — they don't need to be read.
                if (!usedNames.contains(name) && !isTopLevelInGeneratedQuantities(node)) {
                    PsiElement psiName = nameNode.getPsi();
                    holder.registerProblem(psiName,
                            "Variable '" + name + "' is never used",
                            ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                }
            }
            // still recurse — DECLARED_VAR can have an initializer with nested decls (tuples)
        }
        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            checkDeclarations(child, usedNames, holder);
        }
    }

    /**
     * Walk FUN_DEF nodes and flag ARG_DECL parameters that are never used in the function body.
     * Scoped per-function: a parameter is only checked against its own function's body.
     */
    private void checkFunctionParameters(ASTNode node, ProblemsHolder holder) {
        if (node.getElementType() == StanElementTypes.FUN_DEF) {
            ASTNode paramList = null;
            ASTNode bodyStart = null;
            for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
                if (child.getElementType() == StanElementTypes.PARAM_LIST) {
                    paramList = child;
                } else if (paramList != null && child.getElementType() == StanTokenTypes.RPAREN) {
                    bodyStart = child.getTreeNext();
                    break;
                }
            }
            if (paramList == null) return;

            Set<String> usedInBody = new HashSet<>();
            for (ASTNode bodyNode = bodyStart; bodyNode != null; bodyNode = bodyNode.getTreeNext()) {
                collectUsedNames(bodyNode, usedInBody);
            }

            for (ASTNode child = paramList.getFirstChildNode(); child != null; child = child.getTreeNext()) {
                if (child.getElementType() != StanElementTypes.ARG_DECL) continue;
                // The parameter name is the last direct identifier-like child of ARG_DECL
                ASTNode nameNode = null;
                for (ASTNode argChild = child.getFirstChildNode(); argChild != null; argChild = argChild.getTreeNext()) {
                    if (StanTokenTypes.IDENTIFIERS.contains(argChild.getElementType())) {
                        nameNode = argChild;
                    }
                }
                if (nameNode == null) continue;
                String name = nameNode.getText();
                if (!usedInBody.contains(name)) {
                    holder.registerProblem(nameNode.getPsi(),
                            "Parameter '" + name + "' is never used",
                            ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                }
            }
            return; // Stan doesn't allow nested FUN_DEFs, so no need to recurse further
        }
        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            checkFunctionParameters(child, holder);
        }
    }

    /** Returns true when the DECLARED_VAR is a direct child of a top-level VAR_DECL in generated quantities. */
    private boolean isTopLevelInGeneratedQuantities(ASTNode declaredVar) {
        ASTNode varDecl = declaredVar.getTreeParent();
        if (varDecl == null || varDecl.getElementType() != StanElementTypes.VAR_DECL) return false;
        ASTNode block = varDecl.getTreeParent();
        return block != null && block.getElementType() == StanElementTypes.GENERATED_QUANTITIES_BLOCK;
    }
}
