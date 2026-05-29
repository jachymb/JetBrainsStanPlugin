package org.intellij.stan;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;
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
        public final @Nullable String ret; // null == void

        Signature(List<String> args, @Nullable String ret) {
            this.args = Collections.unmodifiableList(args);
            this.ret = ret;
        }
    }

    // ── Singleton ─────────────────────────────────────────────────────────────

    private static volatile StanSignatureDatabase INSTANCE;

    private final Map<String, List<Signature>> functions; // name → overloads
    private final Set<String> distributionFunctions;     // names ending in _lpdf/_lpmf/_lcdf/_lccdf

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
                if (argsRaw != null)
                    for (Object a : argsRaw) args.add((String) a);
                String ret = (String) sig.get(1);
                sigs.add(new Signature(args, ret));
            }
            result.put(e.getKey(), sigs);
        }
        return result;
    }

    private static final class JsonReader {
        final String s;
        int i;
        JsonReader(String s) { this.s = s; i = 0; }
        void skipWs() { while (i < s.length() && s.charAt(i) <= ' ') i++; }

        String readString() {
            i++; // skip opening "
            int start = i;
            while (i < s.length() && s.charAt(i) != '"') {
                if (s.charAt(i) == '\\') i++;
                i++;
            }
            String v = s.substring(start, i);
            i++; // skip closing "
            return v;
        }

        List<Object> readArray() {
            i++; // skip [
            List<Object> list = new ArrayList<>();
            skipWs();
            while (i < s.length() && s.charAt(i) != ']') {
                list.add(readValue());
                skipWs();
                if (i < s.length() && s.charAt(i) == ',') { i++; skipWs(); }
            }
            i++; // skip ]
            return list;
        }

        Map<String, Object> readObject() {
            i++; // skip {
            Map<String, Object> map = new LinkedHashMap<>();
            skipWs();
            while (i < s.length() && s.charAt(i) != '}') {
                String key = readString();
                skipWs(); i++; // skip :
                map.put(key, readValue());
                skipWs();
                if (i < s.length() && s.charAt(i) == ',') { i++; skipWs(); }
            }
            i++; // skip }
            return map;
        }

        Object readValue() {
            skipWs();
            char c = i < s.length() ? s.charAt(i) : 0;
            if (c == '"') return readString();
            if (c == '[') return readArray();
            if (c == '{') return readObject();
            if (c == 'n') { i += 4; return null; } // null
            if (c == 't') { i += 4; return Boolean.TRUE; }
            if (c == 'f') { i += 5; return Boolean.FALSE; }
            // number
            int start = i;
            while (i < s.length() && ",]}".indexOf(s.charAt(i)) < 0) i++;
            String num = s.substring(start, i).trim();
            return num;
        }
    }

    // ── Type string utilities (shared by multiple inspections) ────────────────

    /**
     * Convert a parsed type AST node (INT_TYPE, MATRIX_TYPE, ARRAY_TYPE, etc.)
     * to the canonical type string used in the signature database.
     * Returns null if the type cannot be represented (tuples, unsized types).
     */
    public static @Nullable String typeNodeToString(@Nullable ASTNode typeNode) {
        if (typeNode == null) return null;
        IElementType t = typeNode.getElementType();

        if (t == StanElementTypes.INT_TYPE)     return "int";
        if (t == StanElementTypes.REAL_TYPE)    return "real";
        if (t == StanElementTypes.COMPLEX_TYPE) return "complex";

        if (t == StanElementTypes.VECTOR_TYPE
         || t == StanElementTypes.ORDERED_TYPE
         || t == StanElementTypes.POSITIVE_ORDERED_TYPE
         || t == StanElementTypes.SIMPLEX_TYPE
         || t == StanElementTypes.UNIT_VECTOR_TYPE
         || t == StanElementTypes.SUM_TO_ZERO_VECTOR_TYPE) return "vector";

        if (t == StanElementTypes.ROW_VECTOR_TYPE) return "row_vector";

        if (t == StanElementTypes.MATRIX_TYPE
         || t == StanElementTypes.SUM_TO_ZERO_MATRIX_TYPE
         || t == StanElementTypes.CHOLESKY_FACTOR_CORR_TYPE
         || t == StanElementTypes.CHOLESKY_FACTOR_COV_TYPE
         || t == StanElementTypes.CORR_MATRIX_TYPE
         || t == StanElementTypes.COV_MATRIX_TYPE
         || t == StanElementTypes.COLUMN_STOCHASTIC_MATRIX_TYPE
         || t == StanElementTypes.ROW_STOCHASTIC_MATRIX_TYPE) return "matrix";

        if (t == StanElementTypes.COMPLEX_VECTOR_TYPE)     return "complex_vector";
        if (t == StanElementTypes.COMPLEX_ROW_VECTOR_TYPE) return "complex_row_vector";
        if (t == StanElementTypes.COMPLEX_MATRIX_TYPE)     return "complex_matrix";

        if (t == StanElementTypes.ARRAY_TYPE) {
            // Count top-level COMMA children to determine the number of dimensions.
            // array[n, m] int has 1 comma → 2 dimensions → "a[a[int]]".
            // Commas inside dimension expressions are inside composite children and not counted.
            int dims = 1;
            ASTNode inner = typeNode.getLastChildNode();
            for (ASTNode c = typeNode.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                if (c.getElementType() == StanTokenTypes.COMMA) dims++;
            }
            // Inner element type is the last composite child (after the closing bracket)
            while (inner != null && inner.getFirstChildNode() == null) {
                inner = inner.getTreePrev();
            }
            String innerStr = typeNodeToString(inner);
            if (innerStr == null) return null;
            for (int i = 0; i < dims; i++) innerStr = "a[" + innerStr + "]";
            return innerStr;
        }
        if (t == StanElementTypes.UNSIZED_ARRAY_TYPE) {
            // Children: ARRAY_KW, UNSIZED_DIMS ([,] etc.), then element type.
            // Count commas in UNSIZED_DIMS to get the number of dimensions.
            int dims = 1;
            for (ASTNode c = typeNode.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                if (c.getElementType() == StanElementTypes.UNSIZED_DIMS) {
                    for (ASTNode cc = c.getFirstChildNode(); cc != null; cc = cc.getTreeNext())
                        if (cc.getElementType() == StanTokenTypes.COMMA) dims++;
                    break;
                }
            }
            ASTNode last = typeNode.getLastChildNode();
            if (last == null) return null;
            String innerStr = last.getFirstChildNode() != null
                    ? typeNodeToString(last)
                    : kwToTypeString(last.getElementType());
            if (innerStr == null) return null;
            for (int i = 0; i < dims; i++) innerStr = "a[" + innerStr + "]";
            return innerStr;
        }
        return null; // tuple — skip
    }

    /**
     * Infer the type string for an expression node, using a pre-built type map
     * from the surrounding scope.  Returns null when type cannot be determined
     * (which causes callers to skip the check rather than produce false positives).
     */
    public static @Nullable String inferExprType(@Nullable ASTNode expr,
                                                  @NotNull Map<String, String> typeMap) {
        if (expr == null) return null;
        IElementType t = expr.getElementType();

        if (t == StanElementTypes.INT_LITERAL_EXPR)  return "int";
        if (t == StanElementTypes.REAL_LITERAL_EXPR) return "real";
        if (t == StanElementTypes.IMAG_LITERAL_EXPR) return "complex";

        if (t == StanElementTypes.VARIABLE_EXPR) {
            ASTNode name = expr.getFirstChildNode();
            if (name != null && name.getElementType() == StanTokenTypes.IDENTIFIER)
                return typeMap.get(name.getText());
            return null;
        }

        if (t == StanElementTypes.PAREN_EXPR) {
            ASTNode inner = expr.getFirstChildNode(); // LPAREN
            if (inner != null) inner = inner.getTreeNext();
            return inferExprType(inner, typeMap);
        }

        if (t == StanElementTypes.PREFIX_OP_EXPR) {
            // unary -/+ preserves numeric type
            ASTNode op = expr.getFirstChildNode();
            return inferExprType(op != null ? op.getTreeNext() : null, typeMap);
        }

        if (t == StanElementTypes.INDEXED_EXPR) {
            // First child is the base expression; strip one indexing dimension per index.
            ASTNode base = expr.getFirstChildNode();
            String baseType = inferExprType(base, typeMap);
            if (baseType == null) return null;
            // Count indices: 1 + number of top-level COMMA tokens in INDEX_LIST.
            int dims = 1;
            for (ASTNode c = expr.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                if (c.getElementType() == StanElementTypes.INDEX_LIST) {
                    for (ASTNode ic = c.getFirstChildNode(); ic != null; ic = ic.getTreeNext())
                        if (ic.getElementType() == StanTokenTypes.COMMA) dims++;
                    break;
                }
            }
            String result = baseType;
            for (int d = 0; d < dims; d++) {
                if (result.startsWith("a["))                                    result = result.substring(2, result.length() - 1);
                else if ("matrix".equals(result))                               result = "row_vector";
                else if ("complex_matrix".equals(result))                       result = "complex_row_vector";
                else if ("vector".equals(result) || "row_vector".equals(result)) result = "real";
                else if ("complex_vector".equals(result) || "complex_row_vector".equals(result)) result = "complex";
                else return null;
            }
            return result;
        }

        if (t == StanElementTypes.FUN_CALL_EXPR || t == StanElementTypes.COND_DIST_EXPR) {
            // Look up the return type of the called function from the database.
            // COND_DIST_EXPR (bar notation) is treated identically: same name, same return type.
            ASTNode nameNode = expr.getFirstChildNode();
            if (nameNode != null && (nameNode.getElementType() == StanTokenTypes.BUILTIN_FUNCTION
                                  || nameNode.getElementType() == StanTokenTypes.IDENTIFIER)) {
                String fnName = nameNode.getText();
                List<Signature> sigs = getInstance().getSignatures(fnName);
                if (!sigs.isEmpty()) {
                    // Collect arg nodes and infer their types across both ARG_LISTs (before and after |).
                    List<ASTNode> argNodes = new ArrayList<>();
                    for (ASTNode c = expr.getFirstChildNode(); c != null; c = c.getTreeNext())
                        if (c.getElementType() == StanElementTypes.ARG_LIST)
                            for (ASTNode a = c.getFirstChildNode(); a != null; a = a.getTreeNext())
                                if (a.getFirstChildNode() != null) argNodes.add(a);
                    int argCount = argNodes.size();
                    List<String> argTypes = new ArrayList<>(argCount);
                    for (ASTNode a : argNodes)
                        argTypes.add(inferExprType(a, typeMap));

                    // If all arg types are unknown we can't determine the return type.
                    // Picking the first matching signature would be arbitrary and cause false positives.
                    boolean anyKnown = argTypes.stream().anyMatch(s -> s != null);
                    if (!anyKnown) return null;

                    // Find the best matching signature for arity and known arg types.
                    // Two passes: exact match first, then promotion-compatible match.
                    // This avoids picking (complex)->complex over (real)->real for a real arg,
                    // and avoids picking deeply-vectorized signatures for scalar args.
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
                }
            }
            return null;
        }

        if (t == StanElementTypes.BINARY_OP_EXPR) {
            // Use common_type promotion on the two operands
            ASTNode lhsNode = expr.getFirstChildNode();
            ASTNode rhsNode = lhsNode != null ? lhsNode.getTreeNext() : null; // op token
            if (rhsNode != null) rhsNode = rhsNode.getTreeNext(); // rhs
            String lhs = inferExprType(lhsNode, typeMap);
            String rhs = inferExprType(rhsNode, typeMap);
            return commonType(lhs, rhs);
        }

        return null;
    }

    private static @Nullable ASTNode findArgList(ASTNode funCall) {
        for (ASTNode c = funCall.getFirstChildNode(); c != null; c = c.getTreeNext()) {
            if (c.getElementType() == StanElementTypes.ARG_LIST) return c;
        }
        return null;
    }

    private static int countArgListExprs(ASTNode argList) {
        int count = 0;
        for (ASTNode c = argList.getFirstChildNode(); c != null; c = c.getTreeNext()) {
            if (c.getFirstChildNode() != null) count++; // composite node = an expression
        }
        return count;
    }

    /**
     * Build a name → type-string map for all variables declared in the file.
     * Covers VAR_DECL, ARG_DECL, and for-range loop variables.
     */
    public static @NotNull Map<String, String> buildTypeMap(@NotNull ASTNode root) {
        Map<String, String> map = new HashMap<>();
        buildTypeMapRec(root, map);
        return map;
    }

    private static void buildTypeMapRec(ASTNode node, Map<String, String> map) {
        IElementType t = node.getElementType();

        if (t == StanElementTypes.VAR_DECL) {
            ASTNode typeNode = node.getFirstChildNode();
            String typeStr = typeNodeToString(typeNode);
            if (typeStr != null) {
                for (ASTNode c = typeNode.getTreeNext(); c != null; c = c.getTreeNext()) {
                    if (c.getElementType() == StanElementTypes.DECLARED_VAR) {
                        ASTNode name = c.getFirstChildNode();
                        if (name != null && name.getElementType() == StanTokenTypes.IDENTIFIER)
                            map.put(name.getText(), typeStr);
                    }
                }
            }

        } else if (t == StanElementTypes.ARG_DECL) {
            // type node is first composite child, name is the IDENTIFIER after it
            ASTNode typeNode = null;
            String paramName = null;
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                if (c.getElementType() == StanTokenTypes.DATA_KW) continue;
                if (typeNode == null && c.getFirstChildNode() != null) {
                    typeNode = c;
                } else if (c.getElementType() == StanTokenTypes.IDENTIFIER
                        || c.getElementType() == StanTokenTypes.BUILTIN_FUNCTION) {
                    paramName = c.getText();
                }
            }
            // For basic unsized types (e.g. "real foo"), the type is a bare keyword token
            if (typeNode == null) {
                for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                    if (c.getElementType() == StanTokenTypes.DATA_KW) continue;
                    String s = kwToTypeString(c.getElementType());
                    if (s != null) { typeNode = c; break; }
                }
            }
            if (typeNode != null && paramName != null) {
                String ts = typeNodeToString(typeNode);
                if (ts == null) ts = kwToTypeString(typeNode.getElementType());
                if (ts != null) map.put(paramName, ts);
            }
            return; // no nested declarations inside ARG_DECL

        } else if (t == StanElementTypes.FOR_RANGE_STMT) {
            // loop variable is always int
            for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                if (c.getElementType() == StanTokenTypes.IDENTIFIER) {
                    map.put(c.getText(), "int");
                    break;
                }
            }
        }

        for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext())
            buildTypeMapRec(c, map);
    }

    private static @Nullable String kwToTypeString(IElementType kw) {
        if (kw == StanTokenTypes.INT_KW)              return "int";
        if (kw == StanTokenTypes.REAL_KW)             return "real";
        if (kw == StanTokenTypes.COMPLEX_KW)          return "complex";
        if (kw == StanTokenTypes.VECTOR_KW)           return "vector";
        if (kw == StanTokenTypes.ROW_VECTOR_KW)       return "row_vector";
        if (kw == StanTokenTypes.MATRIX_KW)           return "matrix";
        if (kw == StanTokenTypes.COMPLEX_VECTOR_KW)     return "complex_vector";
        if (kw == StanTokenTypes.COMPLEX_ROW_VECTOR_KW) return "complex_row_vector";
        if (kw == StanTokenTypes.COMPLEX_MATRIX_KW)     return "complex_matrix";
        return null;
    }

    // ── Type compatibility ────────────────────────────────────────────────────

    /**
     * True when a value of type {@code actual} can be used where {@code expected} is required.
     * Implements the promotion rules from stanc3's UnsizedType.common_type.
     * null actual → true (unknown, skip the check).
     */
    public static boolean isCompatible(@NotNull String expected, @Nullable String actual) {
        if (actual == null) return true;
        if (expected.equals(actual)) return true;
        // scalar promotions: int → real → complex
        if (expected.equals("real")    && actual.equals("int"))   return true;
        if (expected.equals("complex") && (actual.equals("int")  || actual.equals("real"))) return true;
        // container promotions: real variant → complex variant
        if (expected.equals("complex_vector")     && actual.equals("vector"))     return true;
        if (expected.equals("complex_row_vector") && actual.equals("row_vector")) return true;
        if (expected.equals("complex_matrix")     && actual.equals("matrix"))     return true;
        // array: element-wise
        if (expected.startsWith("a[") && actual.startsWith("a[")) {
            String ei = expected.substring(2, expected.length() - 1);
            String ai = actual.substring(2, actual.length() - 1);
            return isCompatible(ei, ai);
        }
        return false;
    }

    /**
     * Minimal type that both t1 and t2 can promote to (mirrors stanc3's common_type).
     * Returns null if no common type exists.
     */
    public static @Nullable String commonType(@Nullable String t1, @Nullable String t2) {
        if (t1 == null || t2 == null) return null;
        if (t1.equals(t2)) return t1;
        if (isCompatible(t2, t1)) return t2;
        if (isCompatible(t1, t2)) return t1;
        // array element-wise
        if (t1.startsWith("a[") && t2.startsWith("a[")) {
            String c = commonType(t1.substring(2, t1.length()-1), t2.substring(2, t2.length()-1));
            return c != null ? "a[" + c + "]" : null;
        }
        // Broadcasting: container op scalar (e.g. matrix + int → matrix, vector + complex → complex_vector)
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
        return container; // int or real scalar preserves the container type
    }
}
