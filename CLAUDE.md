This is a JetBrains IDE plugin for the Stan statistical modeling language.

Main sources: `src/main/java/org/intellij/stan/`
Write idiomatic Java.

---

## Architecture overview

The plugin uses the **Gradle Grammar-Kit Plugin** to generate the lexer and parser from grammar
files. All semantic checks are implemented as `LocalInspectionTool` subclasses.
The signature database is generated from stanc3 at dev time and shipped as a JSON resource.

### Pipeline

```
Stan.bnf  →(generateParser)→  src/main/gen/org/intellij/stan/parser/StanParser.java
                               src/main/gen/org/intellij/stan/psi/StanTypes.java  (token + element type constants)
                               src/main/gen/org/intellij/stan/psi/Stan*.java      (PSI interfaces)
                               src/main/gen/org/intellij/stan/psi/impl/Stan*Impl.java

Stan.flex →(generateLexer) →  src/main/gen/org/intellij/stan/lexer/_StanLexer.java

.stan file
  → StanLexer          (FlexAdapter wrapping _StanLexer; classifies tokens)
  → StanParser         (generated LL parser; builds PSI tree of StanTypes nodes)
  → StanBuiltinAnnotator  (colours builtin function names; checks StanSignatureDatabase)
  → Inspections        (walk the AST, report problems via ProblemsHolder)
```

`src/main/gen/` is gitignored. Regenerate with:
```
./gradlew.bat generateParser generateLexer
```
(requires JDK 21; set `JAVA_HOME` to `C:/Users/jachym.barvinek/scoop/apps/temurin21-jdk/21.0.11-10.0`)

---

## Key files

### Grammar — `src/main/grammars/Stan.bnf`
GrammarKit BNF file. Defines all token types (in the `tokens = [...]` block) and all grammar
rules. Every named rule becomes a PSI interface + implementation class and a constant in
`StanTypes`.

Important naming constraint: the rule for `IDENTIFIER | TRUNCATE` is named **`ident`** (not
`identifier`) to avoid a name clash with the `IDENTIFIER` token constant in the generated
`StanTypes.java`.

Named sub-rules that inspections depend on (partial list):
- `fun_call_expr` — ordinary call `f(args)`.
- `cond_dist_expr` — bar-notation call `f(y | params)`.
- `tilde_stmt` — sampling statement `y ~ dist(params)`.
- `assignment_stmt` — assignment `lvalue op rhs ;`.
- `fun_call_stmt` — void function call as a statement.
- `for_range_stmt` / `for_each_stmt` — the two for-loop forms.
- `var_decl` / `declared_var` / `declared_var_extra` — local variable declarations.
- `top_var_decl` / `top_declared_var` — block-level declarations with optional init.
- `top_var_decl_no_assign` / `no_assign_var` — data/parameters block declarations.
- `variable_expr` — bare identifier used as a value.
- `int_literal_expr` / `real_literal_expr` / `imag_literal_expr` — typed literals.
- `function_def` / `arg_decl` — function definition and its formal parameters.

### Lexer — `src/main/grammars/Stan.flex`
JFlex specification. Generates `_StanLexer.java`. All identifiers (including builtin function
names) are returned as `IDENTIFIER`; there is no `BUILTIN_FUNCTION` token.

`src/main/java/org/intellij/stan/lexer/StanLexer.java` — one-line `FlexAdapter` wrapper used
by `StanParserDefinition` and `StanSyntaxHighlighter`.

### Token / element type constants — `StanTypes` (generated)
`src/main/gen/org/intellij/stan/psi/StanTypes.java` — **the single source of truth for all
`IElementType` constants**, replacing the old `StanTokenTypes` and `StanElementTypes`.

Key token names (selected):
- Literals: `INTNUMERAL`, `REALNUMERAL`, `IMAGNUMERAL`, `STRINGLITERAL`
- Identifiers: `IDENTIFIER`, `TRUNCATE` (bare tokens); `IDENT` (the wrapper rule node)
- Block keywords: `FUNCTIONBLOCK`, `DATABLOCK`, `TRANSFORMEDDATABLOCK`, `PARAMETERSBLOCK`,
  `TRANSFORMEDPARAMETERSBLOCK`, `MODELBLOCK`, `GENERATEDQUANTITIESBLOCK`
