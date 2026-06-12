package org.intellij.stan;

import com.intellij.lang.ASTNode;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StanDocumentationProvider extends AbstractDocumentationProvider {

    @Override
    public @Nullable PsiElement getCustomDocumentationElement(
            @NotNull Editor editor,
            @NotNull PsiFile file,
            @Nullable PsiElement contextElement,
            int targetOffset) {
        if (contextElement == null) return null;
        IElementType type = contextElement.getNode().getElementType();
        if (type != StanTokenTypes.IDENTIFIER && type != StanTokenTypes.BUILTIN_FUNCTION) return null;
        return canDocument(contextElement) ? contextElement : null;
    }

    @Override
    public @Nullable String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        PsiElement token = originalElement != null ? originalElement : element;
        if (token == null) return null;
        IElementType type = token.getNode().getElementType();
        if (type != StanTokenTypes.IDENTIFIER && type != StanTokenTypes.BUILTIN_FUNCTION) return null;
        return buildDoc(token);
    }

    // ── Eligibility check ────────────────────────────────────────────────────

    private static boolean canDocument(PsiElement element) {
        PsiElement parent = element.getParent();
        if (parent == null) return false;
        IElementType parentType = parent.getNode().getElementType();
        String name = element.getText();
        StanSignatureDatabase db = StanSignatureDatabase.getInstance();

        if (isFunctionNameInCall(element, parent, parentType)) {
            return db.hasFunction(name)
                    || hasUserDefinedFunDef(element, name);
        }
        if (parentType == StanElementTypes.TILDE_STMT && isAfterTilde(element)) {
            return db.hasFunction(name + "_lpdf") || db.hasFunction(name + "_lpmf");
        }
        if (parentType == StanElementTypes.FUN_DEF && isFunDefNameToken(element, parent)) {
            return true; // always show for user-defined function defs
        }
        return false;
    }

    // ── Documentation builder ────────────────────────────────────────────────

    private static @Nullable String buildDoc(PsiElement token) {
        PsiElement parent = token.getParent();
        if (parent == null) return null;
        IElementType parentType = parent.getNode().getElementType();
        String name = token.getText();
        StanSignatureDatabase db = StanSignatureDatabase.getInstance();

        // Tilde distribution name: y ~ normal(mu, sigma)
        if (parentType == StanElementTypes.TILDE_STMT && isAfterTilde(token)) {
            return buildDistributionDoc(name, db);
        }

        // Function call or conditional distribution call
        if (isFunctionNameInCall(token, parent, parentType)) {
            // User-defined function — show its signature from the FUN_DEF
            ASTNode funDef = findFunDef(token.getContainingFile().getNode(), name);
            if (funDef != null && !db.hasFunction(name)) {
                return buildUserFunDoc(name, funDef);
            }
            // Builtin (or shadowed builtin — show builtin sigs)
            List<StanSignatureDatabase.Signature> sigs = db.getSignatures(name);
            if (!sigs.isEmpty()) return buildBuiltinDoc(name, sigs, findMatchedSignature(token, sigs));
            // User-defined fallback
            if (funDef != null) return buildUserFunDoc(name, funDef);
            return null;
        }

        // Function definition name token
        if (parentType == StanElementTypes.FUN_DEF && isFunDefNameToken(token, parent)) {
            return buildUserFunDoc(name, parent.getNode());
        }

        return null;
    }

    // ── Distribution tilde documentation ────────────────────────────────────

    private static String buildDistributionDoc(String baseName, StanSignatureDatabase db) {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>").append(esc(baseName)).append("</b>");
        sb.append(" &nbsp;<i>(distribution)</i>");

        boolean found = false;
        // Show _lpdf overloads (continuous distributions)
        found |= appendFunctionSigs(sb, baseName + "_lpdf", db, true);
        // Show _lpmf overloads (discrete distributions)
        found |= appendFunctionSigs(sb, baseName + "_lpmf", db, true);

        if (!found) return null;
        return sb.toString();
    }

    // ── Builtin function documentation ───────────────────────────────────────

    private static String buildBuiltinDoc(String name, List<StanSignatureDatabase.Signature> sigs,
                                          @Nullable StanSignatureDatabase.Signature matched) {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>").append(esc(name)).append("</b>");
        sb.append("<pre style='margin:4px 0'>");
        boolean isCondDist = isConditioningSuffix(name);
        if (matched != null) {
            sb.append("<b>");
            appendSignatureLine(sb, name, matched, isCondDist);
            sb.append("</b>");
            for (StanSignatureDatabase.Signature sig : sigs) {
                if (sig == matched) continue;
                sb.append("\n");
                appendSignatureLine(sb, name, sig, isCondDist);
            }
        } else {
            for (StanSignatureDatabase.Signature sig : sigs) {
                appendSignatureLine(sb, name, sig, isCondDist);
                sb.append("\n");
            }
            if (sb.charAt(sb.length() - 1) == '\n') sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("</pre>");
        return sb.toString();
    }

    /**
     * Returns the lowest-promotion-cost overload for the call at {@code callToken}, or null if
     * no overload is compatible or two overloads tie. Exact matches (cost 0) naturally beat
     * promoted ones, so no separate exact-match pass is needed.
     */
    private static @Nullable StanSignatureDatabase.Signature findMatchedSignature(
            PsiElement callToken, List<StanSignatureDatabase.Signature> sigs) {
        PsiFile file = callToken.getContainingFile();
        if (file == null) return null;
        Map<String, String> typeMap = StanSignatureDatabase.buildTypeMap(file.getNode());
        List<ASTNode> argNodes = StanSignatureDatabase.collectArgExprs(callToken.getParent().getNode());
        int argCount = argNodes.size();
        List<String> argTypes = new ArrayList<>(argCount);
        for (ASTNode a : argNodes) argTypes.add(StanSignatureDatabase.inferExprType(a, typeMap));

        StanSignatureDatabase.Signature best = null;
        int bestCost = Integer.MAX_VALUE;
        boolean unique = false;
        for (StanSignatureDatabase.Signature sig : sigs) {
            if (sig.args.size() != argCount) continue;
            int cost = sigCost(sig.args, argTypes);
            if (cost < bestCost) { bestCost = cost; best = sig; unique = true; }
            else if (cost == bestCost) { unique = false; }
        }
        return unique ? best : null;
    }

    /** Total promotion cost from {@code actual} arg types to {@code expected} param types. */
    private static int sigCost(List<String> expected, List<String> actual) {
        int total = 0;
        for (int i = 0; i < expected.size(); i++) {
            int c = argCost(expected.get(i), i < actual.size() ? actual.get(i) : null);
            if (c == Integer.MAX_VALUE) return Integer.MAX_VALUE;
            total += c;
        }
        return total;
    }

    /**
     * Promotion steps from {@code actual} to {@code expected}.
     * Scalars follow the chain int(0)→real(1)→complex(2); distance = index difference.
     * Container promotions (e.g. vector→complex_vector) cost 1 via {@code isCompatible}.
     * Returns {@code Integer.MAX_VALUE} when incompatible.
     */
    private static int argCost(String expected, @Nullable String actual) {
        if (actual == null || expected.equals(actual)) return 0;
        int ei = scalarIndex(expected), ai = scalarIndex(actual);
        if (ei > ai && ai >= 0) return ei - ai;
        if (StanSignatureDatabase.isCompatible(expected, actual)) return 1;
        return Integer.MAX_VALUE;
    }

    private static int scalarIndex(String type) {
        if ("int".equals(type))     return 0;
        if ("real".equals(type))    return 1;
        if ("complex".equals(type)) return 2;
        return -1;
    }

    private static boolean appendFunctionSigs(StringBuilder sb, String name, StanSignatureDatabase db, boolean header) {
        List<StanSignatureDatabase.Signature> sigs = db.getSignatures(name);
        if (sigs.isEmpty()) return false;
        if (header) {
            sb.append("<br/><i>").append(esc(name)).append(":</i>");
        }
        sb.append("<pre style='margin:4px 0'>");
        boolean isCondDist = isConditioningSuffix(name);
        for (StanSignatureDatabase.Signature sig : sigs) {
            appendSignatureLine(sb, name, sig, isCondDist);
            sb.append("\n");
        }
        if (sb.charAt(sb.length() - 1) == '\n') sb.deleteCharAt(sb.length() - 1);
        sb.append("</pre>");
        return true;
    }

    private static void appendSignatureLine(StringBuilder sb, String name,
                                             StanSignatureDatabase.Signature sig, boolean isCondDist) {
        sb.append("(");

        List<String> args = sig.args;
        if (isCondDist && args.size() >= 1) {
            // First arg before |, rest after (Stan bar notation convention)
            sb.append(esc(args.get(0)));
            sb.append(" | ");
            for (int i = 1; i < args.size(); i++) {
                if (i > 1) sb.append(", ");
                sb.append(esc(args.get(i)));
            }
        } else {
            for (int i = 0; i < args.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(esc(args.get(i)));
            }
        }
        sb.append(") =&gt; ");

        // Return type
        if (sig.retTuple != null) {
            sb.append("(");
            for (int i = 0; i < sig.retTuple.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(esc(sig.retTuple.get(i)));
            }
            sb.append(")");
        } else {
            sb.append(esc(sig.ret != null ? sig.ret : "void"));
        }
    }

    // ── User-defined function documentation ─────────────────────────────────

    private static String buildUserFunDoc(String name, ASTNode funDef) {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>").append(esc(name)).append("</b>");
        sb.append(" &nbsp;<i>(user-defined)</i>");
        sb.append("<pre style='margin:4px 0'>");
        appendFunDefSignature(sb, funDef);
        sb.append("</pre>");
        return sb.toString();
    }

    private static void appendFunDefSignature(StringBuilder sb, ASTNode funDef) {
        // Return type: first child of FUN_DEF is either VOID_KW (leaf) or an unsized type node.
        ASTNode firstChild = funDef.getFirstChildNode();
        String returnType = null;
        if (firstChild != null) {
            if (firstChild.getElementType() == StanTokenTypes.VOID_KW) {
                returnType = "void";
            } else {
                returnType = StanSignatureDatabase.typeNodeToString(firstChild);
            }
        }
        if (returnType == null) returnType = "void";

        ASTNode nameToken = StanSignatureDatabase.funDefNameToken(funDef);
        String name = nameToken != null ? nameToken.getText() : "?";

        sb.append("(");

        // Parameters
        boolean firstParam = true;
        for (ASTNode c = funDef.getFirstChildNode(); c != null; c = c.getTreeNext()) {
            if (c.getElementType() == StanElementTypes.PARAM_LIST) {
                for (ASTNode param = c.getFirstChildNode(); param != null; param = param.getTreeNext()) {
                    if (param.getElementType() == StanElementTypes.ARG_DECL) {
                        if (!firstParam) sb.append(", ");
                        firstParam = false;
                        appendArgDecl(sb, param);
                    }
                }
                break;
            }
        }
        sb.append(") =&gt; ").append(esc(returnType));
    }

    private static void appendArgDecl(StringBuilder sb, ASTNode argDecl) {
        // Optional data keyword
        boolean hasDataKw = false;
        ASTNode typeNode = null;
        for (ASTNode c = argDecl.getFirstChildNode(); c != null; c = c.getTreeNext()) {
            if (c.getElementType() == StanTokenTypes.DATA_KW) { hasDataKw = true; continue; }
            if (c.getFirstChildNode() != null) { typeNode = c; break; }
        }
        if (hasDataKw) sb.append("data ");
        String typeStr = typeNode != null ? StanSignatureDatabase.typeNodeToString(typeNode) : null;
        if (typeStr != null) sb.append(esc(typeStr)).append(" ");

        ASTNode nameToken = StanSignatureDatabase.argDeclNameToken(argDecl);
        if (nameToken != null) sb.append(esc(nameToken.getText()));
    }

    // ── AST search helpers ───────────────────────────────────────────────────

    private static @Nullable ASTNode findFunDef(ASTNode root, String name) {
        if (root.getElementType() == StanElementTypes.FUN_DEF) {
            ASTNode nameToken = StanSignatureDatabase.funDefNameToken(root);
            if (nameToken != null && nameToken.getText().equals(name)) return root;
            return null;
        }
        for (ASTNode c = root.getFirstChildNode(); c != null; c = c.getTreeNext()) {
            ASTNode found = findFunDef(c, name);
            if (found != null) return found;
        }
        return null;
    }

    // ── PSI structure predicates ─────────────────────────────────────────────

    /** True when element is the function name token of a FUN_CALL_EXPR or COND_DIST_EXPR. */
    private static boolean isFunctionNameInCall(PsiElement element, PsiElement parent, IElementType parentType) {
        if (parentType != StanElementTypes.FUN_CALL_EXPR && parentType != StanElementTypes.COND_DIST_EXPR) return false;
        ASTNode firstChild = parent.getNode().getFirstChildNode();
        return firstChild != null && firstChild.getPsi().equals(element);
    }

    /** True when element is the name token in a FUN_DEF (right before LPAREN). */
    private static boolean isFunDefNameToken(PsiElement element, PsiElement funDef) {
        ASTNode nameToken = StanSignatureDatabase.funDefNameToken(funDef.getNode());
        return nameToken != null && nameToken.getPsi().equals(element);
    }

    /** True when element is directly preceded (skipping whitespace/comments) by a TILDE token. */
    private static boolean isAfterTilde(PsiElement element) {
        PsiElement prev = element.getPrevSibling();
        while (prev != null && isTrivia(prev.getNode().getElementType())) {
            prev = prev.getPrevSibling();
        }
        return prev != null && prev.getNode().getElementType() == StanTokenTypes.TILDE;
    }

    private static boolean isTrivia(IElementType type) {
        return type == TokenType.WHITE_SPACE
                || type == StanTokenTypes.LINE_COMMENT
                || type == StanTokenTypes.BLOCK_COMMENT;
    }

    private static boolean hasUserDefinedFunDef(PsiElement element, String name) {
        PsiFile file = element.getContainingFile();
        if (file == null) return false;
        return findFunDef(file.getNode(), name) != null;
    }

    private static boolean isConditioningSuffix(String name) {
        return name.endsWith("_lpdf") || name.endsWith("_lpmf")
                || name.endsWith("_lupdf") || name.endsWith("_lupmf")
                || name.endsWith("_lcdf") || name.endsWith("_lccdf")
                || name.endsWith("_cdf");
    }

    // ── Utilities ────────────────────────────────────────────────────────────

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
