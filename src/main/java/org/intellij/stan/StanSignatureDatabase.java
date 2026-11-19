package org.intellij.stan;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Loads the generated Stan function signature database from stan_signatures.json
 * and exposes utilities for type strings and compatibility checks.
 *
 * JSON format: {"v":2,"f":{"name":[[[arg,...],ret],...], ...}}
 * Type strings: "int","real","complex","vector","row_vector","matrix",
 *               "complex_vector","complex_row_vector","complex_matrix",
 *               "array[commas] T" for arrays (e.g. "array[,] int" = 2D int array),
 *               "fun" for higher-order (UFun/UMathLibraryFunction) parameters,
 *               "void"/null for no return.
 * ret field: a JSON string for ordinary returns, a JSON array of strings for
 *            tuple returns (qr, svd, eigendecompose, …), "void" for void.
 */
public final class StanSignatureDatabase {

    // ── Signature record ──────────────────────────────────────────────────────

    public static final class Signature {
        public final List<String> args;
        /** Scalar/container/void return type. null when the function returns a tuple. */
        public final @Nullable String ret;
        /**
         * Non-null for tuple-returning functions (qr, svd, eigendecompose, …).
         * Each element is the type of one tuple component in declaration order.
         * When non-null, {@link #ret} is null; {@code inferExprType} returns null
         * (the call cannot be used as a single-typed expression).
         */
        public final @Nullable List<String> retTuple;

        Signature(List<String> args, @Nullable String ret, @Nullable List<String> retTuple) {
            this.args = Collections.unmodifiableList(args);
            this.ret = ret;
            this.retTuple = retTuple != null ? Collections.unmodifiableList(retTuple) : null;
        }
    }

    // ── Singleton ─────────────────────────────────────────────────────────────

    /** Legacy names kept in {@link #getDistributionBaseNames()} although no _lpdf/_lpmf exists for them. */
    private static final Set<String> LEGACY_DISTRIBUTION_BASE_NAMES = Set.of(
            "hmm_latent",
            "integrate_ode", "integrate_ode_rk45", "integrate_ode_bdf", "integrate_ode_adams",
            "get_lp", "increment_log_prob");

    private static volatile StanSignatureDatabase INSTANCE;