- Type keywords: `INT`, `REAL`, `COMPLEX`, `VECTOR`, `ROWVECTOR`, `MATRIX`,
  `COMPLEXVECTOR`, `COMPLEXROWVECTOR`, `COMPLEXMATRIX`, `ARRAY`, `TUPLE`
- Constrained types: `ORDERED`, `POSITIVEORDERED`, `SIMPLEX`, `UNITVECTOR`, `SUMTOZEROVEC`,
  `SUMTOZEROMAT`, `CHOLESKYFACTORCORR`, `CHOLESKYFACTORCOV`, `CORRMATRIX`, `COVMATRIX`,
  `STOCHASTICCOLUMNMATRIX`, `STOCHASTICROWMATRIX`
- Operators: `PLUS`, `MINUS`, `TIMES`, `DIVIDE`, `MODULO`, `LDIVIDE`, `IDIVIDE`, `HAT`,
  `ELTPOW`, `ELTTIMES`, `ELTDIVIDE`, `OR`, `AND`, `EQUALS`, `NEQUALS`, `LEQ`, `GEQ`,
  `LABRACK` (`<`), `RABRACK` (`>`), `BANG`, `TRANSPOSE`, `TILDE`, `BAR`, `QMARK`
- Assignment ops: `ASSIGN`, `PLUSASSIGN`, `MINUSASSIGN`, `TIMESASSIGN`, `DIVIDEASSIGN`,
  `ELTTIMESASSIGN`, `ELTDIVIDEASSIGN`
- Punctuation: `LBRACE`, `RBRACE`, `LBRACK`, `RBRACK`, `LPAREN`, `RPAREN`,
  `SEMICOLON`, `COMMA`, `COLON`
- Comments/whitespace: `LINE_COMMENT`, `BLOCK_COMMENT`, `WHITE_SPACE`

PSI base classes (hand-written, not generated):
- `org.intellij.stan.psi.StanElementType` — base for composite (rule) node types.
- `org.intellij.stan.psi.StanTokenType` — base for token types.

### Signature database — `StanSignatureDatabase.java`
Singleton loaded from `stan_signatures.json` at first access. Provides:
- `hasFunction(name)` / `getSignatures(name)` — raw lookup.
- `getDistributionFunctionNames()` — set of all `_lpdf`/`_lpmf` function names from the DB
  plus their `_lupdf`/`_lupmf` counterparts.
- `inferExprType(node, typeMap)` — lightweight type inference for an expression node.
  Handles: named literal rules, `variable_expr` (via typeMap), `paren_expr`, `unary_expr`,
  arithmetic binary ops (`add_expr`, `mul_expr`, `ldiv_expr`, `pow_expr`),
  `fun_call_expr`, `cond_dist_expr`, and `index_expr`. Transparent wrapper rules pass through.
  Signature selection uses a two-pass strategy: exact-match first, then promotion-compatible.
- `buildTypeMap(root)` — walks the whole file and builds `name → type-string` for all
  `var_decl`, `top_var_decl`, `top_var_decl_no_assign`, `arg_decl`, and for-range loops.
- `typeNodeToString(node)` — converts a type AST node (`var_type`, `top_var_type`,
  `sized_basic_type`, `unsized_type`, or a bare keyword token) to a canonical type string.
  Dispatches on the **first child token** of the type node, not on the node type itself.
- `isCompatible(expected, actual)` / `commonType(t1, t2)` — promotion rules from stanc3's
  `UnsizedType.common_type`.
- `kwToTypeString(IElementType)` — maps a type keyword token to a type string.

Type strings: `"int"`, `"real"`, `"complex"`, `"vector"`, `"row_vector"`, `"matrix"`,
`"complex_vector"`, `"complex_row_vector"`, `"complex_matrix"`, `"a[T]"` (nestable).

### Utility — `StanSyntaxUtil.java`
- `CONDITIONING_SUFFIXES` — set of suffixes that require bar-notation calls
  (`_lpdf`, `_lupdf`, `_lpmf`, `_lupmf`, `_cdf`, `_lcdf`, `_lccdf`).
- `CPP_RESERVED` — set of C++ keywords forbidden as Stan identifiers.
- `hasConditioningSuffix(name)` — checks the set above.
- `findLeaf(node)` — walks to the first childless (token) node in a subtree. Used by
  inspections to reach the bare `IDENTIFIER` token inside `ident` / `decl_identifier`
  wrapper nodes.

### Inspections

