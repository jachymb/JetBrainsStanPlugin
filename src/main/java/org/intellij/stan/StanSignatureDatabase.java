package org.intellij.stan;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;
import org.intellij.stan.psi.StanTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Loads the generated Stan function signature database from stan_signatures.json
 * and exposes utilities for type strings and compatibility checks.
 *
 * JSON format: {"v":1,"f":{"name":[[[arg,...],ret],...], ...}}
 * Type strings: "int","real","complex","vector","row_vector","matrix",
 *               "complex_vector","complex_row_vector","complex_matrix",
 *               "a[T]" for arrays (nestable), "void"/null for no return.
 */
public final class StanSignatureDatabase {

    // ── Signature record ──────────────────────────────────────────────────────

    public static final class Signature {
        public final List<String> args;
        public final @Nullable String ret;

        Signature(List<String> args, @Nullable String ret) {
            this.args = Collections.unmodifiableList(args);
            this.ret = ret;
        }
    }

    // ── Singleton ─────────────────────────────────────────────────────────────

    private static volatile StanSignatureDatabase INSTANCE;

    private final Map<String, List<Signature>> functions;
    private final Set<String> distributionFunctions;

    public static StanSignatureDatabase getInstance() {
        if (INSTANCE == null) {
            synchronized (StanSignatureDatabase.class) {
                if (INSTANCE == null) INSTANCE = new StanSignatureDatabase();
            }
        }
        return INSTANCE;
    }

    private StanSignatureDatabase() {
        Map<String, List<Signature>> loaded = Collections.emptyMap();
        try (InputStream is = StanSignatureDatabase.class.getResourceAsStream("/stan_signatures.json")) {
            if (is != null) {
                String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                loaded = parseJson(json);
            }
        } catch (Exception ignored) {
        }
        functions = loaded;
        Set<String> dist = new HashSet<>();
        for (String name : functions.keySet()) {
            if (name.endsWith("_lpdf")) {
                dist.add(name);
                dist.add(name.substring(0, name.length() - 5) + "_lupdf");
            } else if (name.endsWith("_lpmf")) {
                dist.add(name);
                dist.add(name.substring(0, name.length() - 5) + "_lupmf");
            }
        }
        distributionFunctions = Collections.unmodifiableSet(dist);
    }

    // ── Public query API ──────────────────────────────────────────────────────

    public boolean hasFunction(@NotNull String name) {
        return functions.containsKey(name);
    }

    public @NotNull Set<String> getDistributionFunctionNames() {
        return distributionFunctions;
    }

    public @NotNull List<Signature> getSignatures(@NotNull String name) {
        return functions.getOrDefault(name, Collections.emptyList());
    }

    // ── JSON parser ───────────────────────────────────────────────────────────

    private static Map<String, List<Signature>> parseJson(String s) {
        JsonReader r = new JsonReader(s);
        r.skipWs();
        Map<String, Object> root = r.readObject();
        @SuppressWarnings("unchecked")
        Map<String, Object> fMap = (Map<String, Object>) root.get("f");
        if (fMap == null) return Collections.emptyMap();

        Map<String, List<Signature>> result = new HashMap<>((int)(fMap.size() * 1.4));
        for (Map.Entry<String, Object> e : fMap.entrySet()) {
            @SuppressWarnings("unchecked")
            List<Object> sigList = (List<Object>) e.getValue();
            List<Signature> sigs = new ArrayList<>(sigList.size());
            for (Object sigObj : sigList) {
                @SuppressWarnings("unchecked")
                List<Object> sig = (List<Object>) sigObj;
                @SuppressWarnings("unchecked")
                List<Object> argsRaw = (List<Object>) sig.get(0);
                List<String> args = new ArrayList<>();
                if (argsRaw != null) for (Object a : argsRaw) args.add((String) a);
                String ret = (String) sig.get(1);
                sigs.add(new Signature(args, ret));
            }
            result.put(e.getKey(), sigs);
        }
        return result;
    }

