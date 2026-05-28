package org.intellij.stan;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Hand-written lexer for the Stan statistical language.
 * <p>
 * Extends {@link LexerBase} and produces token types defined in
 * {@link StanTokenTypes}. The implementation is a single-pass scanner with
 * O(1) keyword lookup via a static {@link HashMap}.
 */
public class StanLexer extends LexerBase {

    // -----------------------------------------------------------------------
    //  Static keyword / builtin / reserved lookup table
    // -----------------------------------------------------------------------

    /**
     * Maps every reserved text to its {@link IElementType}.
     * Populated once in the class initialiser; after that it is read-only
     * and therefore thread-safe.
     */
    private static final HashMap<String, IElementType> KEYWORD_MAP = new HashMap<>(1024);

    static {
        // ---- Control-flow keywords ----
        KEYWORD_MAP.put("if",          StanTokenTypes.IF_KW);
        KEYWORD_MAP.put("else",        StanTokenTypes.ELSE_KW);
        KEYWORD_MAP.put("for",         StanTokenTypes.FOR_KW);
        KEYWORD_MAP.put("while",       StanTokenTypes.WHILE_KW);
        KEYWORD_MAP.put("in",          StanTokenTypes.IN_KW);
        KEYWORD_MAP.put("break",       StanTokenTypes.BREAK_KW);
        KEYWORD_MAP.put("continue",    StanTokenTypes.CONTINUE_KW);
        KEYWORD_MAP.put("return",      StanTokenTypes.RETURN_KW);
        KEYWORD_MAP.put("print",       StanTokenTypes.PRINT_KW);
        KEYWORD_MAP.put("reject",      StanTokenTypes.REJECT_KW);
        KEYWORD_MAP.put("fatal_error", StanTokenTypes.FATAL_ERROR_KW);
        KEYWORD_MAP.put("profile",     StanTokenTypes.PROFILE_KW);
        KEYWORD_MAP.put("target",      StanTokenTypes.TARGET_KW);
        KEYWORD_MAP.put("jacobian",    StanTokenTypes.JACOBIAN_KW);

        // ---- Block keywords ----
        KEYWORD_MAP.put("functions",   StanTokenTypes.FUNCTIONS_KW);
        KEYWORD_MAP.put("data",        StanTokenTypes.DATA_KW);
        KEYWORD_MAP.put("parameters",  StanTokenTypes.PARAMETERS_KW);
        KEYWORD_MAP.put("transformed", StanTokenTypes.TRANSFORMED_KW);
        KEYWORD_MAP.put("model",       StanTokenTypes.MODEL_KW);
        KEYWORD_MAP.put("generated",   StanTokenTypes.GENERATED_KW);
        KEYWORD_MAP.put("quantities",  StanTokenTypes.QUANTITIES_KW);

        // ---- Primitive type keywords ----
        KEYWORD_MAP.put("int",                StanTokenTypes.INT_KW);
        KEYWORD_MAP.put("real",               StanTokenTypes.REAL_KW);
        KEYWORD_MAP.put("complex",            StanTokenTypes.COMPLEX_KW);
        KEYWORD_MAP.put("vector",             StanTokenTypes.VECTOR_KW);
        KEYWORD_MAP.put("row_vector",         StanTokenTypes.ROW_VECTOR_KW);
        KEYWORD_MAP.put("matrix",             StanTokenTypes.MATRIX_KW);
        KEYWORD_MAP.put("complex_vector",     StanTokenTypes.COMPLEX_VECTOR_KW);
        KEYWORD_MAP.put("complex_row_vector", StanTokenTypes.COMPLEX_ROW_VECTOR_KW);
        KEYWORD_MAP.put("complex_matrix",     StanTokenTypes.COMPLEX_MATRIX_KW);
        KEYWORD_MAP.put("array",              StanTokenTypes.ARRAY_KW);
        KEYWORD_MAP.put("tuple",              StanTokenTypes.TUPLE_KW);
        KEYWORD_MAP.put("void",               StanTokenTypes.VOID_KW);

        // ---- Constrained type keywords ----
        KEYWORD_MAP.put("ordered",                  StanTokenTypes.ORDERED_KW);
        KEYWORD_MAP.put("positive_ordered",         StanTokenTypes.POSITIVE_ORDERED_KW);
        KEYWORD_MAP.put("simplex",                  StanTokenTypes.SIMPLEX_KW);
        KEYWORD_MAP.put("unit_vector",              StanTokenTypes.UNIT_VECTOR_KW);
        KEYWORD_MAP.put("sum_to_zero_vector",       StanTokenTypes.SUM_TO_ZERO_VECTOR_KW);
        KEYWORD_MAP.put("sum_to_zero_matrix",       StanTokenTypes.SUM_TO_ZERO_MATRIX_KW);
        KEYWORD_MAP.put("cholesky_factor_corr",     StanTokenTypes.CHOLESKY_FACTOR_CORR_KW);
        KEYWORD_MAP.put("cholesky_factor_cov",      StanTokenTypes.CHOLESKY_FACTOR_COV_KW);
        KEYWORD_MAP.put("corr_matrix",              StanTokenTypes.CORR_MATRIX_KW);
        KEYWORD_MAP.put("cov_matrix",               StanTokenTypes.COV_MATRIX_KW);
        KEYWORD_MAP.put("column_stochastic_matrix", StanTokenTypes.COLUMN_STOCHASTIC_MATRIX_KW);
        KEYWORD_MAP.put("row_stochastic_matrix",    StanTokenTypes.ROW_STOCHASTIC_MATRIX_KW);

        // ---- Constraint sub-keywords ----
        KEYWORD_MAP.put("lower",      StanTokenTypes.LOWER_KW);
        KEYWORD_MAP.put("upper",      StanTokenTypes.UPPER_KW);
        KEYWORD_MAP.put("offset",     StanTokenTypes.OFFSET_KW);
        KEYWORD_MAP.put("multiplier", StanTokenTypes.MULTIPLIER_KW);

        // ---- Built-in functions ----
        // putIfAbsent so that keywords defined above always win.
        String[] builtins = {
            // Basic math
            "abs", "acos", "acosh", "asin", "asinh", "atan", "atan2", "atanh",
            "cbrt", "ceil", "cos", "cosh", "exp", "exp2", "expm1",
            "fabs", "floor", "hypot",
            "inv", "inv_cloglog", "inv_logit", "inv_sqrt", "inv_square", "inv_Phi",
            "is_inf", "is_nan", "ldexp", "lgamma",
            "log", "log1m", "log1m_exp", "log1m_inv_logit",
            "log1p", "log1p_exp", "log2", "log10",
            "log_diff_exp", "log_inv_logit", "log_inv_logit_diff", "log_mix",
            "log_softmax", "log_sum_exp", "logit", "lmultiply",
            "Phi", "Phi_approx",
            "pow", "round", "sin", "sinh", "softmax", "sqrt", "square",
            "step", "tan", "tanh", "tgamma", "trunc",
            "fma", "fmax", "fmin", "fmod", "fdim",
            "lbeta", "binary_log_loss", "owens_t",
            "erf", "erfc", "inv_erfc",
            "inc_beta", "inv_inc_beta",
            "log_falling_factorial", "log_rising_factorial",
            "log_modified_bessel_first_kind",
            "modified_bessel_first_kind", "modified_bessel_second_kind",
            "bessel_first_kind", "bessel_second_kind",
            "falling_factorial", "rising_factorial",
            "lchoose", "choose",
            "digamma", "trigamma", "lmgamma", "gamma_p", "gamma_q",
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
            "crossprod", "tcrossprod", "cumulative_sum",
            "determinant", "log_determinant", "log_determinant_spd",
            "diag_matrix", "diag_post_multiply", "diag_pre_multiply", "diagonal",
            "distance", "squared_distance", "dot_product", "dot_self",
            "eigendecompose", "eigendecompose_sym",
            "eigenvalues", "eigenvalues_sym", "eigenvectors", "eigenvectors_sym",
            "generalized_inverse", "identity_matrix", "inverse", "inverse_spd",
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
            "scale_matrix_exp_multiply", "singular_values",
            "sub_col", "sub_row", "svd_U", "svd_V",
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
            // 1-D integration / map-reduce
            "integrate_1d",
            "map_rect", "reduce_sum", "reduce_sum_static", "partial_sum",
            // RNG functions
            "bernoulli_rng", "bernoulli_logit_rng",
            "beta_rng", "beta_binomial_rng", "beta_neg_binomial_rng", "beta_proportion_rng",
            "binomial_rng", "categorical_rng", "categorical_logit_rng",
            "cauchy_rng", "chi_square_rng",
            "dirichlet_rng", "dirichlet_multinomial_rng", "discrete_range_rng",
            "double_exponential_rng", "exp_mod_normal_rng", "exponential_rng",
            "frechet_rng", "gamma_rng", "gumbel_rng", "hmm_latent_rng",
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
            // Distribution base names
            "bernoulli", "bernoulli_logit", "bernoulli_logit_glm",
            "beta", "beta_binomial", "beta_neg_binomial", "beta_proportion",
            "binomial", "binomial_logit", "binomial_logit_glm",
            "categorical", "categorical_logit", "categorical_logit_glm",
            "cauchy", "chi_square",
            "dirichlet", "dirichlet_multinomial", "discrete_range", "double_exponential",
            "exp_mod_normal", "exponential",
            "frechet", "gamma", "gaussian_dlm_obs",
            "gumbel", "hmm_latent", "hypergeometric",
            "inv_chi_square", "inv_gamma",
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
            "wishart", "wishart_cholesky", "yule_simon",
            // Truncation indicator
            "T",
            // Explicit lpdf / lpmf / lcdf / lccdf names
            "normal_lpdf", "normal_lpmf", "normal_lcdf", "normal_lccdf",
            // Deprecated / legacy
            "lkj_cov",
            "integrate_ode", "integrate_ode_rk45", "integrate_ode_bdf", "integrate_ode_adams",
            "get_lp", "increment_log_prob",
        };
        for (String b : builtins) {
            KEYWORD_MAP.putIfAbsent(b, StanTokenTypes.BUILTIN_FUNCTION);
        }

        // ---- Reserved words (C++ keywords forbidden as Stan identifiers) ----
        String[] reserved = {
            "var", "fvar",
            "STAN_MAJOR", "STAN_MINOR", "STAN_PATCH",
            "STAN_MATH_MAJOR", "STAN_MATH_MINOR", "STAN_MATH_PATCH",
            "alignas", "alignof", "and", "and_eq", "asm", "auto",
            "bitand", "bitor", "bool", "catch",
            "char", "char16_t", "char32_t", "class", "compl",
            "const", "constexpr", "const_cast",
            "decltype", "delete", "do", "double", "dynamic_cast",
            "enum", "explicit", "export", "extern",
            "false", "float", "friend", "goto", "inline",
            "long", "mutable", "namespace", "new", "noexcept",
            "not", "not_eq", "nullptr",
            "operator", "or", "or_eq",
            "private", "protected", "public",
            "register", "reinterpret_cast", "repeat",
            "short", "signed", "sizeof",
            "static", "static_assert", "static_cast",
            "struct", "switch",
            "template", "this", "thread_local", "throw", "true", "try",
            "typedef", "typeid", "typename",
            "union", "unsigned", "using",
            "virtual", "volatile", "wchar_t",
            "xor", "xor_eq",
            "then", "until",
        };
        for (String r : reserved) {
            KEYWORD_MAP.putIfAbsent(r, StanTokenTypes.RESERVED);
        }
    }

