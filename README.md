[Stan](https://mc-stan.org/) plugin for JetBrains IDEs.

Currently supports:
- Syntax highlighting (configurable: Settings → Editor → Color Scheme → Stan)
- Syntax validation (Full AST parsing internally)
- Code folding (on `{}` blocks)
- Basic autocompletion (all builtins, keywords, variable names in scope)
- Basic type checking
- Semantic checks for unused variables
- Semantic checks for name shadowing
- Create a new Stan file from the file explorer context menu

Reuses syntax and semantics defined in [Stanc3](https://github.com/stan-dev/stanc3).

Vibe coded slop, so probably contains many bugs, is incomplete, inefficient and has fugly implementation.
But overall seems to work kinda OK.
Reports & PRs are wellcome.

See [CLAUDE.md](CLAUDE.md) for internal details on implementation design.