    private static final class JsonReader {
        final String s; int i;
        JsonReader(String s) { this.s = s; i = 0; }
        void skipWs() { while (i < s.length() && s.charAt(i) <= ' ') i++; }

        String readString() {
            i++; int start = i;
            while (i < s.length() && s.charAt(i) != '"') { if (s.charAt(i) == '\\') i++; i++; }
            String v = s.substring(start, i); i++; return v;
        }
        List<Object> readArray() {
            i++; List<Object> list = new ArrayList<>(); skipWs();
            while (i < s.length() && s.charAt(i) != ']') {
                list.add(readValue()); skipWs();
                if (i < s.length() && s.charAt(i) == ',') { i++; skipWs(); }
            }
            i++; return list;
        }
        Map<String, Object> readObject() {
            i++; Map<String, Object> map = new LinkedHashMap<>(); skipWs();
            while (i < s.length() && s.charAt(i) != '}') {
                String key = readString(); skipWs(); i++;
                map.put(key, readValue()); skipWs();
                if (i < s.length() && s.charAt(i) == ',') { i++; skipWs(); }
            }
            i++; return map;
        }
        Object readValue() {
            skipWs(); char c = i < s.length() ? s.charAt(i) : 0;
            if (c == '"') return readString();
            if (c == '[') return readArray();
            if (c == '{') return readObject();
            if (c == 'n') { i += 4; return null; }
            if (c == 't') { i += 4; return Boolean.TRUE; }
            if (c == 'f') { i += 5; return Boolean.FALSE; }
            int start = i;
            while (i < s.length() && ",]}".indexOf(s.charAt(i)) < 0) i++;
            return s.substring(start, i).trim();
        }
    }

    // ── Type string utilities ─────────────────────────────────────────────────

    /**
     * Convert a type AST node to the canonical type string used in the signature database.
     * Handles: var_type, top_var_type, sized_basic_type, unsized_basic_type, unsized_type,
     * or bare keyword tokens (int, real, vector, …).
     * Returns null for tuples and unrepresentable types.
     */
    public static @Nullable String typeNodeToString(@Nullable ASTNode typeNode) {
        if (typeNode == null) return null;
        IElementType t = typeNode.getElementType();

        // var_type ::= arr_dims element_type | element_type
        if (t == StanTypes.VAR_TYPE) {
            ASTNode first = typeNode.getFirstChildNode();
            if (first == null) return null;
            if (first.getElementType() == StanTypes.ARR_DIMS) {
                int dims = countCommasIn(first) + 1;
                ASTNode elemNode = first.getTreeNext();
                String inner = typeNodeToString(elemNode);
                if (inner == null) return null;
                for (int i = 0; i < dims; i++) inner = "a[" + inner + "]";
                return inner;
            }
            return typeNodeToString(first);
        }

        // sized_basic_type / unsized_basic_type: first child is a keyword token.
        if (t == StanTypes.SIZED_BASIC_TYPE || t == StanTypes.UNSIZED_BASIC_TYPE) {
            ASTNode kw = typeNode.getFirstChildNode();
            return kw != null ? kwToTypeString(kw.getElementType()) : null;
        }

        // top_var_type: first child is the leading keyword (INT, REAL, VECTOR, ORDERED …).
        if (t == StanTypes.TOP_VAR_TYPE) {
            ASTNode kw = typeNode.getFirstChildNode();
            return kw != null ? kwToTypeString(kw.getElementType()) : null;
        }

        // unsized_type ::= ARRAY unsized_dims unsized_basic_type | unsized_basic_type | …
        if (t == StanTypes.UNSIZED_TYPE) {
            ASTNode first = typeNode.getFirstChildNode();
            if (first == null) return null;
            if (first.getElementType() == StanTypes.ARRAY) {
                // Locate unsized_dims to count dimensions.
                ASTNode dimsNode = null;
                for (ASTNode c = first.getTreeNext(); c != null; c = c.getTreeNext()) {
                    if (c.getElementType() == StanTypes.UNSIZED_DIMS) { dimsNode = c; break; }
                }
                int dims = dimsNode != null ? countCommasIn(dimsNode) + 1 : 1;
                ASTNode last = typeNode.getLastChildNode();
                String inner = typeNodeToString(last);
                if (inner == null) return null;
                for (int i = 0; i < dims; i++) inner = "a[" + inner + "]";
                return inner;
            }
            return typeNodeToString(first);
        }

        // Bare keyword token (int, real, vector, …) used directly.
        return kwToTypeString(t);
    }