    // -----------------------------------------------------------------------
    //  Lexer state
    // -----------------------------------------------------------------------

    private CharSequence myBuffer;
    /** Exclusive end offset of the active buffer slice. */
    private int myEnd;
    private int myTokenStart;
    private int myTokenEnd;
    private IElementType myTokenType;

    // -----------------------------------------------------------------------
    //  LexerBase interface
    // -----------------------------------------------------------------------

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        myBuffer     = buffer;
        myEnd        = endOffset;
        myTokenStart = startOffset;
        myTokenEnd   = startOffset;
        myTokenType  = null;
        advance();
    }

    @Override
    public int getState() {
        return 0;
    }

    @Nullable
    @Override
    public IElementType getTokenType() {
        return myTokenType;
    }

    @Override
    public int getTokenStart() {
        return myTokenStart;
    }

    @Override
    public int getTokenEnd() {
        return myTokenEnd;
    }

    @Override
    public void advance() {
        myTokenStart = myTokenEnd;
        if (myTokenStart >= myEnd) {
            myTokenType = null;
            return;
        }
        myTokenType = scanToken();
    }

    @NotNull
    @Override
    public CharSequence getBufferSequence() {
        return myBuffer;
    }

    @Override
    public int getBufferEnd() {
        return myEnd;
    }

    // -----------------------------------------------------------------------
    //  Core scanner
    // -----------------------------------------------------------------------

    /**
     * Scans one token beginning at {@code myTokenStart}, sets {@code myTokenEnd}
     * to the first position after the token, and returns the token type.
     */
    private IElementType scanToken() {
        int pos = myTokenStart;
        char c = charAt(pos);

        // ----------------------------------------------------------------
        //  Whitespace
        // ----------------------------------------------------------------
        if (Character.isWhitespace(c)) {
            pos++;
            while (pos < myEnd && Character.isWhitespace(charAt(pos))) pos++;
            myTokenEnd = pos;
            return StanTokenTypes.WHITE_SPACE;
        }

        // ----------------------------------------------------------------
        //  Comments (must be checked before '/' as a plain operator)
        // ----------------------------------------------------------------
        if (c == '/') {
            if (pos + 1 < myEnd) {
                char c2 = charAt(pos + 1);
                if (c2 == '/') {
                    // Line comment
                    pos += 2;
                    while (pos < myEnd && charAt(pos) != '\n') pos++;
                    myTokenEnd = pos;
                    return StanTokenTypes.LINE_COMMENT;
                }
                if (c2 == '*') {
                    // Block comment (unterminated = consume to EOF)
                    pos += 2;
                    while (pos < myEnd) {
                        if (charAt(pos) == '*' && pos + 1 < myEnd && charAt(pos + 1) == '/') {
                            pos += 2;
                            break;
                        }
                        pos++;
                    }
                    myTokenEnd = pos;
                    return StanTokenTypes.BLOCK_COMMENT;
                }
            }
            // Fall through: plain '/' or '/=' handled in the operator section below.
        }

        if (c == '#') {
            // Deprecated Stan line comment
            pos++;
            while (pos < myEnd && charAt(pos) != '\n') pos++;
            myTokenEnd = pos;
            return StanTokenTypes.LINE_COMMENT;
        }

        // ----------------------------------------------------------------
        //  String literal
        // ----------------------------------------------------------------
        if (c == '"') {
            pos++;
            while (pos < myEnd) {
                char sc = charAt(pos);
                if (sc == '\\') {
                    pos += 2; // skip escape + following char
                } else if (sc == '"') {
                    pos++;    // consume closing quote
                    break;
                } else {
                    pos++;
                }
            }
            myTokenEnd = pos;
            return StanTokenTypes.STRING_LITERAL;
        }

        // ----------------------------------------------------------------
        //  Numeric literals
        //  Priority: check leading digit, or leading '.' followed by a digit.
        // ----------------------------------------------------------------
        if (Character.isDigit(c)) {
            return scanNumberFromDigit(pos);
        }

        // Leading dot: could be .digit (real), or .* ./  .^ .*= ./= (operators), or plain .
        if (c == '.') {
            if (pos + 1 < myEnd && Character.isDigit(charAt(pos + 1))) {
                return scanNumberFromDot(pos);
            }
            return scanDotOperator(pos);
        }

        // ----------------------------------------------------------------
        //  Identifiers and keywords
        //  Stan: [a-zA-Z][a-zA-Z0-9_]* | _[a-zA-Z0-9_]+
        // ----------------------------------------------------------------
        if (Character.isLetter(c) || c == '_') {
            pos++;
            while (pos < myEnd) {
                char ic = charAt(pos);
                if (Character.isLetterOrDigit(ic) || ic == '_') {
                    pos++;
                } else {
                    break;
                }
            }
            myTokenEnd = pos;
            String text = myBuffer.subSequence(myTokenStart, myTokenEnd).toString();
            IElementType kw = KEYWORD_MAP.get(text);
            return kw != null ? kw : StanTokenTypes.IDENTIFIER;
        }

        // ----------------------------------------------------------------
        //  Operators and punctuation
        // ----------------------------------------------------------------
        return scanOperator(pos);
    }

    // -----------------------------------------------------------------------
    //  Number scanning helpers
    // -----------------------------------------------------------------------

    /**
     * Scan a number whose first character (a digit) is at {@code pos}.
     * Handles integers, reals, and imaginary suffixes.
     */
    private IElementType scanNumberFromDigit(int pos) {
        pos = consumeDigits(pos); // consume leading digit(s)

        boolean isReal = false;

        // Optional '.' fractional part — only if the dot is not a dot-operator
        if (pos < myEnd && charAt(pos) == '.') {
            int afterDot = pos + 1;
            // Accept '.' followed by: digit, 'e', 'E', or end-of-token-stream chars.
            // Reject '.' followed by another identifier char (e.g. tuple access x.1)
            // or dot-operator chars (*, /, ^).
            if (afterDot >= myEnd
                    || Character.isDigit(charAt(afterDot))
                    || charAt(afterDot) == 'e'
                    || charAt(afterDot) == 'E'
                    || isNumberTrailingChar(charAt(afterDot))) {
                isReal = true;
                pos++; // consume '.'
                pos = consumeDigits(pos);
            }
        }

        // Optional exponent
        int afterExp = tryConsumeExponent(pos);
        if (afterExp > pos) {
            isReal = true;
            pos = afterExp;
        }

        // Optional imaginary suffix 'i' — must not be followed by identifier chars
        if (pos < myEnd && charAt(pos) == 'i'
                && (pos + 1 >= myEnd || !isIdentChar(charAt(pos + 1)))) {
            pos++;
            myTokenEnd = pos;
            return StanTokenTypes.IMAG_LITERAL;
        }

        myTokenEnd = pos;
        return isReal ? StanTokenTypes.REAL_LITERAL : StanTokenTypes.INT_LITERAL;
    }

    /**
     * Scan a real literal whose first character is '.' (verified: next char is a digit).
     */
    private IElementType scanNumberFromDot(int pos) {
        pos++; // consume '.'
        pos = consumeDigits(pos);
        pos = tryConsumeExponent(pos);

        // Optional imaginary suffix
        if (pos < myEnd && charAt(pos) == 'i'
                && (pos + 1 >= myEnd || !isIdentChar(charAt(pos + 1)))) {
            pos++;
            myTokenEnd = pos;
            return StanTokenTypes.IMAG_LITERAL;
        }

        myTokenEnd = pos;
        return StanTokenTypes.REAL_LITERAL;
    }

    /**
     * Returns true if {@code ch} is a character that can legally follow a number
     * (i.e. it is NOT an identifier character and NOT '.', '*', '/', '^' which
     * would signal a dot-operator rather than a decimal point).
     */
    private static boolean isNumberTrailingChar(char ch) {
        switch (ch) {
            case ' ': case '\t': case '\r': case '\n':
            case ',': case ';': case ')': case ']': case '}':
            case '+': case '-': case '~': case '|': case '&':
            case '>': case '<': case '=': case '!':
            case '?': case ':': case '\'': case 'i':
                return true;
            default:
                return false;
        }
    }

    /** Consume zero or more ASCII decimal digits; return updated position. */
    private int consumeDigits(int pos) {
        while (pos < myEnd && Character.isDigit(charAt(pos))) pos++;
        return pos;
    }

    /**
     * Try to consume an exponent {@code [eE][+-]?[0-9]+} starting at {@code pos}.
     * Returns the new position (same as {@code pos} if no exponent was present).
     */
    private int tryConsumeExponent(int pos) {
        if (pos >= myEnd) return pos;
        char c = charAt(pos);
        if (c != 'e' && c != 'E') return pos;
        // Require at least one digit after optional sign, otherwise don't consume
        int next = pos + 1;
        if (next < myEnd && (charAt(next) == '+' || charAt(next) == '-')) next++;
        if (next >= myEnd || !Character.isDigit(charAt(next))) return pos;
        pos++; // consume 'e'/'E'
        if (pos < myEnd && (charAt(pos) == '+' || charAt(pos) == '-')) pos++;
        pos = consumeDigits(pos);
        return pos;
    }

    // -----------------------------------------------------------------------
    //  Dot-operator scanner
    // -----------------------------------------------------------------------

    /**
     * Called when we have '.' at {@code pos} that is NOT followed by a digit.
     * Handles: .*=  ./=  .*  ./  .^  and bare .
     */
    private IElementType scanDotOperator(int pos) {
        if (pos + 1 < myEnd) {
            char c2 = charAt(pos + 1);
            if (c2 == '*') {
                if (pos + 2 < myEnd && charAt(pos + 2) == '=') {
                    myTokenEnd = pos + 3;
                    return StanTokenTypes.ELT_TIMES_ASSIGN;
                }
                myTokenEnd = pos + 2;
                return StanTokenTypes.ELT_TIMES;
            }
            if (c2 == '/') {
                if (pos + 2 < myEnd && charAt(pos + 2) == '=') {
                    myTokenEnd = pos + 3;
                    return StanTokenTypes.ELT_DIVIDE_ASSIGN;
                }
                myTokenEnd = pos + 2;
                return StanTokenTypes.ELT_DIVIDE;
            }
            if (c2 == '^') {
                myTokenEnd = pos + 2;
                return StanTokenTypes.ELT_POW;
            }
        }
        myTokenEnd = pos + 1;
        return StanTokenTypes.DOT;
    }

    // -----------------------------------------------------------------------
    //  Operator / punctuation scanner
    // -----------------------------------------------------------------------

    /**
     * Called for every character that is not whitespace, a comment starter,
     * a quote, a digit, a letter, or an underscore.  Also called for '/'
     * that was not a comment start, and for '.' that was not a number/dot-op.
     */
    private IElementType scanOperator(int pos) {
        char c = charAt(pos);
        switch (c) {
            // ---- Modulo / integer-divide ----
            case '%':
                if (pos + 2 < myEnd && charAt(pos + 1) == '/' && charAt(pos + 2) == '%') {
                    myTokenEnd = pos + 3;
                    return StanTokenTypes.IDIVIDE;
                }
                myTokenEnd = pos + 1;
                return StanTokenTypes.MODULO;

            // ---- Logical OR / pipe (bar) ----
            case '|':
                if (pos + 1 < myEnd && charAt(pos + 1) == '|') {
                    myTokenEnd = pos + 2;
                    return StanTokenTypes.OR;
                }
                myTokenEnd = pos + 1;
                return StanTokenTypes.BAR;

            // ---- Logical AND ----
            case '&':
                if (pos + 1 < myEnd && charAt(pos + 1) == '&') {
                    myTokenEnd = pos + 2;
                    return StanTokenTypes.AND;
                }
                // Single '&' is not valid Stan; emit bad character
                myTokenEnd = pos + 1;
                return StanTokenTypes.BAD_CHARACTER;

            // ---- Equality / assignment ----
            case '=':
                if (pos + 1 < myEnd && charAt(pos + 1) == '=') {
                    myTokenEnd = pos + 2;
                    return StanTokenTypes.EQUALS;
                }
                myTokenEnd = pos + 1;
                return StanTokenTypes.ASSIGN;

            // ---- Not-equal / bang ----
            case '!':
                if (pos + 1 < myEnd && charAt(pos + 1) == '=') {
                    myTokenEnd = pos + 2;
                    return StanTokenTypes.NEQUALS;
                }
                myTokenEnd = pos + 1;
                return StanTokenTypes.BANG;

            // ---- Less-than / LEQ / arrow ----
            case '<':
                if (pos + 1 < myEnd) {
                    char c2 = charAt(pos + 1);
                    if (c2 == '=') { myTokenEnd = pos + 2; return StanTokenTypes.LEQ; }
                    if (c2 == '-') { myTokenEnd = pos + 2; return StanTokenTypes.ARROW; }
                }
                myTokenEnd = pos + 1;
                return StanTokenTypes.LESS;

            // ---- Greater-than / GEQ ----
            case '>':
                if (pos + 1 < myEnd && charAt(pos + 1) == '=') {
                    myTokenEnd = pos + 2;
                    return StanTokenTypes.GEQ;
                }
                myTokenEnd = pos + 1;
                return StanTokenTypes.GREATER;

            // ---- Plus / plus-assign ----
            case '+':
                if (pos + 1 < myEnd && charAt(pos + 1) == '=') {
                    myTokenEnd = pos + 2;
                    return StanTokenTypes.PLUS_ASSIGN;
                }
                myTokenEnd = pos + 1;
                return StanTokenTypes.PLUS;

            // ---- Minus / minus-assign ----
            case '-':
                if (pos + 1 < myEnd && charAt(pos + 1) == '=') {
                    myTokenEnd = pos + 2;
                    return StanTokenTypes.MINUS_ASSIGN;
                }
                myTokenEnd = pos + 1;
                return StanTokenTypes.MINUS;

            // ---- Times / times-assign ----
            case '*':
                if (pos + 1 < myEnd && charAt(pos + 1) == '=') {
                    myTokenEnd = pos + 2;
                    return StanTokenTypes.TIMES_ASSIGN;
                }
                myTokenEnd = pos + 1;
                return StanTokenTypes.TIMES;

            // ---- Divide / divide-assign (comments already handled) ----
            case '/':
                if (pos + 1 < myEnd && charAt(pos + 1) == '=') {
                    myTokenEnd = pos + 2;
                    return StanTokenTypes.DIVIDE_ASSIGN;
                }
                myTokenEnd = pos + 1;
                return StanTokenTypes.DIVIDE;

            // ---- Exponentiation ----
            case '^':
                myTokenEnd = pos + 1;
                return StanTokenTypes.POW;

            // ---- Matrix left-divide ----
            case '\\':
                myTokenEnd = pos + 1;
                return StanTokenTypes.LDIVIDE;

            // ---- Sampling / tilde ----
            case '~':
                myTokenEnd = pos + 1;
                return StanTokenTypes.TILDE;

            // ---- Transpose ----
            case '\'':
                myTokenEnd = pos + 1;
                return StanTokenTypes.TRANSPOSE;

            // ---- Ternary question ----
            case '?':
                myTokenEnd = pos + 1;
                return StanTokenTypes.QUESTION;

            // ---- Colon ----
            case ':':
                myTokenEnd = pos + 1;
                return StanTokenTypes.COLON;

            // ---- Braces / brackets / parens ----
            case '{': myTokenEnd = pos + 1; return StanTokenTypes.LBRACE;
            case '}': myTokenEnd = pos + 1; return StanTokenTypes.RBRACE;
            case '[': myTokenEnd = pos + 1; return StanTokenTypes.LBRACKET;
            case ']': myTokenEnd = pos + 1; return StanTokenTypes.RBRACKET;
            case '(': myTokenEnd = pos + 1; return StanTokenTypes.LPAREN;
            case ')': myTokenEnd = pos + 1; return StanTokenTypes.RPAREN;

            // ---- Statement / argument separators ----
            case ';': myTokenEnd = pos + 1; return StanTokenTypes.SEMICOLON;
            case ',': myTokenEnd = pos + 1; return StanTokenTypes.COMMA;

            // ---- Anything else ----
            default:
                myTokenEnd = pos + 1;
                return StanTokenTypes.BAD_CHARACTER;
        }
    }

    // -----------------------------------------------------------------------
    //  Small utilities
    // -----------------------------------------------------------------------

    /** Returns {@code true} if {@code ch} is a valid identifier continuation character. */
    private static boolean isIdentChar(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '_';
    }

    /** Safe {@code charAt} that stays within buffer bounds (caller must verify). */
    private char charAt(int pos) {
        return myBuffer.charAt(pos);
    }
}