All registered in `plugin.xml`. Each calls `buildTypeMap` and/or `collectUserDefinedFunctions`
once per file, then walks the AST using `ASTNode` operations.

Inspections navigate the tree by checking `node.getElementType()` against `StanTypes.*`
constants. Because the grammar has wrapper rules (`ident`, `decl_identifier`, etc.), use
`node.getText()` to get an identifier's text (works on any wrapper), and `StanSyntaxUtil.findLeaf()`
to get the specific token node for error highlighting.

| Class                              | Checks                                                                                                                                                                             |
|------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `StanFunctionCallInspection`       | Undefined functions (`IDENT` head not in functions block or DB); conditioning-suffix functions called with 2+ args without `\|`; arity mismatch; argument type mismatch for builtins. |
| `StanTypeMismatchInspection`       | LHS/RHS type incompatibility in `VAR_DECL` initialisers and `ASSIGNMENT_STMT`.                                                                                                     |
| `StanUndeclaredVariableInspection` | Variable used before declaration.                                                                                                                                                  |
| `StanUnusedVariableInspection`     | Variable declared but never read.                                                                                                                                                  |
| `StanReservedWordAnnotator`        | Stan keywords used as identifiers (via `RESERVED_WORD` node inside `DECL_IDENTIFIER`); C++ reserved words used as identifiers (bare `IDENTIFIER` token text checked against `StanSyntaxUtil.CPP_RESERVED`). |
| `StanConsecutiveUnaryInspection`   | E.g. `--x` or `!!x` (consecutive `UNARY_EXPR` nodes each with an operator first child).                                                                                            |

### Builtin function highlighting — `StanBuiltinAnnotator.java`
Registered as an `<annotator>` in `plugin.xml` (not as an inspection). Runs after PSI
construction; colours `IDENT` nodes in function-call head position when their text is a known
function in `StanSignatureDatabase`. This replaces the old `BUILTIN_FUNCTION` lexer token —
the JFlex lexer emits plain `IDENTIFIER` for everything.

### Signature database generator — `tools/generate_stan_signatures.py`
Parses stanc3's `src/stan_math_signatures/Generate.ml` and writes `stan_signatures.json`.
Re-run whenever stanc3 is updated: `python3 tools/generate_stan_signatures.py [path/to/stanc3/src]`
Output: `{"v":1, "f":{"name": [[[arg,...], ret], ...]}}`.

The stanc3 repo is expected at `../stanc3` relative to the project root.

Run python through wsl.

### Infrastructure

- `StanParserDefinition.java` — wires `StanLexer` + generated `StanParser` into the platform;
  returns `StanTypes.Factory.createElement(node)` for PSI construction.
- `StanSyntaxHighlighter.java` / `StanSyntaxHighlighterFactory.java` — maps `StanTypes` token
  constants to `TextAttributeKey`s. Uses inline `TokenSet` definitions (no separate
  `StanTokenTypes` class).
- `StanColorSettingsPage.java` — exposes color settings in IDE preferences.
- `StanFileType.java` / `StanLanguage.java` — registers `.stan` extension.
- `StanCreateFileAction.java` — "New → Stan File" action.
- `StanIcons.java` — file icon.

---

## Build system note

The Grammar-Kit Gradle plugin (2023.3.0.3) needs IntelliJ platform JARs on its classpath to
run the code generator. As of 2026-05, **`intellijRelease = "213.6777.52"` (IntelliJ 2021.3.2)**
is the newest build whose Maven artifact tree is fully publicly resolvable. Newer builds
(≥ 2021.3.3) pull in private JetBrains artifacts (`ai.grazie.*`,
`com.jetbrains.infra:download-pgp-verifier`) that are not in any public Maven repo.
This setting is **build-time only** and does not affect plugin compatibility.

---

## Relationship to stanc3

The reference compiler lives at `../stanc3` (sibling directory). It is **not** a build
dependency — it is only read by the generator script. When adding new language features,
cross-check against:
- `src/stan_math_signatures/Generate.ml` — all built-in function signatures.
- `src/middle/Utils.ml` — `conditioning_suffices`, `distribution_suffices`,
  `unnormalized_suffices`, `is_unnormalized_distribution`.
- `src/frontend/Typechecker.ml` — semantic rules (what is an error, in which block).
- `src/frontend/parser.mly` — grammar reference for the parser.
- `src/frontend/Semantic_error.ml` — error messages (useful for understanding intent).
