This is a JetBrains IDE plugin for the Stan statistical modeling language.

Main sources: `src/main/java/org/intellij/stan/`
Write idiomatic Java.

---

## Architecture overview

The plugin is a handwritten lexer + recursive-descent parser (no grammar file / GrammarKit).
All semantic checks are implemented as `LocalInspectionTool` subclasses.
The signature database is generated from stanc3 at dev time and shipped as a JSON resource.

### Pipeline

```
.stan file
  → StanLexer          (tokenises; classifies names as KEYWORD / BUILTIN_FUNCTION / IDENTIFIER)
  → StanParser         (builds PSI tree of StanElementTypes nodes)
  → Inspections        (walk the AST, report problems via ProblemsHolder)
```

---

## Key files

### Lexer — `StanLexer.java`
Single-pass scanner. The main data structure is `KEYWORD_MAP: HashMap<String, IElementType>`,
populated once in a `static {}` block. Every identifier is looked up there; unknown names become
`IDENTIFIER`.

Token categories relevant to inspections:
- `BUILTIN_FUNCTION` — all stan_math functions (from the database) plus plain distribution names
  (`normal`, `poisson`, …) and their `_lpdf`/`_lpmf`/`_lupdf`/`_lupmf` variants.
- `TRUNCATE_KW` — the special token for `T` in truncation syntax `T[lo, hi]`.
- `RESERVED` — C++ keywords that are forbidden as Stan identifiers.
- `IDENTIFIER` — everything else (user-defined names).

Distribution suffixed builtins (`normal_lpdf`, `poisson_lupmf`, …) are populated dynamically at
startup from `StanSignatureDatabase.getDistributionFunctionNames()`, which reads the JSON and
derives `_lupdf`/`_lupmf` variants by suffix substitution.

### Token types — `StanTokenTypes.java`
All leaf token `IElementType` constants. Key sets: `KEYWORDS`, `BLOCK_KEYWORDS`, `TYPE_KEYWORDS`,
`CONSTRAINT_KEYWORDS`, `ALL_OPERATORS`, `IDENTIFIERS`.

### AST node types — `StanElementTypes.java`
All composite (non-terminal) `IElementType` constants. Key ones for inspections:
- `FUN_DEF` — a function definition; first `IDENTIFIER`/`BUILTIN_FUNCTION` child before `LPAREN`
  is the function name.
- `FUN_CALL_EXPR` — ordinary call `f(args)`.
- `COND_DIST_EXPR` — bar-notation call `f(y | params)`; two `ARG_LIST` children.
- `TILDE_STMT` — sampling statement `y ~ dist(params)`.
- `VAR_DECL` / `DECLARED_VAR` — variable declarations with optional initializers.
- `ARG_DECL` — a single formal parameter in a function signature.
- `TRUNCATION` — `T[lo?, hi?]` node produced after a tilde statement.

### Parser — `StanParser.java`
Recursive-descent, directly emits PSI via `PsiBuilder`. Grammar reference: stanc3 `parser.mly`.

Important parser-level semantic checks (not inspections):
- **Conditioning notation**: `parseNameAtom` captures the function name before consuming it;
  when `|` is seen, `hasConditioningSuffix(name)` is checked. If the name does not end in
  `_lpdf`, `_lupdf`, `_lpmf`, `_lupmf`, `_cdf`, `_lcdf`, or `_lccdf` (stanc3's
  `conditioning_suffices` list), the `|` token is wrapped in a parse-error node.
- `CONDITIONING_SUFFIXES` / `hasConditioningSuffix()` — static constant + helper, reused by
  inspections.

### Signature database — `StanSignatureDatabase.java`
Singleton loaded from `stan_signatures.json` at first access. Provides:
- `hasFunction(name)` / `getSignatures(name)` — raw lookup.
- `getDistributionFunctionNames()` — set of all `_lpdf`/`_lpmf` function names from the DB plus
  their `_lupdf`/`_lupmf` counterparts; used by the lexer to register builtins.
- `inferExprType(node, typeMap)` — lightweight type inference for an expression node. Handles
  literals, variables (via typeMap), parentheses, unary ops, binary ops (`common_type`),
  `FUN_CALL_EXPR`, and `COND_DIST_EXPR`. Signature selection uses a two-pass strategy:
  exact-match first, then promotion-compatible, to avoid picking `(complex)→complex` over
  `(real)→real` for a real argument when the sorted DB has complex before real.
- `buildTypeMap(root)` — walks the whole file and builds `name → type-string` for all
  `VAR_DECL`, `ARG_DECL`, and for-loop variables.
- `typeNodeToString(node)` — converts a type AST node to a canonical type string
  (`"int"`, `"real"`, `"vector"`, `"a[T]"` for arrays, etc.).
- `isCompatible(expected, actual)` / `commonType(t1, t2)` — promotion rules from stanc3's
  `UnsizedType.common_type`.

Type strings used throughout: `"int"`, `"real"`, `"complex"`, `"vector"`, `"row_vector"`,
`"matrix"`, `"complex_vector"`, `"complex_row_vector"`, `"complex_matrix"`, `"a[T]"` (nestable).

### Inspections

All registered in `plugin.xml`. Each calls `buildTypeMap` and/or `collectUserDefinedFunctions`
once per file, then walks the AST.

| Class                              | Checks                                                                                                                                                                             |
|------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `StanFunctionCallInspection`       | Undefined functions (IDENTIFIER head not in functions block); conditioning-suffix functions called with 2+ args without `\|`; arity mismatch; argument type mismatch for builtins. |
| `StanTypeMismatchInspection`       | LHS/RHS type incompatibility in `VAR_DECL` initialisers and `ASSIGNMENT_STMT`.                                                                                                     |
| `StanUndeclaredVariableInspection` | Variable used before declaration.                                                                                                                                                  |
| `StanUnusedVariableInspection`     | Variable declared but never read.                                                                                                                                                  |
| `StanReservedWordAnnotator`        | `RESERVED` token used as an identifier (C++ keywords).                                                                                                                             |
| `StanConsecutiveUnaryInspection`   | E.g. `--x` or `!!x`.                                                                                                                                                               |

### Signature database generator — `tools/generate_stan_signatures.py`
Parses stanc3's `src/stan_math_signatures/Generate.ml` and writes `stan_signatures.json`.
Re-run whenever stanc3 is updated: `python3 tools/generate_stan_signatures.py [path/to/stanc3/src]`
Output: `{"v":1, "f":{"name": [[[arg,...], ret], ...]}}`.

The stanc3 repo is expected at `../stanc3` relative to the project root.

Run python through wsl.

### Infrastructure

- `StanParserDefinition.java` — wires lexer + parser into the IntelliJ platform.
- `StanSyntaxHighlighter.java` / `StanSyntaxHighlighterFactory.java` — maps token types to
  `TextAttributeKey`s.
- `StanColorSettingsPage.java` — exposes color settings in IDE preferences.
- `StanFileType.java` / `StanLanguage.java` — registers `.stan` extension.
- `StanCreateFileAction.java` — "New → Stan File" action.
- `StanIcons.java` — file icon.
- `StanTokenType.java` — trivial `IElementType` subclass used for both token and node types.

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