    private static int countCommasIn(@NotNull ASTNode node) {
        int n = 0;
        for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext())
            if (c.getElementType() == StanTypes.COMMA) n++;
        return n;
    }

    /**
     * Infer the type string for an expression node using a pre-built type map.
     * Returns null when the type cannot be determined.
     */
    public static @Nullable String inferExprType(@Nullable ASTNode expr,
                                                  @NotNull Map<String, String> typeMap) {
        if (expr == null) return null;
        IElementType t = expr.getElementType();

        // Named literal rules
        if (t == StanTypes.INT_LITERAL_EXPR)  return "int";
        if (t == StanTypes.REAL_LITERAL_EXPR) return "real";
        if (t == StanTypes.IMAG_LITERAL_EXPR) return "complex";
        // Bare literal tokens
        if (t == StanTypes.INTNUMERAL)  return "int";
        if (t == StanTypes.REALNUMERAL) return "real";
        if (t == StanTypes.IMAGNUMERAL) return "complex";

        // Variable reference: variable_expr ::= ident
        if (t == StanTypes.VARIABLE_EXPR) {
            return typeMap.get(expr.getText());
        }

        // Parenthesised expression
        if (t == StanTypes.PAREN_EXPR) {
            ASTNode lp = expr.getFirstChildNode(); // LPAREN
            return inferExprType(lp != null ? lp.getTreeNext() : null, typeMap);
        }

        // Function calls
        if (t == StanTypes.FUN_CALL_EXPR || t == StanTypes.COND_DIST_EXPR) {
            ASTNode identNode = expr.getFirstChildNode(); // ident wrapper
            if (identNode == null) return null;
            String fnName = identNode.getText();
            List<Signature> sigs = getInstance().getSignatures(fnName);
            if (sigs.isEmpty()) return null;

            List<ASTNode> argNodes = new ArrayList<>();
            collectCallArgs(expr, argNodes);
            int argCount = argNodes.size();
            List<String> argTypes = new ArrayList<>(argCount);
            for (ASTNode a : argNodes) argTypes.add(inferExprType(a, typeMap));

            if (argTypes.stream().noneMatch(s -> s != null)) return null;

            for (int pass = 0; pass < 2; pass++) {
                for (Signature sig : sigs) {
                    if (argCount > 0 && sig.args.size() != argCount) continue;
                    boolean ok = true;
                    for (int i = 0; i < argTypes.size(); i++) {
                        String actual = argTypes.get(i);
                        String expected = sig.args.get(i);
                        boolean match = pass == 0
                                ? (actual == null || expected.equals(actual))
                                : isCompatible(expected, actual);
                        if (!match) { ok = false; break; }
                    }
                    if (ok) return sig.ret;
                }
            }
            return null;
        }

        // Indexing expression
        if (t == StanTypes.INDEX_EXPR) {
            ASTNode base = expr.getFirstChildNode();
            if (base == null) return null;
            if (base.getTreeNext() == null) return inferExprType(base, typeMap);

            String baseType = inferExprType(base, typeMap);
            if (baseType == null) return null;
            int dims = 1;
            for (ASTNode c = base.getTreeNext(); c != null; c = c.getTreeNext()) {
                if (c.getElementType() == StanTypes.INDEX_LIST) { dims += countCommasIn(c); break; }
            }
            String result = baseType;
            for (int d = 0; d < dims; d++) {
                if (result.startsWith("a["))                                     result = result.substring(2, result.length() - 1);
                else if ("matrix".equals(result))                                result = "row_vector";
                else if ("complex_matrix".equals(result))                        result = "complex_row_vector";
                else if ("vector".equals(result) || "row_vector".equals(result)) result = "real";
                else if ("complex_vector".equals(result) || "complex_row_vector".equals(result)) result = "complex";
                else return null;
            }
            return result;
        }

        // Unary expression: BANG unary_expr | MINUS/PLUS unary_expr | pow_expr
        if (t == StanTypes.UNARY_EXPR) {
            ASTNode first = expr.getFirstChildNode();
            if (first == null) return null;
            IElementType ft = first.getElementType();
            if (ft == StanTypes.BANG) return "int";
            if (ft == StanTypes.MINUS || ft == StanTypes.PLUS)
                return inferExprType(first.getTreeNext(), typeMap);
            return inferExprType(first, typeMap); // pow_expr transparent
        }

        // Arithmetic binary ops
        if (t == StanTypes.ADD_EXPR || t == StanTypes.MUL_EXPR || t == StanTypes.LDIV_EXPR) {
            return binaryOpType(expr, typeMap);
        }
        if (t == StanTypes.POW_EXPR) {
            ASTNode first = expr.getFirstChildNode();
            if (first == null) return null;
            if (first.getTreeNext() == null) return inferExprType(first, typeMap);
            return binaryOpType(expr, typeMap);
        }

        // Transparent wrappers
        ASTNode first = expr.getFirstChildNode();
        if (first != null && first.getTreeNext() == null)
            return inferExprType(first, typeMap);

        return null;
    }

    private static void collectCallArgs(ASTNode callNode, List<ASTNode> result) {
        boolean inArgs = false;
        for (ASTNode c = callNode.getFirstChildNode(); c != null; c = c.getTreeNext()) {
            IElementType ct = c.getElementType();
            if (ct == StanTypes.LPAREN) { inArgs = true; continue; }
            if (ct == StanTypes.RPAREN) break;
            if (!inArgs) continue;
            if (ct == StanTypes.COMMA || ct == StanTypes.BAR) continue;
            if (c.getFirstChildNode() != null) result.add(c);
        }
    }

    private static @Nullable String binaryOpType(ASTNode expr, Map<String, String> typeMap) {
        ASTNode lhs = expr.getFirstChildNode();
        if (lhs == null) return null;
        ASTNode op = lhs.getTreeNext();
        ASTNode rhs = op != null ? op.getTreeNext() : null;
        return commonType(inferExprType(lhs, typeMap), inferExprType(rhs, typeMap));
    }

    /**
     * Build a name → type-string map for all declared variables in the file.
     */
    public static @NotNull Map<String, String> buildTypeMap(@NotNull ASTNode root) {
        Map<String, String> map = new HashMap<>();
        buildTypeMapRec(root, map);
        return map;
    }

    private static void buildTypeMapRec(ASTNode node, Map<String, String> map) {
        IElementType t = node.getElementType();

        if (t == StanTypes.VAR_DECL) {
            // var_decl ::= var_type declared_var (COMMA declared_var_extra)* SEMICOLON
            ASTNode typeNode = node.getFirstChildNode(); // var_type
            String typeStr = typeNodeToString(typeNode);
            if (typeStr != null) {
                for (ASTNode c = typeNode != null ? typeNode.getTreeNext() : null;
                     c != null; c = c.getTreeNext()) {
                    IElementType ct = c.getElementType();
                    if (ct == StanTypes.DECLARED_VAR || ct == StanTypes.DECLARED_VAR_EXTRA)
                        recordDeclaredVar(c, typeStr, map);
                }
            }

        } else if (t == StanTypes.TOP_VAR_DECL) {
            // top_var_decl ::= [arr_dims] top_var_type top_declared_var ... SEMICOLON
            String typeStr = computeTopDeclType(node, StanTypes.TOP_DECLARED_VAR,
                                                StanTypes.TOP_DECLARED_VAR_EXTRA, map);
            // recurse with already-registered names — skip registering again
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext())
                buildTypeMapRec(c, map);
            return;

        } else if (t == StanTypes.TOP_VAR_DECL_NO_ASSIGN) {
            // top_var_decl_no_assign ::= [arr_dims] top_var_type no_assign_var ... SEMICOLON
            ASTNode[] typeInfo = findTypeAndArrDims(node);
            ASTNode arrDims = typeInfo[0];
            ASTNode typeNode = typeInfo[1];
            String base = typeNodeToString(typeNode);
            if (base != null) {
                if (arrDims != null) {
                    int dims = countCommasIn(arrDims) + 1;
                    for (int i = 0; i < dims; i++) base = "a[" + base + "]";
                }
                final String typeStr = base;
                for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                    IElementType ct = c.getElementType();
                    if (ct == StanTypes.NO_ASSIGN_VAR || ct == StanTypes.NO_ASSIGN_VAR_EXTRA) {
                        // no_assign_var ::= decl_identifier
                        ASTNode di = c.getFirstChildNode();
                        if (di != null) map.put(di.getText(), typeStr);
                    }
                }
            }

        } else if (t == StanTypes.ARG_DECL) {
            // arg_decl ::= DATABLOCK? unsized_type decl_identifier
            ASTNode typeNode = null;
            String paramName = null;
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                if (c.getElementType() == StanTypes.DATABLOCK) continue;
                if (typeNode == null && c.getFirstChildNode() != null) typeNode = c;
                if (c.getElementType() == StanTypes.DECL_IDENTIFIER) { paramName = c.getText(); }
            }
            if (typeNode == null) {
                for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                    if (c.getElementType() == StanTypes.DATABLOCK) continue;
                    String s = kwToTypeString(c.getElementType());
                    if (s != null) { typeNode = c; break; }
                }
            }
            if (paramName != null && typeNode != null) {
                String ts = typeNodeToString(typeNode);
                if (ts == null) ts = kwToTypeString(typeNode.getElementType());
                if (ts != null) map.put(paramName, ts);
            }
            return;

        } else if (t == StanTypes.FOR_RANGE_STMT) {
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                if (c.getElementType() == StanTypes.IDENT) { map.put(c.getText(), "int"); break; }
            }
        }

        for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext())
            buildTypeMapRec(c, map);
    }

    /** Find optional ARR_DIMS and the TOP_VAR_TYPE node in a top-level decl. */
    private static ASTNode[] findTypeAndArrDims(ASTNode decl) {
        ASTNode arrDims = null, typeNode = null;
        for (ASTNode c = decl.getFirstChildNode(); c != null; c = c.getTreeNext()) {
            IElementType ct = c.getElementType();
            if (ct == StanTypes.ARR_DIMS) { arrDims = c; }
            else if (ct == StanTypes.TOP_VAR_TYPE || ct == StanTypes.TOP_TUPLE_TYPE) {
                typeNode = c; break;
            }
        }
        return new ASTNode[]{arrDims, typeNode};
    }

    private static @Nullable String computeTopDeclType(ASTNode decl,
                                                        IElementType declVar,
                                                        IElementType declVarExtra,
                                                        Map<String, String> map) {
        ASTNode[] typeInfo = findTypeAndArrDims(decl);
        ASTNode arrDims = typeInfo[0];
        ASTNode typeNode = typeInfo[1];
        String base = typeNodeToString(typeNode);
        if (base == null) return null;
        if (arrDims != null) {
            int dims = countCommasIn(arrDims) + 1;
            for (int i = 0; i < dims; i++) base = "a[" + base + "]";
        }
        final String typeStr = base;
        for (ASTNode c = decl.getFirstChildNode(); c != null; c = c.getTreeNext()) {
            IElementType ct = c.getElementType();
            if (ct == declVar || ct == declVarExtra) recordDeclaredVar(c, typeStr, map);
        }
        return typeStr;
    }

    private static void recordDeclaredVar(ASTNode declVar, String typeStr, Map<String, String> map) {
        // declared_var ::= decl_identifier (ASSIGN expression)?
        // decl_identifier ::= ident | reserved_word
        // Use .getText() on the first child to get the name regardless of depth.
        ASTNode first = declVar.getFirstChildNode();
        if (first != null) map.put(first.getText(), typeStr);
    }

    /** Map a type keyword token to a canonical type string. */
    public static @Nullable String kwToTypeString(IElementType kw) {
        if (kw == StanTypes.INT)            return "int";
        if (kw == StanTypes.REAL)           return "real";
        if (kw == StanTypes.COMPLEX)        return "complex";
        if (kw == StanTypes.VECTOR)         return "vector";
        if (kw == StanTypes.ROWVECTOR)      return "row_vector";
        if (kw == StanTypes.MATRIX)         return "matrix";
        if (kw == StanTypes.COMPLEXVECTOR)     return "complex_vector";
        if (kw == StanTypes.COMPLEXROWVECTOR)  return "complex_row_vector";
        if (kw == StanTypes.COMPLEXMATRIX)     return "complex_matrix";
        if (kw == StanTypes.ORDERED || kw == StanTypes.POSITIVEORDERED
         || kw == StanTypes.SIMPLEX || kw == StanTypes.UNITVECTOR
         || kw == StanTypes.SUMTOZEROVEC)   return "vector";
        if (kw == StanTypes.CHOLESKYFACTORCORR || kw == StanTypes.CHOLESKYFACTORCOV
         || kw == StanTypes.CORRMATRIX || kw == StanTypes.COVMATRIX
         || kw == StanTypes.SUMTOZEROMAT
         || kw == StanTypes.STOCHASTICCOLUMNMATRIX || kw == StanTypes.STOCHASTICROWMATRIX)
            return "matrix";
        return null;
    }

    // ── Type compatibility ────────────────────────────────────────────────────

    public static boolean isCompatible(@NotNull String expected, @Nullable String actual) {
        if (actual == null) return true;
        if (expected.equals(actual)) return true;
        if (expected.equals("real")    && actual.equals("int"))   return true;
        if (expected.equals("complex") && (actual.equals("int")  || actual.equals("real"))) return true;
        if (expected.equals("complex_vector")     && actual.equals("vector"))     return true;
        if (expected.equals("complex_row_vector") && actual.equals("row_vector")) return true;
        if (expected.equals("complex_matrix")     && actual.equals("matrix"))     return true;
        if (expected.startsWith("a[") && actual.startsWith("a[")) {
            String ei = expected.substring(2, expected.length() - 1);
            String ai = actual.substring(2, actual.length() - 1);
            return isCompatible(ei, ai);
        }
        return false;
    }

    public static @Nullable String commonType(@Nullable String t1, @Nullable String t2) {
        if (t1 == null || t2 == null) return null;
        if (t1.equals(t2)) return t1;
        if (isCompatible(t2, t1)) return t2;
        if (isCompatible(t1, t2)) return t1;
        if (t1.startsWith("a[") && t2.startsWith("a[")) {
            String c = commonType(t1.substring(2, t1.length()-1), t2.substring(2, t2.length()-1));
            return c != null ? "a[" + c + "]" : null;
        }
        if (isContainerType(t1) && isScalarType(t2)) return broadcastContainerScalar(t1, t2);
        if (isScalarType(t1) && isContainerType(t2)) return broadcastContainerScalar(t2, t1);
        return null;
    }

    private static boolean isContainerType(String t) {
        return "matrix".equals(t) || "vector".equals(t) || "row_vector".equals(t)
            || "complex_matrix".equals(t) || "complex_vector".equals(t) || "complex_row_vector".equals(t)
            || t.startsWith("a[");
    }

    private static boolean isScalarType(String t) {
        return "int".equals(t) || "real".equals(t) || "complex".equals(t);
    }

    private static String broadcastContainerScalar(String container, String scalar) {
        if ("complex".equals(scalar)) {
            if ("matrix".equals(container))     return "complex_matrix";
            if ("vector".equals(container))     return "complex_vector";
            if ("row_vector".equals(container)) return "complex_row_vector";
        }
        return container;
    }
}
