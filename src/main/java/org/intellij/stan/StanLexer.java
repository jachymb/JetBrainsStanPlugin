package org.intellij.stan;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StanLexer extends LexerBase {

    // Control-flow and special statement keywords (Stan reference manual §6)
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
        "break", "continue", "else", "for", "if", "in", "print", "profile",
        "reject", "return", "while", "fatal_error", "target", "jacobian"
    ));

    // Top-level block names (§3)
    private static final Set<String> BLOCK_KEYWORDS = new HashSet<>(Arrays.asList(
        "functions", "data", "parameters", "transformed",
        "model", "generated", "quantities"
    ));

    // Variable types and constraint identifiers (§4)
    private static final Set<String> TYPES = new HashSet<>(Arrays.asList(
        "int", "real", "vector", "row_vector", "matrix",
        "array", "tuple", "void",
        "complex", "complex_vector", "complex_matrix", "complex_row_vector",
        "ordered", "positive_ordered", "simplex", "unit_vector",
        "corr_matrix", "cov_matrix",
        "cholesky_factor_corr", "cholesky_factor_cov",
        "column_stochastic_matrix", "row_stochastic_matrix",
        "sum_to_zero_vector", "sum_to_zero_matrix",
        // Constraint keywords used inside < >
        "lower", "upper", "offset", "multiplier"
    ));

    // Reserved words that are not yet used but must not be used as identifiers
    private static final Set<String> RESERVED = new HashSet<>(Arrays.asList(
        "var", "fvar", "STAN_MAJOR", "STAN_MINOR", "STAN_PATCH", "STAN_MATH_MAJOR",
        "STAN_MATH_MINOR", "STAN_MATH_PATCH", "alignas", "alignof", "and", "and_eq",
        "asm", "auto", "bitand", "bitor", "bool", "catch", "char", "char16_t",
        "char32_t", "class", "compl", "const", "constexpr", "const_cast", "decltype",
        "delete", "do", "double", "dynamic_cast", "enum", "explicit", "export",
        "extern", "false", "float", "friend", "goto", "inline", "long", "mutable",
        "namespace", "new", "noexcept", "not", "not_eq", "nullptr", "operator", "or",
        "or_eq", "private", "protected", "public", "register", "reinterpret_cast",
        "repeat", "short", "signed", "sizeof", "static", "static_assert",
        "static_cast", "struct", "switch", "template", "this", "thread_local",
        "throw", "true", "try", "typedef", "typeid", "typename", "union", "unsigned",
        "using", "virtual", "volatile", "wchar_t", "xor", "xor_eq", "then", "until"
    ));

    // Built-in mathematical and statistical functions (Stan reference manual §10–§25)
    private static final Set<String> BUILTINS = new HashSet<>(Arrays.asList(
        // Basic math
        "abs", "acos", "acosh", "asin", "asinh", "atan", "atan2", "atanh",
        "cbrt", "ceil", "cos", "cosh", "exp", "exp2", "expm1",
        "fabs", "floor", "hypot", "inv", "inv_cloglog", "inv_logit", "inv_sqrt", "inv_square",
        "inv_Phi", "is_inf", "is_nan", "ldexp", "lgamma",
        "log", "log1m", "log1m_exp", "log1m_inv_logit", "log1p", "log1p_exp",
        "log2", "log10", "log_diff_exp", "log_inv_logit", "log_inv_logit_diff", "log_mix",
        "log_softmax", "log_sum_exp", "logit", "lmultiply",
        "Phi", "Phi_approx",
        "pow", "round", "sin", "sinh", "softmax", "sqrt", "square",
        "step", "tan", "tanh", "tgamma", "trunc",
        "fma", "fmax", "fmin", "fmod", "fdim",
        "lbeta", "binary_log_loss",
        "owens_t", "erf", "erfc", "inv_erfc",
        "inc_beta", "inv_inc_beta",
        "log_falling_factorial", "log_rising_factorial",
        "log_modified_bessel_first_kind",
        "modified_bessel_first_kind", "modified_bessel_second_kind",
        "bessel_first_kind", "bessel_second_kind",
        "falling_factorial", "rising_factorial", "lchoose", "choose",
        "digamma", "trigamma",
        "lmgamma", "gamma_p", "gamma_q",
        "lambert_w0", "lambert_wm1",
        "std_normal_qf", "std_normal_log_qf",
        // Complex number functions
        "to_complex", "get_real", "get_imag", "conj", "arg", "polar", "proj",
        // Numeric constants (nullary functions)
        "e", "pi", "sqrt2", "machine_precision",
        "not_a_number", "positive_infinity", "negative_infinity",
        // Integer / array utilities
        "max", "min", "sum", "prod", "mean", "variance", "sd",
        "num_elements", "size", "dims", "int_step", "to_int",
        "append_array", "rep_array",
        "zeros_array", "zeros_int_array", "zeros_row_vector", "zeros_vector",
        "ones_array", "ones_int_array", "ones_row_vector", "ones_vector",
        "one_hot_array", "one_hot_int_array", "one_hot_row_vector", "one_hot_vector",
        "linspaced_array", "linspaced_int_array", "linspaced_row_vector", "linspaced_vector",
        "uniform_simplex",
        "head", "tail", "segment", "reverse",
        "sort_asc", "sort_desc", "sort_indices_asc", "sort_indices_desc",
        "rank", "to_array_1d", "to_array_2d", "quantile",
        // Matrix functions
        "add_diag", "append_col", "append_row", "block",
        "chol2inv", "cholesky_decompose",
        "col", "cols", "columns_dot_product", "columns_dot_self",
        "crossprod", "tcrossprod",
        "cumulative_sum",
        "determinant", "log_determinant", "log_determinant_spd",
        "diag_matrix", "diag_post_multiply", "diag_pre_multiply",
        "diagonal", "distance", "squared_distance",
        "dot_product", "dot_self",
        "eigendecompose", "eigendecompose_sym",
        "eigenvalues", "eigenvalues_sym", "eigenvectors", "eigenvectors_sym",
        "generalized_inverse",
        "identity_matrix",
        "inverse", "inverse_spd",
        "kronecker_product",
        "matrix_exp", "matrix_exp_multiply", "matrix_power",
        "mdivide_left", "mdivide_left_spd", "mdivide_left_tri_low",
        "mdivide_right", "mdivide_right_spd", "mdivide_right_tri_low",
        "multiply_lower_tri_self_transpose",
        "norm", "norm1", "norm2",
        "quad_form", "quad_form_diag", "quad_form_sym",
        "qr_Q", "qr_R", "qr_thin_Q", "qr_thin_R",
        "rep_matrix", "rep_row_vector", "rep_vector",
        "row", "rows", "rows_dot_product", "rows_dot_self",
        "scale_matrix_exp_multiply",
        "singular_values",
        "sub_col", "sub_row",
        "svd_U", "svd_V",
        "symmetrize_from_lower_tri",
        "to_matrix", "to_row_vector", "to_vector",
        "trace", "trace_dot", "trace_gen_quad_form", "trace_quad_form",
        "transpose",
        // Complex Schur decomposition
        "complex_schur_decompose", "complex_schur_decompose_t", "complex_schur_decompose_u",
        // FFT
        "fft", "fft2", "inv_fft", "inv_fft2",
        // Sparse matrix
        "csr_extract_w", "csr_extract_v", "csr_extract_u",
        "csr_to_dense_matrix", "csr_matrix_times_vector",
        // Gaussian process covariance functions
        "gp_dot_prod_cov", "gp_exp_quad_cov", "gp_exponential_cov",
        "gp_matern32_cov", "gp_matern52_cov", "gp_periodic_cov",
        // HMM functions
        "hmm_hidden_state_prob", "hmm_marginal",
        // ODE solvers
        "ode_adams", "ode_adams_tol",
        "ode_bdf", "ode_bdf_tol",
        "ode_ckrk", "ode_ckrk_tol",
        "ode_rk45", "ode_rk45_tol",
        "ode_adjoint_tol_ctl",
        // DAE solvers
        "dae", "dae_tol",
        // Algebraic solvers
        "algebra_solver", "algebra_solver_newton",
        "solve_newton", "solve_newton_tol",
        "solve_powell", "solve_powell_tol",
        // 1-D integration
        "integrate_1d",
        // Map reduce
        "map_rect", "reduce_sum", "reduce_sum_static",
        // Pseudo-random number generators
        "bernoulli_rng", "bernoulli_logit_rng",
        "beta_rng", "beta_binomial_rng", "beta_neg_binomial_rng", "beta_proportion_rng",
        "binomial_rng", "categorical_rng", "categorical_logit_rng",
        "cauchy_rng", "chi_square_rng",
        "dirichlet_rng", "dirichlet_multinomial_rng", "discrete_range_rng",
        "double_exponential_rng",
        "exp_mod_normal_rng", "exponential_rng",
        "frechet_rng", "gamma_rng", "gumbel_rng",
        "hmm_latent_rng",
        "hypergeometric_rng", "inv_chi_square_rng", "inv_gamma_rng",
        "inv_wishart_rng", "inv_wishart_cholesky_rng",
        "lkj_corr_rng", "lkj_corr_cholesky_rng",
        "logistic_rng", "loglogistic_rng", "lognormal_rng",
        "multinomial_rng", "multinomial_logit_rng",
        "multi_normal_rng", "multi_normal_cholesky_rng", "multi_normal_prec_rng",
        "multi_student_t_rng", "multi_student_t_cholesky_rng",
        "neg_binomial_rng", "neg_binomial_2_rng", "neg_binomial_2_log_rng",
        "normal_rng", "ordered_logistic_rng", "ordered_probit_rng",
        "pareto_rng", "pareto_type_2_rng",
        "poisson_rng", "poisson_log_rng",
        "rayleigh_rng", "scaled_inv_chi_square_rng",
        "skew_normal_rng", "skew_double_exponential_rng",
        "std_normal_rng", "student_t_rng",
        "uniform_rng", "von_mises_rng", "weibull_rng", "wiener_rng",
        "wishart_rng", "wishart_cholesky_rng",
        "yule_simon_rng",
        // Distribution base names (_lpdf/_lpmf/_lcdf/_lccdf/_cdf/_ccdf)
        "bernoulli", "bernoulli_logit", "bernoulli_logit_glm",
        "beta", "beta_binomial", "beta_neg_binomial", "beta_proportion",
        "binomial", "binomial_logit", "binomial_logit_glm",
        "categorical", "categorical_logit", "categorical_logit_glm",
        "cauchy", "chi_square",
        "dirichlet", "dirichlet_multinomial", "discrete_range", "double_exponential",
        "exp_mod_normal", "exponential",
        "frechet", "gamma", "gaussian_dlm_obs",
        "gumbel", "hmm_latent", "hypergeometric", "inv_chi_square", "inv_gamma",
        "inv_wishart", "inv_wishart_cholesky",
        "lkj_corr", "lkj_corr_cholesky",
        "logistic", "loglogistic", "lognormal",
        "multinomial", "multinomial_logit",
        "multi_gp", "multi_gp_cholesky",
        "multi_normal", "multi_normal_cholesky", "multi_normal_prec",
        "multi_student_t", "multi_student_t_cholesky",
        "neg_binomial", "neg_binomial_2", "neg_binomial_2_log", "neg_binomial_2_log_glm",
        "normal", "normal_id_glm",
        "ordered_logistic", "ordered_logistic_glm", "ordered_probit",
        "pareto", "pareto_type_2",
        "poisson", "poisson_log", "poisson_log_glm",
        "rayleigh", "scaled_inv_chi_square",
        "skew_normal", "skew_double_exponential",
        "std_normal", "student_t",
        "uniform", "von_mises", "weibull", "wiener",
        "wishart", "wishart_cholesky",
        "yule_simon",
        // Truncation indicator
        "T",
        // Deprecated / legacy (still accepted by stanc3)
        "lkj_cov",
        "integrate_ode", "integrate_ode_rk45", "integrate_ode_bdf", "integrate_ode_adams",
        "get_lp", "increment_log_prob"
    ));

    private CharSequence buffer;
    private int bufferEnd;
    private int tokenStart;
    private int tokenEnd;
    private IElementType tokenType;

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer;
        this.bufferEnd = endOffset;
        this.tokenStart = startOffset;
        this.tokenEnd = startOffset;
        this.tokenType = null;
        advance();
    }

    @Override
    public int getState() {
        return 0;
    }

    @Nullable
    @Override
    public IElementType getTokenType() {
        return tokenType;
    }

    @Override
    public int getTokenStart() {
        return tokenStart;
    }

    @Override
    public int getTokenEnd() {
        return tokenEnd;
    }

    @Override
    public void advance() {
        tokenStart = tokenEnd;
        if (tokenStart >= bufferEnd) {
            tokenType = null;
            return;
        }
        tokenType = nextToken();
    }

    @NotNull
    @Override
    public CharSequence getBufferSequence() {
        return buffer;
    }

    @Override
    public int getBufferEnd() {
        return bufferEnd;
    }

    private char charAt(int pos) {
        return buffer.charAt(pos);
    }

    private IElementType nextToken() {
        int pos = tokenStart;
        char c = charAt(pos);

        // Whitespace
        if (Character.isWhitespace(c)) {
            while (pos < bufferEnd && Character.isWhitespace(charAt(pos))) pos++;
            tokenEnd = pos;
            return StanTokenTypes.WHITE_SPACE;
        }

        // Line comment: //
        if (c == '/' && pos + 1 < bufferEnd && charAt(pos + 1) == '/') {
            pos += 2;
            while (pos < bufferEnd && charAt(pos) != '\n') pos++;
            tokenEnd = pos;
            return StanTokenTypes.LINE_COMMENT;
        }

        // Line comment: # (deprecated but still supported in older Stan)
        if (c == '#') {
            pos++;
            while (pos < bufferEnd && charAt(pos) != '\n') pos++;
            tokenEnd = pos;
            return StanTokenTypes.LINE_COMMENT;
        }

        // Block comment: /* ... */
        if (c == '/' && pos + 1 < bufferEnd && charAt(pos + 1) == '*') {
            pos += 2;
            while (pos + 1 < bufferEnd && !(charAt(pos) == '*' && charAt(pos + 1) == '/')) pos++;
            if (pos + 1 < bufferEnd) pos += 2; // consume closing */
            else pos = bufferEnd;               // unterminated — consume to end
            tokenEnd = pos;
            return StanTokenTypes.BLOCK_COMMENT;
        }

        // String literal
        if (c == '"') {
            pos++;
            while (pos < bufferEnd && charAt(pos) != '"') {
                if (charAt(pos) == '\\' && pos + 1 < bufferEnd) pos++; // skip escape
                pos++;
            }
            if (pos < bufferEnd) pos++; // consume closing "
            tokenEnd = pos;
            return StanTokenTypes.STRING;
        }

        // Numeric literal: integer, real, or imaginary
        // Accepts: 123, 1.5, .5, 1e3, 1.5e-3, 1.5i
        if (Character.isDigit(c) || (c == '.' && pos + 1 < bufferEnd && Character.isDigit(charAt(pos + 1)))) {
            pos++;
            while (pos < bufferEnd && Character.isDigit(charAt(pos))) pos++;
            // Optional fractional part
            if (pos < bufferEnd && charAt(pos) == '.' && !(c == '.')) {
                pos++;
                while (pos < bufferEnd && Character.isDigit(charAt(pos))) pos++;
            }
            // Optional exponent
            if (pos < bufferEnd && (charAt(pos) == 'e' || charAt(pos) == 'E')) {
                pos++;
                if (pos < bufferEnd && (charAt(pos) == '+' || charAt(pos) == '-')) pos++;
                while (pos < bufferEnd && Character.isDigit(charAt(pos))) pos++;
            }
            // Optional imaginary suffix
            if (pos < bufferEnd && charAt(pos) == 'i') pos++;
            tokenEnd = pos;
            return StanTokenTypes.NUMBER;
        }

        // Identifier or keyword
        if (Character.isLetter(c) || c == '_') {
            pos++;
            while (pos < bufferEnd && (Character.isLetterOrDigit(charAt(pos)) || charAt(pos) == '_')) pos++;
            tokenEnd = pos;
            String word = buffer.subSequence(tokenStart, tokenEnd).toString();
            if (KEYWORDS.contains(word))      return StanTokenTypes.KEYWORD;
            if (BLOCK_KEYWORDS.contains(word)) return StanTokenTypes.BLOCK_KEYWORD;
            if (TYPES.contains(word))          return StanTokenTypes.TYPE;
            if (BUILTINS.contains(word))       return StanTokenTypes.BUILTIN_FUNCTION;
            if (RESERVED.contains(word))       return StanTokenTypes.KEYWORD; // highlight reserved words
            return StanTokenTypes.IDENTIFIER;
        }

        // Single-character advance for operators and punctuation
        pos++;
        tokenEnd = pos;

        switch (c) {
            case '{': return StanTokenTypes.LBRACE;
            case '}': return StanTokenTypes.RBRACE;
            case '[': return StanTokenTypes.LBRACKET;
            case ']': return StanTokenTypes.RBRACKET;
            case '(': return StanTokenTypes.LPAREN;
            case ')': return StanTokenTypes.RPAREN;
            case ';': return StanTokenTypes.SEMICOLON;
            case ',': return StanTokenTypes.COMMA;

            case '+':
                // +=
                if (pos < bufferEnd && charAt(pos) == '=') { pos++; tokenEnd = pos; }
                return StanTokenTypes.OPERATOR;

            case '-':
                return StanTokenTypes.OPERATOR;

            case '*':
                return StanTokenTypes.OPERATOR;

            case '/':
                return StanTokenTypes.OPERATOR;

            case '^':
                return StanTokenTypes.OPERATOR;

            case '\\':
                // matrix left-division
                return StanTokenTypes.OPERATOR;

            case '\'':
                // matrix transpose operator
                return StanTokenTypes.OPERATOR;

            case '%':
                // %/% integer division
                if (pos < bufferEnd && charAt(pos) == '/' && pos + 1 < bufferEnd && charAt(pos + 1) == '%') {
                    pos += 2;
                    tokenEnd = pos;
                }
                return StanTokenTypes.OPERATOR;

            case '|':
                // || logical or
                if (pos < bufferEnd && charAt(pos) == '|') { pos++; tokenEnd = pos; }
                return StanTokenTypes.OPERATOR;

            case '&':
                // && logical and
                if (pos < bufferEnd && charAt(pos) == '&') { pos++; tokenEnd = pos; }
                return StanTokenTypes.OPERATOR;

            case '!':
                // != not-equal
                if (pos < bufferEnd && charAt(pos) == '=') { pos++; tokenEnd = pos; }
                return StanTokenTypes.OPERATOR;

            case '=':
                // == equality
                if (pos < bufferEnd && charAt(pos) == '=') { pos++; tokenEnd = pos; }
                return StanTokenTypes.OPERATOR;

            case '<':
                // <= less-or-equal  |  <- old-style assignment
                if (pos < bufferEnd && (charAt(pos) == '=' || charAt(pos) == '-')) {
                    pos++;
                    tokenEnd = pos;
                }
                return StanTokenTypes.OPERATOR;

            case '>':
                // >= greater-or-equal
                if (pos < bufferEnd && charAt(pos) == '=') { pos++; tokenEnd = pos; }
                return StanTokenTypes.OPERATOR;

            case '~':
                // sampling statement operator
                return StanTokenTypes.OPERATOR;

            case '?':
            case ':':
                // ternary operator
                return StanTokenTypes.OPERATOR;

            case '.':
                // bare dot (shouldn't normally appear outside a number, but handle gracefully)
                return StanTokenTypes.OPERATOR;

            default:
                return StanTokenTypes.BAD_CHARACTER;
        }
    }
}
