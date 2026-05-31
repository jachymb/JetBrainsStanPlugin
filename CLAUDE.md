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
  → StanLexer                    (tokenises; classifies names as KEYWORD / BUILTIN_FUNCTION / IDENTIFIER)
  → StanParser                   (builds PSI tree of StanElementTypes nodes)
  → StanSemanticHighlightAnnotator  (per-token annotator; fixes up BUILTIN_FUNCTION color when shadowed)
  → StanReservedWordAnnotator    (marks RESERVED tokens with error annotations)
  → Inspections                  (walk the AST, report problems via ProblemsHolder)
  → StanCompletionContributor    (type-aware completion)
  → StanFoldingBuilder           (code folding for block-level constructs)
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
  **Critical**: the lexer emits `BUILTIN_FUNCTION` only when the name is immediately followed by
  `(` (skipping whitespace). In any other position (declarations, variable references, lvalues)
  the token type is downgraded to `IDENTIFIER`. This means declaration sites always see
  `IDENTIFIER`, regardless of whether the name happens to match a built-in.
- `TRUNCATE_KW` — the special token for `T` in truncation syntax `T[lo, hi]`.
- `RESERVED` — C++ keywords that are forbidden as Stan identifiers.
- `IDENTIFIER` — everything else (user-defined names).

Distribution suffixed builtins (`normal_lpdf`, `poisson_lupmf`, …) are populated dynamically at
startup from `StanSignatureDatabase.getDistributionFunctionNames()`, which reads the JSON and
derives `_lupdf`/`_lupmf` variants by suffix substitution.

### Token types — `StanTokenTypes.java`
All leaf token `IElementType` constants. Key sets: `KEYWORDS`, `BLOCK_KEYWORDS`, `TYPE_KEYWORDS`,
`CONSTRAINT_KEYWORDS`, `ALL_OPERATORS`, `IDENTIFIERS`, `ASSIGNMENT_OPS`.

`ASSIGNMENT_OPS` covers `=`, `+=`, `-=`, `*=`, `/=`, `.*=`, `./=`, and deprecated `<-`.
Plain `=` and `<-` are pure writes; the rest are compound (read + write).

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
- `FOR_RANGE_STMT` — `for (i in lo:hi)` loop; loop variable is always typed `int`.
- `FOR_EACH_STMT` — `for (x in expr)` loop; loop variable type is unknown.
- `ASSIGNMENT_STMT` — the lvalue identifier is a raw `IDENTIFIER` token (not inside `VARIABLE_EXPR`).
  Index subscripts appear as `INDEX_LIST` children between the identifier and the assignment op.

`BLOCK_TYPES` (TokenSet) — all seven top-level block element types; used by the folding builder,
rng-context inspection, and completion contributor.

### Parser — `StanParser.java`
Recursive-descent, directly emits PSI via `PsiBuilder`. Grammar reference: stanc3 `parser.mly`.

Important parser-level semantic checks (not inspections):
- **Conditioning notation**: `parseNameAtom` captures the function name before consuming it;
  when `|` is seen, `hasConditioningSuffix(name)` is checked. If the name does not end in
  `_lpdf`, `_lupdf`, `_lpmf`, `_lupmf`, `_cdf`, `_lcdf`, or `_lccdf` (stanc3's
  `conditioning_suffices` list), the `|` token is wrapped in a parse-error node.
- `CONDITIONING_SUFFIXES` / `hasConditioningSuffix()` — static constant + helper, reused by
  inspections.

Type parsing is unified in `parseVarType(boolean allowConstraints)`:
- `allowConstraints = true` (top-level blocks): parses optional `<lower=…>` / `<offset=…>`
  constraint annotations and constrained-only types (`ordered`, `simplex`, etc.).
- `allowConstraints = false` (model-block locals): plain sizes only; no constraint syntax.
- `parseTopVarType()` and `parseSizedBasicType()` are thin wrappers around `parseVarType`.

For-loop parsing: `parseExpr()` is used for both the range start and iterator expressions after
`in`. Since `:` is not a binary operator in Stan, `parseExpr()` stops before it naturally. If `:`
follows, the result is `FOR_RANGE_STMT`; otherwise `FOR_EACH_STMT`.