    private final Map<String, List<Signature>> functions; // name → overloads
    private final Set<String> distributionFunctions;     // names ending in _lpdf/_lpmf/_lupdf/_lupmf
    private final Set<String> distributionBaseNames;     // tilde-statement stems, e.g. "normal"

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
        Set<String> bases = new HashSet<>(LEGACY_DISTRIBUTION_BASE_NAMES);
        for (String name : functions.keySet()) {
            if (name.endsWith("_lpdf") || name.endsWith("_lpmf")) {
                dist.add(name);
                dist.add(unnormalizedName(name));
                bases.add(name.substring(0, name.length() - 5));
            }
        }
        distributionFunctions = Collections.unmodifiableSet(dist);
        distributionBaseNames = Collections.unmodifiableSet(bases);
    }

    /** foo_lpdf → foo_lupdf, foo_lpmf → foo_lupmf; other names unchanged. */
    private static String unnormalizedName(String name) {
        return name.replaceFirst("_l(p[dm]f)$", "_lu$1");
    }

    // ── AST utilities (shared by inspections and completion) ─────────────────

    /**
     * Returns the name token (IDENTIFIER) of a FUN_DEF node,
     * i.e. the first IDENTIFIER child before the LPAREN.  Returns null on malformed nodes.
     */
    public static @Nullable ASTNode funDefNameToken(@NotNull ASTNode funDef) {
        for (ASTNode c = funDef.getFirstChildNode(); c != null; c = c.getTreeNext()) {
            if (c.getElementType() == StanTokenTypes.LPAREN) break;
            IElementType t = c.getElementType();
            if (t == StanTokenTypes.IDENTIFIER || t == StanTokenTypes.BUILTIN_FUNCTION) return c;
        }
        return null;
    }

    /**
     * Returns the name token of an ARG_DECL node: the IDENTIFIER that follows the type node
     * (and optional DATA_KW).  Returns null on malformed nodes.
     */
    public static @Nullable ASTNode argDeclNameToken(@NotNull ASTNode argDecl) {
        boolean typeFound = false;
        for (ASTNode c = argDecl.getFirstChildNode(); c != null; c = c.getTreeNext()) {
            IElementType et = c.getElementType();
            if (et == StanTokenTypes.DATA_KW) continue;
            if (!typeFound) { typeFound = true; continue; }
            if (et == StanTokenTypes.IDENTIFIER || et == StanTokenTypes.BUILTIN_FUNCTION) return c;
        }
        return null;
    }

    /**
     * Collects the names of all user-defined functions in the given AST subtree, plus the
     * _lupdf/_lupmf names under which user _lpdf/_lpmf functions can also be called.
     * Used by inspections and completion to distinguish user-defined from built-in functions.
     */
    public static @NotNull Set<String> collectUserDefinedFunctionNames(@NotNull ASTNode root) {
        Set<String> names = new HashSet<>();
        collectUserDefinedFunctionNamesRec(root, names);
        return names;
    }

    private static void collectUserDefinedFunctionNamesRec(ASTNode node, Set<String> names) {
        if (node.getElementType() == StanElementTypes.FUN_DEF) {
            ASTNode nameToken = funDefNameToken(node);
            if (nameToken != null) {
                names.add(nameToken.getText());
                names.add(unnormalizedName(nameToken.getText()));
            }
            return; // no nested function definitions in Stan
        }
        for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext())
            collectUserDefinedFunctionNamesRec(c, names);
    }

    // ── Public query API ──────────────────────────────────────────────────────

    public boolean hasFunction(@NotNull String name) {
        return functions.containsKey(name);
    }

    public @NotNull Set<String> getDistributionFunctionNames() {
        return distributionFunctions;
    }

    /** Distribution stems usable in tilde statements ({@code y ~ normal(...)}), derived from the DB. */
    public @NotNull Set<String> getDistributionBaseNames() {
        return distributionBaseNames;
    }

    public @NotNull List<Signature> getSignatures(@NotNull String name) {
        return functions.getOrDefault(name, Collections.emptyList());
    }

    public @NotNull Set<String> getFunctionNames() {
        return Collections.unmodifiableSet(functions.keySet());
    }

    /**
     * True if {@code name} is any kind of built-in identifier: a function in the DB,
     * a distribution suffix variant (_lupdf/_lupmf), or a distribution family base name
     * used in tilde statements.
     */
    public boolean isBuiltinName(@NotNull String name) {
        return functions.containsKey(name)
                || distributionFunctions.contains(name)
                || distributionBaseNames.contains(name);
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

                // ret may be a JSON string (ordinary type / "void") or a JSON array (UTuple).
                Object retRaw = sig.get(1);
                String ret = null;
                List<String> retTuple = null;
                if (retRaw instanceof String) {
                    ret = (String) retRaw;
                } else if (retRaw instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> tupleRaw = (List<Object>) retRaw;
                    retTuple = new ArrayList<>(tupleRaw.size());
                    for (Object t : tupleRaw) retTuple.add((String) t);
                }
                sigs.add(new Signature(args, ret, retTuple));
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

    private static final Map<IElementType, String> SIMPLE_TYPE_STRINGS;
    private static final Map<IElementType, String> KW_TYPE_STRINGS;
    static {
        Map<IElementType, String> ts = new HashMap<>();
        ts.put(StanElementTypes.INT_TYPE,     "int");
        ts.put(StanElementTypes.REAL_TYPE,    "real");
        ts.put(StanElementTypes.COMPLEX_TYPE, "complex");
        ts.put(StanElementTypes.VECTOR_TYPE,             "vector");
        ts.put(StanElementTypes.ORDERED_TYPE,            "vector");
        ts.put(StanElementTypes.POSITIVE_ORDERED_TYPE,   "vector");
        ts.put(StanElementTypes.SIMPLEX_TYPE,            "vector");
        ts.put(StanElementTypes.UNIT_VECTOR_TYPE,        "vector");
        ts.put(StanElementTypes.SUM_TO_ZERO_VECTOR_TYPE, "vector");
        ts.put(StanElementTypes.ROW_VECTOR_TYPE,                "row_vector");
        ts.put(StanElementTypes.MATRIX_TYPE,                    "matrix");
        ts.put(StanElementTypes.SUM_TO_ZERO_MATRIX_TYPE,        "matrix");
        ts.put(StanElementTypes.CHOLESKY_FACTOR_CORR_TYPE,      "matrix");
        ts.put(StanElementTypes.CHOLESKY_FACTOR_COV_TYPE,       "matrix");
        ts.put(StanElementTypes.CORR_MATRIX_TYPE,               "matrix");
        ts.put(StanElementTypes.COV_MATRIX_TYPE,                "matrix");
        ts.put(StanElementTypes.COLUMN_STOCHASTIC_MATRIX_TYPE,  "matrix");
        ts.put(StanElementTypes.ROW_STOCHASTIC_MATRIX_TYPE,     "matrix");
        ts.put(StanElementTypes.COMPLEX_VECTOR_TYPE,     "complex_vector");
        ts.put(StanElementTypes.COMPLEX_ROW_VECTOR_TYPE, "complex_row_vector");
        ts.put(StanElementTypes.COMPLEX_MATRIX_TYPE,     "complex_matrix");
        SIMPLE_TYPE_STRINGS = Collections.unmodifiableMap(ts);

        Map<IElementType, String> kt = new HashMap<>();
        kt.put(StanTokenTypes.INT_KW,               "int");
        kt.put(StanTokenTypes.REAL_KW,              "real");
        kt.put(StanTokenTypes.COMPLEX_KW,           "complex");
        kt.put(StanTokenTypes.VECTOR_KW,            "vector");
        kt.put(StanTokenTypes.ROW_VECTOR_KW,        "row_vector");
        kt.put(StanTokenTypes.MATRIX_KW,            "matrix");
        kt.put(StanTokenTypes.COMPLEX_VECTOR_KW,     "complex_vector");
        kt.put(StanTokenTypes.COMPLEX_ROW_VECTOR_KW, "complex_row_vector");
        kt.put(StanTokenTypes.COMPLEX_MATRIX_KW,     "complex_matrix");
        KW_TYPE_STRINGS = Collections.unmodifiableMap(kt);
    }

    /**
     * Convert a parsed type AST node (INT_TYPE, MATRIX_TYPE, ARRAY_TYPE, etc.)
     * to the canonical type string used in the signature database.
     * Returns null if the type cannot be represented (tuples, unsized types).
     */
    public static @Nullable String typeNodeToString(@Nullable ASTNode typeNode) {
        if (typeNode == null) return null;
        IElementType t = typeNode.getElementType();

        String simple = SIMPLE_TYPE_STRINGS.get(t);
        if (simple != null) return simple;

        // Leaf keyword type token (e.g. INT_KW, REAL_KW used directly without a wrapper node).
        String kw = KW_TYPE_STRINGS.get(t);
        if (kw != null) return kw;

        if (t == StanElementTypes.ARRAY_TYPE) {
            // Count top-level COMMA children to determine the number of dimensions.
            // array[n, m] int has 1 comma → 2 dimensions → "array[,] int".
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
            return wrapArrayDims(innerStr, dims);
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
            return wrapArrayDims(innerStr, dims);
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
            String baseType = inferExprType(expr.getFirstChildNode(), typeMap);
            ASTNode indexList = expr.findChildByType(StanElementTypes.INDEX_LIST);
            return baseType == null || indexList == null ? null : applyIndices(baseType, indexList, typeMap);
        }

        if (t == StanElementTypes.FUN_CALL_EXPR || t == StanElementTypes.COND_DIST_EXPR) {
            // Look up the return type of the called function from the database.
            // COND_DIST_EXPR (bar notation) is treated identically: same name, same return type.
            ASTNode nameNode = expr.getFirstChildNode();
            if (nameNode != null && (nameNode.getElementType() == StanTokenTypes.IDENTIFIER
                                  || nameNode.getElementType() == StanTokenTypes.BUILTIN_FUNCTION)) {
                String fnName = nameNode.getText();
                List<Signature> sigs = getInstance().getSignatures(fnName);
                if (!sigs.isEmpty()) {
                    List<ASTNode> argNodes = collectArgExprs(expr);
                    int argCount = argNodes.size();
                    List<String> argTypes = new ArrayList<>(argCount);
                    for (ASTNode a : argNodes)
                        argTypes.add(inferExprType(a, typeMap));

                    // If all arg types are unknown we can't determine the return type.
                    // Picking the first matching signature would be arbitrary and cause false positives.
                    boolean anyKnown = argTypes.stream().anyMatch(Objects::nonNull);
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
                            if (ok) return sig.retTuple != null ? null : sig.ret;
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

    private static final TokenSet MULTI_INDEXES = TokenSet.create(StanElementTypes.ALL_INDEX,
            StanElementTypes.UPFROM_INDEX, StanElementTypes.DOWNFROM_INDEX, StanElementTypes.BETWEEN_INDEX);

    /**
     * Type of a {@code type} value indexed by {@code indexList}, or null when unknown.
     * A single int index drops a dimension; a multi-index (':', a range, or array[] int) keeps it.
     */
    private static @Nullable String applyIndices(String type, ASTNode indexList, Map<String, String> typeMap) {
        int keptArrayDims = 0;
        boolean columnIndexPending = false; // matrix rows were multi-indexed; the next index selects columns
        for (ASTNode idx = indexList.getFirstChildNode(); idx != null; idx = idx.getTreeNext()) {
            boolean single;
            if (idx.getElementType() == StanElementTypes.SINGLE_INDEX) {
                String indexType = inferExprType(idx.getFirstChildNode(), typeMap);
                if ("int".equals(indexType)) single = true;
                else if ("array[] int".equals(indexType)) single = false;
                else return null;
            } else if (MULTI_INDEXES.contains(idx.getElementType())) {
                single = false;
            } else {
                continue; // comma or whitespace
            }

            boolean complex = type.startsWith("complex");
            if (type.startsWith("array[")) {
                type = stripOneArrayDim(type);
                if (!single) keptArrayDims++;
            } else if (columnIndexPending) {
                columnIndexPending = false;
                if (single) type = complex ? "complex_vector" : "vector";
            } else if (type.endsWith("matrix")) {
                if (single) type = complex ? "complex_row_vector" : "row_vector";
                else columnIndexPending = true;
            } else if (type.endsWith("vector")) {
                if (single) type = complex ? "complex" : "real";
            } else {
                return null; // scalars cannot be indexed
            }
        }
        return keptArrayDims == 0 ? type : wrapArrayDims(type, keptArrayDims);
    }

    /** Collect all expression nodes from the ARG_LIST children of a call node. */
    public static @NotNull List<ASTNode> collectArgExprs(@NotNull ASTNode callNode) {
        List<ASTNode> result = new ArrayList<>();
        for (ASTNode child = callNode.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            if (child.getElementType() == StanElementTypes.ARG_LIST) {
                for (ASTNode c = child.getFirstChildNode(); c != null; c = c.getTreeNext()) {
                    if (c.getFirstChildNode() != null)
                        result.add(c);
                }
            }
        }
        return result;
    }

    private static final TokenSet GLOBAL_BLOCKS = TokenSet.create(StanElementTypes.DATA_BLOCK,
            StanElementTypes.TRANSFORMED_DATA_BLOCK, StanElementTypes.PARAMETERS_BLOCK,
            StanElementTypes.TRANSFORMED_PARAMETERS_BLOCK);

    /**
     * Name → type map (null type = unknown) of the variables visible inside {@code scope},
     * a FUN_DEF or a top-level block. A function sees its parameters and locals; a block sees
     * the preceding data/parameters blocks (Stan's waterfall) plus its own declarations.
     * ponytail: flat per scope, so sibling local scopes reusing a name with different types
     * collide; track nested scopes if that ever matters.
     */
    public static @NotNull Map<String, String> buildScopeTypeMap(@NotNull ASTNode scope) {
        Map<String, String> map = new HashMap<>();
        if (scope.getElementType() != StanElementTypes.FUN_DEF) {
            for (ASTNode b = scope.getTreeParent().getFirstChildNode(); b != scope; b = b.getTreeNext())
                if (GLOBAL_BLOCKS.contains(b.getElementType())) buildTypeMapRec(b, map);
        }
        for (ASTNode c = scope.getFirstChildNode(); c != null; c = c.getTreeNext())
            buildTypeMapRec(c, map);
        return map;
    }

    /** {@link #buildScopeTypeMap} of the FUN_DEF or top-level block enclosing {@code node}. */
    public static @NotNull Map<String, String> typeMapAt(@NotNull ASTNode node) {
        for (ASTNode n = node; n != null; n = n.getTreeParent()) {
            IElementType t = n.getElementType();
            if (t == StanElementTypes.FUN_DEF || StanElementTypes.BLOCK_TYPES.contains(t))
                return buildScopeTypeMap(n);
        }
        return new HashMap<>();
    }

    /**
     * Build a type map containing only the formal parameters of a single function definition
     * (no body locals; see {@link #buildScopeTypeMap} for the full function scope).
     */
    public static @NotNull Map<String, String> buildFunctionParamMap(@NotNull ASTNode funDefNode) {
        Map<String, String> map = new HashMap<>();
        for (ASTNode c = funDefNode.getFirstChildNode(); c != null; c = c.getTreeNext()) {
            if (c.getElementType() == StanElementTypes.PARAM_LIST) {
                for (ASTNode param = c.getFirstChildNode(); param != null; param = param.getTreeNext()) {
                    if (param.getElementType() == StanElementTypes.ARG_DECL)
                        processArgDecl(param, map);
                }
                break;
            }
        }
        return map;
    }

    private static void processArgDecl(ASTNode argDecl, Map<String, String> map) {
        ASTNode nameToken = argDeclNameToken(argDecl);
        if (nameToken == null) return;
        for (ASTNode c = argDecl.getFirstChildNode(); c != nameToken; c = c.getTreeNext()) {
            if (c.getElementType() == StanTokenTypes.DATA_KW) continue;
            String typeStr = c.getFirstChildNode() != null ? typeNodeToString(c) : kwToTypeString(c.getElementType());
            if (typeStr != null) { map.put(nameToken.getText(), typeStr); return; }
        }
    }

    private static void buildTypeMapRec(ASTNode node, Map<String, String> map) {
        IElementType t = node.getElementType();

        // Function definitions use their own isolated scope; skip entirely so that
        // function parameter names do not bleed into the file-level type map.
        if (t == StanElementTypes.FUN_DEF) return;

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
            processArgDecl(node, map);
            return; // no nested declarations inside ARG_DECL

        } else if (t == StanElementTypes.FOR_RANGE_STMT || t == StanElementTypes.FOR_EACH_STMT) {
            // range loop variable is always int; for-each element type is not inferred
            ASTNode loopVar = node.findChildByType(StanTokenTypes.IDENTIFIER);
            if (loopVar != null) map.put(loopVar.getText(), t == StanElementTypes.FOR_RANGE_STMT ? "int" : null);
        }

        for (ASTNode c = node.getFirstChildNode(); c != null; c = c.getTreeNext())
            buildTypeMapRec(c, map);
    }

    private static @Nullable String kwToTypeString(IElementType kw) {
        return KW_TYPE_STRINGS.get(kw);
    }

    // ── Type compatibility ────────────────────────────────────────────────────

    /**
     * True when a value of type {@code actual} can be used where {@code expected} is required.
     * Implements the promotion rules from stanc3's UnsizedType.common_type.
     * null actual → true (unknown, skip the check).
     */
    public static boolean isCompatible(@NotNull String expected, @Nullable String actual) {
        if (actual == null) return true;
        if ("fun".equals(expected)) return true; // higher-order arg: accept any function ref
        if (expected.equals(actual)) return true;
        // scalar promotions: int → real → complex
        if (expected.equals("real")    && actual.equals("int"))   return true;
        if (expected.equals("complex") && (actual.equals("int")  || actual.equals("real"))) return true;
        // container promotions: real variant → complex variant
        if (expected.equals("complex_vector")     && actual.equals("vector"))     return true;
        if (expected.equals("complex_row_vector") && actual.equals("row_vector")) return true;
        if (expected.equals("complex_matrix")     && actual.equals("matrix"))     return true;
        // array: element-wise (strip one dim and recurse so dimension count is checked implicitly)
        if (expected.startsWith("array[") && actual.startsWith("array["))
            return isCompatible(stripOneArrayDim(expected), stripOneArrayDim(actual));
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
        if (t1.startsWith("array[") && t2.startsWith("array[")) {
            String c = commonType(stripOneArrayDim(t1), stripOneArrayDim(t2));
            return c != null ? rewrapOneArrayDim(t1, c) : null;
        }
        // Broadcasting: container op scalar (e.g. matrix + int → matrix, vector + complex → complex_vector)
        if (isContainerType(t1) && isScalarType(t2)) return broadcastContainerScalar(t1, t2);
        if (isScalarType(t1) && isContainerType(t2)) return broadcastContainerScalar(t2, t1);
        return null;
    }

    // ── Array type string helpers ─────────────────────────────────────────────

    /** "array[,,] int" → "array[,] int"; "array[] int" → "int". */
    private static String stripOneArrayDim(String t) {
        int bracketEnd = t.indexOf(']');
        String commas = t.substring(6, bracketEnd);
        String base   = t.substring(bracketEnd + 2);
        return commas.isEmpty() ? base : "array[" + commas.substring(1) + "] " + base;
    }

    /**
     * Wrap {@code inner} with the same number of array dimensions as {@code ref}.
     * Used in commonType to restore the dimension count after recursing.
     */
    private static String rewrapOneArrayDim(String ref, String inner) {
        int bracketEnd = ref.indexOf(']');
        String commas = ref.substring(6, bracketEnd);
        return "array[" + commas + "] " + inner;
    }

    /**
     * Produce "array[commas] base" for {@code dims} dimensions, flattening any
     * existing array dimensions already present in {@code inner}.
     */
    private static String wrapArrayDims(String inner, int dims) {
        if (inner.startsWith("array[")) {
            int bracketEnd = inner.indexOf(']');
            String innerCommas = inner.substring(6, bracketEnd);
            String base = inner.substring(bracketEnd + 2);
            int totalDims = dims + (innerCommas.isEmpty() ? 1 : innerCommas.length() + 1);
            return "array[" + ",".repeat(totalDims - 1) + "] " + base;
        }
        return "array[" + ",".repeat(dims - 1) + "] " + inner;
    }

    public static boolean isContainerType(String t) {
        return "matrix".equals(t) || "vector".equals(t) || "row_vector".equals(t)
            || "complex_matrix".equals(t) || "complex_vector".equals(t) || "complex_row_vector".equals(t)
            || t.startsWith("array[");
    }

    public static boolean isScalarType(String t) {
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