### Signature database — `StanSignatureDatabase.java`
Singleton loaded from `stan_signatures.json` at first access. Provides:

**Query API:**
- `hasFunction(name)` / `getSignatures(name)` — raw lookup.
- `getFunctionNames()` — all names in the DB.
- `getDistributionFunctionNames()` — set of all `_lpdf`/`_lpmf` names from the DB plus their
  `_lupdf`/`_lupmf` counterparts; used by the lexer to register builtins.
- `isBuiltinName(name)` — true for any DB function, distribution suffix variant, or
  distribution family base name.
- `DISTRIBUTION_BASE_NAMES` (static Set) — authoritative set of distribution family stems
  (e.g. `"normal"`, `"poisson"`) used in tilde statements. Includes legacy names not in the DB.
  This is the source of truth for tilde-statement completions.

**AST utilities (shared by inspections and completion):**
- `funDefNameToken(funDef)` — the `IDENTIFIER` child before `LPAREN` in a `FUN_DEF` node.
- `argDeclNameToken(argDecl)` — the `IDENTIFIER` that follows the type node in an `ARG_DECL`
  node (skips optional `DATA_KW`). Used by `processArgDecl` and `StanShadowingInspection`.
- `collectUserDefinedFunctionNames(root)` — walks the AST and returns all `FUN_DEF` names.
  Shared by `StanFunctionCallInspection` and any code that needs to distinguish user-defined
  from built-in functions.

**Type map construction:**
- `buildTypeMap(root)` — walks the whole file and builds `name → type-string` for all
  `VAR_DECL`, `ARG_DECL`, and for-range loop variables. Skips `FUN_DEF` bodies entirely so
  function parameter names do not bleed into the file-level map.
- `buildFunctionParamMap(funDefNode)` — builds a type map containing only the formal parameters
  of a single `FUN_DEF`. Used by inspections inside function bodies, so they only see the
  function's own scope, not outer block variables.

**Type inference:**
- `inferExprType(node, typeMap)` — lightweight type inference for an expression node. Handles
  literals, variables (via typeMap), parentheses, unary ops, binary ops (`common_type`),
  `FUN_CALL_EXPR`, and `COND_DIST_EXPR`. Signature selection uses a two-pass strategy:
  exact-match first, then promotion-compatible, to avoid picking `(complex)→complex` over
  `(real)→real` for a real argument when the sorted DB has complex before real.
- `typeNodeToString(node)` — converts a type AST node to a canonical type string
  (`"int"`, `"real"`, `"vector"`, `"array[,] int"` for arrays, etc.).
- `isCompatible(expected, actual)` / `commonType(t1, t2)` — promotion rules from stanc3's
  `UnsizedType.common_type`.
- `collectArgExprs(callNode)` — collects expression children from the `ARG_LIST`(s) of a call
  node; handles both `FUN_CALL_EXPR` (one `ARG_LIST`) and `COND_DIST_EXPR` (two `ARG_LIST`s).

Type strings used throughout: `"int"`, `"real"`, `"complex"`, `"vector"`, `"row_vector"`,
`"matrix"`, `"complex_vector"`, `"complex_row_vector"`, `"complex_matrix"`, `"array[…] T"`
(nestable, commas inside brackets indicate dimensions: `"array[,] int"` = 2D int array).

### Annotators

**`StanSemanticHighlightAnnotator`** — per-element `Annotator` that fires on every
`BUILTIN_FUNCTION` token in the file. If the name is present in the type map (i.e. shadowed by
a local variable or parameter), it overrides the color to `IDENTIFIER` so the editor renders it
as a plain name rather than a built-in. Calls `buildTypeMap` on every invocation — this is
O(file-size) per token and has no caching. Acceptable for typical Stan files, but worth noting
if performance becomes an issue.

**`StanReservedWordAnnotator`** — marks `RESERVED` tokens (C++ keywords used as Stan identifiers)
with error-level annotations.

### Inspections

All registered in `plugin.xml`. Each that does type checking calls `buildTypeMap` (and/or
`buildFunctionParamMap` inside function bodies) once per file, then walks the AST.
`collectUserDefinedFunctionNames` is used wherever the distinction between user-defined and
built-in functions matters.

| Class                              | Checks                                                                                                                                                                                                              |
|------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `StanFunctionCallInspection`       | Undefined functions; conditioning-suffix functions called with 2+ args without `\|`; variable used as function; arity mismatch; argument type mismatch for builtins.                                                |
| `StanTypeMismatchInspection`       | LHS/RHS type incompatibility in `VAR_DECL` initialisers and `ASSIGNMENT_STMT`.                                                                                                                                      |
| `StanUndeclaredVariableInspection` | Variable used before declaration; respects Stan's waterfall block scope and per-function isolation.                                                                                                                 |
| `StanUnusedVariableInspection`     | Variable declared but never read. Compound assignments (`x += e`) and indexed assignments (`x[i] = e`) count the lvalue as "read". Pure writes (`x = e`, `x <- e`) do not. Checks function parameters per-function. |
| `StanShadowingInspection`          | Warns at declaration sites (`ARG_DECL`, `DECLARED_VAR`) when the declared name matches a built-in function. Uses `argDeclNameToken` to locate the name token.                                                       |
| `StanRngCallContextInspection`     | `_rng` functions may only be called from transformed data, generated quantities, or a user function whose name ends in `_rng`.                                                                                      |
| `StanConsecutiveUnaryInspection`   | Consecutive unary operators e.g. `--x` or `!!x`.                                                                                                                                                                    |

### Completion — `StanCompletionContributor.java`
Type-aware completion contributor. Position is classified in two passes:
1. **Token pass** — looks at the nearest preceding non-trivia token. Operators, open brackets,
   keywords like `return`/`in` → `EXPR`. Type keywords, `{`, `}`, `;`, `else` → `STMT_START`.
2. **PSI-ancestor pass** — walks upward through PSI parents. Structural expression ancestors
   (e.g. `ARG_LIST`, `BINARY_OP_EXPR`, `INDEXED_EXPR`) → `EXPR`. Statement/block ancestors → `STMT_START`.

For `EXPR` positions, a `TypeConstraint` is computed from the surrounding context:
- Inside `ARG_LIST`: expected type from the function signature at the relevant argument index.
- Inside `BINARY_OP_EXPR`: must form a common type with the other operand.
- Inside an index node: must be `int`.
- Inside `DECLARED_VAR`: must be compatible with the declared type.
- Inside `ASSIGNMENT_STMT`: must be compatible with the LHS type.

Completions offered: type keywords, statement keywords, built-in functions (filtered by rng
context and return type), user-defined functions (from `collectFunDefs`), and in-scope variables.
After `~`, only distribution family base names from `DISTRIBUTION_BASE_NAMES` are offered.

### Folding — `StanFoldingBuilder.java`
`FoldingBuilderEx` that creates fold regions for all top-level block types (`BLOCK_TYPES`),
`FUN_DEF`, `BLOCK_STMT`, and `PROFILE_STMT`. Folds the range from `{` to `}` (inclusive).
Skips expression nodes (arrays, row vectors, tuples) deliberately to avoid registering hundreds
of tiny regions on large files. Returns early (`EMPTY_ARRAY`) in quick mode.

### Signature database generator — `tools/generate_stan_signatures.py`
Parses stanc3's `src/stan_math_signatures/Generate.ml` and writes `stan_signatures.json`.
Re-run whenever stanc3 is updated: `python3 tools/generate_stan_signatures.py [path/to/stanc3/src]`
Output: `{"v":1, "f":{"name": [[[arg,...], ret], ...]}}`.

The stanc3 repo is expected at `../stanc3` relative to the project root.

### Infrastructure

- `StanParserDefinition.java` — wires lexer + parser into the IntelliJ platform.
- `StanSyntaxHighlighter.java` / `StanSyntaxHighlighterFactory.java` — maps token types to
  `TextAttributeKey`s. `RESERVED` tokens are colored via the highlighter (not an annotator).
- `StanColorSettingsPage.java` — exposes color settings in IDE preferences; demo text loaded
  from `colorSettingsDemo.stan`.
- `StanFileType.java` / `StanLanguage.java` — registers `.stan` extension.
- `StanCreateFileAction.java` — "New → Stan File" action; creates a file from a template and
  positions the caret inside the first block.
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
