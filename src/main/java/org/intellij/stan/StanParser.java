package org.intellij.stan;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * Recursive-descent parser for the Stan statistical language.
 *
 * Grammar reference: stanc3 parser.mly
 * Token types:   StanTokenTypes
 * Element types: StanElementTypes
 */
public class StanParser implements PsiParser {

    // Suffixes that allow the conditional-distribution bar notation: f(y | theta)
    // Source: stanc3 src/middle/Utils.ml conditioning_suffices
    static final String[] CONDITIONING_SUFFIXES =
        {"_lpdf", "_lupdf", "_lpmf", "_lupmf", "_cdf", "_lcdf", "_lccdf"};

    static boolean hasConditioningSuffix(String name) {
        if (name == null) return false;
        for (String s : CONDITIONING_SUFFIXES)
            if (name.endsWith(s)) return true;
        return false;
    }

    private PsiBuilder builder;

    @Override
    public @NotNull ASTNode parse(@NotNull IElementType root, @NotNull PsiBuilder builder) {
        this.builder = builder;
        PsiBuilder.Marker m = builder.mark();
        parseProgram();
        m.done(root);
        return builder.getTreeBuilt();
    }

    // -------------------------------------------------------------------------
    // Helper utilities
    // -------------------------------------------------------------------------

    private boolean at(IElementType t) {
        return builder.getTokenType() == t;
    }

    private boolean atAny(IElementType... ts) {
        for (IElementType t : ts) {
            if (at(t)) return true;
        }
        return false;
    }

    /** True if the current token is an identifier, a built-in function name, or the truncation keyword 'T' (which the grammar allows as an ordinary identifier). */
    private boolean isName() {
        return at(StanTokenTypes.IDENTIFIER) || at(StanTokenTypes.BUILTIN_FUNCTION) || at(StanTokenTypes.TRUNCATE_KW);
    }

    /** Advances past the current name token, or emits an error and does not advance. */
    private void expectName(String fallback) {
        if (isName()) {
            advance();
        } else if (StanTokenTypes.ALL_KEYWORDS.contains(builder.getTokenType())) {
            PsiBuilder.Marker m = builder.mark();
            String text = builder.getTokenText();
            advance();
            m.error("Expected a new identifier but found reserved keyword '" + text + "'");
        } else {
            builder.error(fallback);
        }
    }

    /**
     * When consuming a name token in a *definition* position (function name in FUN_DEF),
     * reclassify BUILTIN_FUNCTION → IDENTIFIER so the PSI node is always IDENTIFIER
     * regardless of whether the name happens to match a built-in.
     */
    private void remapIfBuiltin() {
        if (at(StanTokenTypes.BUILTIN_FUNCTION) || at(StanTokenTypes.TRUNCATE_KW))
            builder.remapCurrentToken(StanTokenTypes.IDENTIFIER);
    }

    private void advance() {
        builder.advanceLexer();
    }

    /**
     * Consume the current token if it matches {@code t}, otherwise emit an error.
     * @return true if the token was consumed.
     */
    private boolean expect(IElementType t, String msg) {
        if (at(t)) {
            advance();
            return true;
        }
        builder.error(msg);
        return false;
    }

    /**
     * Consume the current token if it matches {@code t}.
     * @return true if consumed.
     */
    private boolean consume(IElementType t) {
        if (at(t)) {
            advance();
            return true;
        }
        return false;
    }

    private boolean eof() {
        return builder.eof();
    }

    // -------------------------------------------------------------------------
    // Program
    // -------------------------------------------------------------------------

    /**
     * program:
     *   functionsBlock? dataBlock? transformedDataBlock? parametersBlock?
     *   transformedParametersBlock? modelBlock? generatedQuantitiesBlock? EOF
     */
    private void parseProgram() {
        if (at(StanTokenTypes.FUNCTIONS_KW)) parseFunctionsBlock();
        if (at(StanTokenTypes.DATA_KW)) parseDataBlock();
        if (at(StanTokenTypes.TRANSFORMED_KW)) parseTransformedBlock();
        if (at(StanTokenTypes.PARAMETERS_KW)) parseParametersBlock();
        if (at(StanTokenTypes.TRANSFORMED_KW)) parseTransformedBlock();
        if (at(StanTokenTypes.MODEL_KW)) parseModelBlock();
        if (at(StanTokenTypes.GENERATED_KW)) parseGeneratedQuantitiesBlock();
        if (!eof()) {
            builder.error("Unexpected token at top level");
            // consume remaining tokens to avoid infinite loop
            while (!eof()) advance();
        }
    }

    // -------------------------------------------------------------------------
    // Top-level blocks
    // -------------------------------------------------------------------------

    /** functionsBlock: FUNCTIONS_KW '{' funDef* '}' */
    private void parseFunctionsBlock() {
        PsiBuilder.Marker m = builder.mark();
        advance(); // FUNCTIONS_KW
        expect(StanTokenTypes.LBRACE, "'{' expected after 'functions'");
        while (!at(StanTokenTypes.RBRACE) && !eof()) {
            parseFunDef();
        }
        expect(StanTokenTypes.RBRACE, "'}' expected to close functions block");
        m.done(StanElementTypes.FUNCTIONS_BLOCK);
    }

    /** dataBlock: DATA_KW '{' topVarDeclNoAssign* '}' */
    private void parseDataBlock() {
        PsiBuilder.Marker m = builder.mark();
        advance(); // DATA_KW
        expect(StanTokenTypes.LBRACE, "'{' expected after 'data'");
        while (!at(StanTokenTypes.RBRACE) && !eof()) {
            if (!parseTopVarDeclNoAssign()) {
                builder.error("Variable declaration expected");
                advance();
            }
        }
        expect(StanTokenTypes.RBRACE, "'}' expected to close data block");
        m.done(StanElementTypes.DATA_BLOCK);
    }

    /**
     * Handles both transformedDataBlock and transformedParametersBlock.
     * We look ahead after TRANSFORMED_KW to decide which block we are in.
     *
     * transformedDataBlock:       TRANSFORMED_KW DATA_KW       '{' topVarDeclOrStatement* '}'
     * transformedParametersBlock: TRANSFORMED_KW PARAMETERS_KW '{' topVarDeclOrStatement* '}'
     */
    private void parseTransformedBlock() {
        PsiBuilder.Marker m = builder.mark();
        advance(); // TRANSFORMED_KW
        boolean isData = at(StanTokenTypes.DATA_KW);
        boolean isParameters = at(StanTokenTypes.PARAMETERS_KW);
        if (isData) {
            advance(); // DATA_KW
        } else if (isParameters) {
            advance(); // PARAMETERS_KW
        } else {
            builder.error("'data' or 'parameters' expected after 'transformed'");
        }
        expect(StanTokenTypes.LBRACE, "'{' expected");
        while (!at(StanTokenTypes.RBRACE) && !eof()) {
            parseTopVarDeclOrStatement();
        }
        expect(StanTokenTypes.RBRACE, "'}' expected to close transformed block");
        if (isData) {
            m.done(StanElementTypes.TRANSFORMED_DATA_BLOCK);
        } else {
            m.done(StanElementTypes.TRANSFORMED_PARAMETERS_BLOCK);
        }
    }

    /** parametersBlock: PARAMETERS_KW '{' topVarDeclNoAssign* '}' */
    private void parseParametersBlock() {
        PsiBuilder.Marker m = builder.mark();
        advance(); // PARAMETERS_KW
        expect(StanTokenTypes.LBRACE, "'{' expected after 'parameters'");
        while (!at(StanTokenTypes.RBRACE) && !eof()) {
            if (!parseTopVarDeclNoAssign()) {
                builder.error("Variable declaration expected");
                advance();
            }
        }
        expect(StanTokenTypes.RBRACE, "'}' expected to close parameters block");
        m.done(StanElementTypes.PARAMETERS_BLOCK);
    }

    /** modelBlock: MODEL_KW '{' varDeclOrStatement* '}' */
    private void parseModelBlock() {
        PsiBuilder.Marker m = builder.mark();
        advance(); // MODEL_KW
        expect(StanTokenTypes.LBRACE, "'{' expected after 'model'");
        while (!at(StanTokenTypes.RBRACE) && !eof()) {
            parseVarDeclOrStatement();
        }
        expect(StanTokenTypes.RBRACE, "'}' expected to close model block");
        m.done(StanElementTypes.MODEL_BLOCK);
    }

    /** generatedQuantitiesBlock: GENERATED_KW QUANTITIES_KW '{' topVarDeclOrStatement* '}' */
    private void parseGeneratedQuantitiesBlock() {
        PsiBuilder.Marker m = builder.mark();
        advance(); // GENERATED_KW
        expect(StanTokenTypes.QUANTITIES_KW, "'quantities' expected after 'generated'");
        expect(StanTokenTypes.LBRACE, "'{' expected");
        while (!at(StanTokenTypes.RBRACE) && !eof()) {
            parseTopVarDeclOrStatement();
        }
        expect(StanTokenTypes.RBRACE, "'}' expected to close generated quantities block");
        m.done(StanElementTypes.GENERATED_QUANTITIES_BLOCK);
    }

    // -------------------------------------------------------------------------
    // Function definitions
    // -------------------------------------------------------------------------

    /**
     * funDef: returnType IDENTIFIER '(' paramList ')' statement
     *
     * returnType: VOID_KW | unsizedType
     */
    private void parseFunDef() {
        PsiBuilder.Marker m = builder.mark();
        parseReturnType();
        if (isName()) {
            remapIfBuiltin(); // definition site: always IDENTIFIER regardless of built-in name
            advance(); // function name
        } else {
            expectName("Function name expected");
        }
        expect(StanTokenTypes.LPAREN, "'(' expected in function definition");
        parseParamList();
        expect(StanTokenTypes.RPAREN, "')' expected in function definition");
        parseStatement();
        m.done(StanElementTypes.FUN_DEF);
    }

    private void parseReturnType() {
        if (at(StanTokenTypes.VOID_KW)) {
            advance();
        } else {
            parseUnsizedType();
        }
    }

    /**
     * paramList: (argDecl (',' argDecl)*)?
     */
    private void parseParamList() {
        PsiBuilder.Marker m = builder.mark();
        if (!at(StanTokenTypes.RPAREN) && !eof()) {
            parseArgDecl();
            while (at(StanTokenTypes.COMMA)) {
                advance(); // ','
                parseArgDecl();
            }
        }
        m.done(StanElementTypes.PARAM_LIST);
    }

    /**
     * argDecl: DATA_KW? unsizedType IDENTIFIER
     */
    private void parseArgDecl() {
        PsiBuilder.Marker m = builder.mark();
        consume(StanTokenTypes.DATA_KW);
        parseUnsizedType();
        expectName("Parameter name expected");
        m.done(StanElementTypes.ARG_DECL);
    }

    // -------------------------------------------------------------------------
    // Unsized types (for function signatures)
    // -------------------------------------------------------------------------

    /**
     * unsizedType:
     *   ARRAY_KW unsizedDims basicUnsizedType  -> UNSIZED_ARRAY_TYPE
     *   | basicUnsizedType
     *   | unsizedTupleType
     */
    private void parseUnsizedType() {
        if (at(StanTokenTypes.ARRAY_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance(); // ARRAY_KW
            parseUnsizedDims();
            parseBasicUnsizedType();
            m.done(StanElementTypes.UNSIZED_ARRAY_TYPE);
        } else if (at(StanTokenTypes.TUPLE_KW)) {
            parseUnsizedTupleType();
        } else {
            parseBasicUnsizedType();
        }
    }

    /**
     * basicUnsizedType: INT_KW | REAL_KW | COMPLEX_KW | VECTOR_KW | ROW_VECTOR_KW | MATRIX_KW
     *                 | COMPLEX_VECTOR_KW | COMPLEX_ROW_VECTOR_KW | COMPLEX_MATRIX_KW
     */
    private void parseBasicUnsizedType() {
        if (atAny(StanTokenTypes.INT_KW, StanTokenTypes.REAL_KW, StanTokenTypes.COMPLEX_KW,
                  StanTokenTypes.VECTOR_KW, StanTokenTypes.ROW_VECTOR_KW, StanTokenTypes.MATRIX_KW,
                  StanTokenTypes.COMPLEX_VECTOR_KW, StanTokenTypes.COMPLEX_ROW_VECTOR_KW,
                  StanTokenTypes.COMPLEX_MATRIX_KW)) {
            advance();
        } else {
            builder.error("Type expected");
        }
    }

    /**
     * unsizedTupleType: TUPLE_KW '(' unsizedType (',' unsizedType)+ ')'
     */
    private void parseUnsizedTupleType() {
        PsiBuilder.Marker m = builder.mark();
        advance(); // TUPLE_KW
        expect(StanTokenTypes.LPAREN, "'(' expected in tuple type");
        parseUnsizedType();
        while (at(StanTokenTypes.COMMA)) {
            advance(); // ','
            parseUnsizedType();
        }
        expect(StanTokenTypes.RPAREN, "')' expected in tuple type");
        m.done(StanElementTypes.UNSIZED_TUPLE_TYPE);
    }

    /**
     * unsizedDims: '[' ','* ']'   -- commas + 1 = number of dimensions
     */
    private void parseUnsizedDims() {
        PsiBuilder.Marker m = builder.mark();
        expect(StanTokenTypes.LBRACKET, "'[' expected for array dimensions");
        while (at(StanTokenTypes.COMMA)) {
            advance();
        }
        expect(StanTokenTypes.RBRACKET, "']' expected for array dimensions");
        m.done(StanElementTypes.UNSIZED_DIMS);
    }

    // -------------------------------------------------------------------------
    // Sized types — with optional constraints
    // -------------------------------------------------------------------------

    /** top-level blocks (data/parameters/…): sized types with constraint annotations. */
    private boolean parseTopVarType()    { return parseVarType(true); }

    /** model-block locals: sized types without constraint annotations. */
    private boolean parseSizedBasicType() { return parseVarType(false); }

    /**
     * Unified sized-type parser.
     *
     * @param allowConstraints true = data/parameters blocks (constraint annotations are legal);
     *                         false = model-block locals (no annotations, only plain sizes).
     */
    private boolean parseVarType(boolean allowConstraints) {
        if (at(StanTokenTypes.INT_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance();
            if (allowConstraints) parseRangeConstraint();
            m.done(StanElementTypes.INT_TYPE);

        } else if (at(StanTokenTypes.REAL_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance();
            if (allowConstraints) parseTypeConstraint();
            m.done(StanElementTypes.REAL_TYPE);

        } else if (at(StanTokenTypes.COMPLEX_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance();
            if (allowConstraints) parseTypeConstraint();
            m.done(StanElementTypes.COMPLEX_TYPE);

        } else if (at(StanTokenTypes.VECTOR_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance();
            if (allowConstraints) parseTypeConstraint();
            parseSizeBracket1D();
            m.done(StanElementTypes.VECTOR_TYPE);

        } else if (at(StanTokenTypes.ROW_VECTOR_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance();
            if (allowConstraints) parseTypeConstraint();
            parseSizeBracket1D();
            m.done(StanElementTypes.ROW_VECTOR_TYPE);

        } else if (at(StanTokenTypes.MATRIX_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance();
            if (allowConstraints) parseTypeConstraint();
            parseSizeBracket2D();
            m.done(StanElementTypes.MATRIX_TYPE);

        } else if (at(StanTokenTypes.COMPLEX_VECTOR_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance();
            if (allowConstraints) parseTypeConstraint();
            parseSizeBracket1D();
            m.done(StanElementTypes.COMPLEX_VECTOR_TYPE);

        } else if (at(StanTokenTypes.COMPLEX_ROW_VECTOR_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance();
            if (allowConstraints) parseTypeConstraint();
            parseSizeBracket1D();
            m.done(StanElementTypes.COMPLEX_ROW_VECTOR_TYPE);

        } else if (at(StanTokenTypes.COMPLEX_MATRIX_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance();
            if (allowConstraints) parseTypeConstraint();
            parseSizeBracket2D();
            m.done(StanElementTypes.COMPLEX_MATRIX_TYPE);

        // ---- constrained-only types ----
        } else if (allowConstraints && at(StanTokenTypes.ORDERED_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance(); parseSizeBracket1D();
            m.done(StanElementTypes.ORDERED_TYPE);

        } else if (allowConstraints && at(StanTokenTypes.POSITIVE_ORDERED_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance(); parseSizeBracket1D();
            m.done(StanElementTypes.POSITIVE_ORDERED_TYPE);

        } else if (allowConstraints && at(StanTokenTypes.SIMPLEX_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance(); parseSizeBracket1D();
            m.done(StanElementTypes.SIMPLEX_TYPE);

        } else if (allowConstraints && at(StanTokenTypes.UNIT_VECTOR_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance(); parseSizeBracket1D();
            m.done(StanElementTypes.UNIT_VECTOR_TYPE);

        } else if (allowConstraints && at(StanTokenTypes.SUM_TO_ZERO_VECTOR_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance(); parseSizeBracket1D();
            m.done(StanElementTypes.SUM_TO_ZERO_VECTOR_TYPE);

        } else if (allowConstraints && at(StanTokenTypes.SUM_TO_ZERO_MATRIX_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance(); parseSizeBracket2D();
            m.done(StanElementTypes.SUM_TO_ZERO_MATRIX_TYPE);

        } else if (allowConstraints && at(StanTokenTypes.CHOLESKY_FACTOR_CORR_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance(); parseSizeBracket1D();
            m.done(StanElementTypes.CHOLESKY_FACTOR_CORR_TYPE);

        } else if (allowConstraints && at(StanTokenTypes.CHOLESKY_FACTOR_COV_KW)) {
            // Optional second dimension: cholesky_factor_cov[M] or cholesky_factor_cov[M, N]
            PsiBuilder.Marker m = builder.mark();
            advance();
            expect(StanTokenTypes.LBRACKET, "'[' expected");
            parseExpr();
            if (at(StanTokenTypes.COMMA)) { advance(); parseExpr(); }
            expect(StanTokenTypes.RBRACKET, "']' expected");
            m.done(StanElementTypes.CHOLESKY_FACTOR_COV_TYPE);

        } else if (allowConstraints && at(StanTokenTypes.CORR_MATRIX_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance(); parseSizeBracket1D();
            m.done(StanElementTypes.CORR_MATRIX_TYPE);

        } else if (allowConstraints && at(StanTokenTypes.COV_MATRIX_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance(); parseSizeBracket1D();
            m.done(StanElementTypes.COV_MATRIX_TYPE);

        } else if (allowConstraints && at(StanTokenTypes.COLUMN_STOCHASTIC_MATRIX_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance(); parseSizeBracket2D();
            m.done(StanElementTypes.COLUMN_STOCHASTIC_MATRIX_TYPE);

        } else if (allowConstraints && at(StanTokenTypes.ROW_STOCHASTIC_MATRIX_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance(); parseSizeBracket2D();
            m.done(StanElementTypes.ROW_STOCHASTIC_MATRIX_TYPE);

        } else if (at(StanTokenTypes.ARRAY_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance();
            expect(StanTokenTypes.LBRACKET, "'[' expected for array dimensions");
            parseExpr();
            while (at(StanTokenTypes.COMMA)) { advance(); parseExpr(); }
            expect(StanTokenTypes.RBRACKET, "']' expected for array dimensions");
            parseVarType(allowConstraints);
            m.done(StanElementTypes.ARRAY_TYPE);

        } else if (at(StanTokenTypes.TUPLE_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance();
            expect(StanTokenTypes.LPAREN, "'(' expected in tuple type");
            parseVarType(allowConstraints);
            while (at(StanTokenTypes.COMMA)) { advance(); parseVarType(allowConstraints); }
            expect(StanTokenTypes.RPAREN, "')' expected in tuple type");
            m.done(StanElementTypes.TUPLE_TYPE);

        } else {
            return false;
        }
        return true;
    }

    /** '[' expr ']' — one-dimensional size specification. */
    private void parseSizeBracket1D() {
        expect(StanTokenTypes.LBRACKET, "'[' expected");
        parseExpr();
        expect(StanTokenTypes.RBRACKET, "']' expected");
    }

    /** '[' expr ',' expr ']' — two-dimensional size specification. */
    private void parseSizeBracket2D() {
        expect(StanTokenTypes.LBRACKET, "'[' expected");
        parseExpr();
        expect(StanTokenTypes.COMMA, "',' expected");
        parseExpr();
        expect(StanTokenTypes.RBRACKET, "']' expected");
    }

    // -------------------------------------------------------------------------
    // Type constraints
    // -------------------------------------------------------------------------

    /**
     * typeConstraint: rangeConstraint | '<' offsetMult '>'
     *
     * Since rangeConstraint may also start with '<', we look ahead:
     * if after '<' we see LOWER_KW or UPPER_KW -> rangeConstraint
     * if after '<' we see OFFSET_KW or MULTIPLIER_KW -> offsetMult
     */
    private void parseTypeConstraint() {
        if (!at(StanTokenTypes.LESS)) return; // no constraint
        // peek what follows
        PsiBuilder.Marker probe = builder.mark();
        advance(); // consume '<'
        if (at(StanTokenTypes.OFFSET_KW) || at(StanTokenTypes.MULTIPLIER_KW)) {
            probe.rollbackTo();
            // parse '<' offsetMult '>'
            advance(); // '<'
            parseOffsetMult();
            expect(StanTokenTypes.GREATER, "'>' expected to close type constraint");
        } else {
            probe.rollbackTo();
            parseRangeConstraint();
        }
    }

    /**
     * rangeConstraint: (empty) | '<' range '>'
     */
    private void parseRangeConstraint() {
        if (!at(StanTokenTypes.LESS)) return; // no constraint
        advance(); // '<'
        parseRange();
        expect(StanTokenTypes.GREATER, "'>' expected to close range constraint");
    }

    /**
     * range:
     *   LOWER_KW '=' constraintExpr (',' UPPER_KW '=' constraintExpr)?
     *   | UPPER_KW '=' constraintExpr (',' LOWER_KW '=' constraintExpr)?
     */
    private void parseRange() {
        PsiBuilder.Marker m = builder.mark();
        if (at(StanTokenTypes.LOWER_KW)) {
            advance();
            expect(StanTokenTypes.ASSIGN, "'=' expected after 'lower'");
            parseConstraintExpr();
            if (at(StanTokenTypes.COMMA)) {
                advance();
                expect(StanTokenTypes.UPPER_KW, "'upper' expected");
                expect(StanTokenTypes.ASSIGN, "'=' expected after 'upper'");
                parseConstraintExpr();
            }
        } else if (at(StanTokenTypes.UPPER_KW)) {
            advance();
            expect(StanTokenTypes.ASSIGN, "'=' expected after 'upper'");
            parseConstraintExpr();
            if (at(StanTokenTypes.COMMA)) {
                advance();
                expect(StanTokenTypes.LOWER_KW, "'lower' expected");
                expect(StanTokenTypes.ASSIGN, "'=' expected after 'lower'");
                parseConstraintExpr();
            }
        } else {
            builder.error("'lower' or 'upper' expected in range constraint");
        }
        m.done(StanElementTypes.RANGE_CONSTRAINT);
    }

    /**
     * offsetMult:
     *   OFFSET_KW '=' constraintExpr (',' MULTIPLIER_KW '=' constraintExpr)?
     *   | MULTIPLIER_KW '=' constraintExpr (',' OFFSET_KW '=' constraintExpr)?
     */
    private void parseOffsetMult() {
        PsiBuilder.Marker m = builder.mark();
        if (at(StanTokenTypes.OFFSET_KW)) {
            advance();
            expect(StanTokenTypes.ASSIGN, "'=' expected after 'offset'");
            parseConstraintExpr();
            if (at(StanTokenTypes.COMMA)) {
                advance();
                expect(StanTokenTypes.MULTIPLIER_KW, "'multiplier' expected");
                expect(StanTokenTypes.ASSIGN, "'=' expected after 'multiplier'");
                parseConstraintExpr();
            }
        } else if (at(StanTokenTypes.MULTIPLIER_KW)) {
            advance();
            expect(StanTokenTypes.ASSIGN, "'=' expected after 'multiplier'");
            parseConstraintExpr();
            if (at(StanTokenTypes.COMMA)) {
                advance();
                expect(StanTokenTypes.OFFSET_KW, "'offset' expected");
                expect(StanTokenTypes.ASSIGN, "'=' expected after 'offset'");
                parseConstraintExpr();
            }
        } else {
            builder.error("'offset' or 'multiplier' expected in constraint");
        }
        m.done(StanElementTypes.OFFSET_MULT_CONSTRAINT);
    }

    /** constraintExpr: same as parseAdditive (no ternary/logical operators inside constraints) */
    private void parseConstraintExpr() {
        parseAdditive();
    }

    // -------------------------------------------------------------------------
    // Variable declarations
    // -------------------------------------------------------------------------

    /**
     * topVarDeclNoAssign: topVarType identifiers ';'
     * (no assignment initializers allowed — data/parameters blocks)
     */
    private boolean parseTopVarDeclNoAssign() {
        if (!isTopVarTypeStart()) return false;
        PsiBuilder.Marker m = builder.mark();
        parseTopVarType();
        parseDeclaredVars(false);
        expectSemicolon();
        m.done(StanElementTypes.VAR_DECL);
        return true;
    }

    /**
     * topVarDecl: topVarType declaredVars ';'
     */
    private boolean parseTopVarDecl() {
        if (!isTopVarTypeStart()) return false;
        PsiBuilder.Marker m = builder.mark();
        parseTopVarType();
        parseDeclaredVars(true);
        expectSemicolon();
        m.done(StanElementTypes.VAR_DECL);
        return true;
    }

    /**
     * varDecl: sizedBasicType declaredVars ';'
     */
    private boolean parseVarDecl() {
        if (!isSizedBasicTypeStart()) return false;
        PsiBuilder.Marker m = builder.mark();
        parseSizedBasicType();
        parseDeclaredVars(true);
        expectSemicolon();
        m.done(StanElementTypes.VAR_DECL);
        return true;
    }

    /**
     * declaredVars: declaredVar (',' declaredVar)*
     */
    private void parseDeclaredVars(boolean allowAssign) {
        parseDeclaredVar(allowAssign);
        while (at(StanTokenTypes.COMMA)) {
            advance(); // ','
            parseDeclaredVar(allowAssign);
        }
    }

    /**
     * declaredVar: IDENTIFIER ('[' dims ']')? ('=' expr)?
     */
    private void parseDeclaredVar(boolean allowAssign) {
        PsiBuilder.Marker m = builder.mark();
        expectName("Variable name expected");
        // optional old-style array dimensions on the name (deprecated): name[N]
        if (at(StanTokenTypes.LBRACKET)) {
            advance(); // '['
            parseExpr();
            while (at(StanTokenTypes.COMMA)) {
                advance();
                parseExpr();
            }
            expect(StanTokenTypes.RBRACKET, "']' expected");
        }
        if (allowAssign && at(StanTokenTypes.ASSIGN)) {
            advance(); // '='
            parseExpr();
        }
        m.done(StanElementTypes.DECLARED_VAR);
    }

    private void expectSemicolon() {
        if (!at(StanTokenTypes.SEMICOLON)) {
            builder.error("';' expected");
        } else {
            advance();
        }
    }

    /** True if the current token can start a topVarType. */
    private boolean isTopVarTypeStart() {
        return atAny(
            StanTokenTypes.INT_KW, StanTokenTypes.REAL_KW, StanTokenTypes.COMPLEX_KW,
            StanTokenTypes.VECTOR_KW, StanTokenTypes.ROW_VECTOR_KW, StanTokenTypes.MATRIX_KW,
            StanTokenTypes.COMPLEX_VECTOR_KW, StanTokenTypes.COMPLEX_ROW_VECTOR_KW,
            StanTokenTypes.COMPLEX_MATRIX_KW,
            StanTokenTypes.ORDERED_KW, StanTokenTypes.POSITIVE_ORDERED_KW,
            StanTokenTypes.SIMPLEX_KW, StanTokenTypes.UNIT_VECTOR_KW,
            StanTokenTypes.SUM_TO_ZERO_VECTOR_KW, StanTokenTypes.SUM_TO_ZERO_MATRIX_KW,
            StanTokenTypes.CHOLESKY_FACTOR_CORR_KW, StanTokenTypes.CHOLESKY_FACTOR_COV_KW,
            StanTokenTypes.CORR_MATRIX_KW, StanTokenTypes.COV_MATRIX_KW,
            StanTokenTypes.COLUMN_STOCHASTIC_MATRIX_KW, StanTokenTypes.ROW_STOCHASTIC_MATRIX_KW,
            StanTokenTypes.ARRAY_KW, StanTokenTypes.TUPLE_KW
        );
    }

    /** True if the current token can start a sizedBasicType. */
    private boolean isSizedBasicTypeStart() {
        return atAny(
            StanTokenTypes.INT_KW, StanTokenTypes.REAL_KW, StanTokenTypes.COMPLEX_KW,
            StanTokenTypes.VECTOR_KW, StanTokenTypes.ROW_VECTOR_KW, StanTokenTypes.MATRIX_KW,
            StanTokenTypes.COMPLEX_VECTOR_KW, StanTokenTypes.COMPLEX_ROW_VECTOR_KW,
            StanTokenTypes.COMPLEX_MATRIX_KW,
            StanTokenTypes.ARRAY_KW, StanTokenTypes.TUPLE_KW
        );
    }

    // -------------------------------------------------------------------------
    // Statements
    // -------------------------------------------------------------------------

    /**
     * Dispatch for contexts that allow top-level variable declarations with optional assignment.
     */
    private void parseTopVarDeclOrStatement() {
        if (isTopVarTypeStart()) {
            if (!parseTopVarDecl()) {
                parseStatement();
            }
        } else {
            parseStatement();
        }
    }

    /**
     * Dispatch for model block: sizedBasicType declarations or statements.
     */
    private void parseVarDeclOrStatement() {
        if (isSizedBasicTypeStart()) {
            if (!parseVarDecl()) {
                parseStatement();
            }
        } else {
            parseStatement();
        }
    }

    /**
     * statement: one of many statement forms.
     */
    private void parseStatement() {
        if (eof()) {
            builder.error("Statement expected but got end of file");
            return;
        }

        if (at(StanTokenTypes.SEMICOLON)) {
            // SKIP_STMT
            PsiBuilder.Marker m = builder.mark();
            advance();
            m.done(StanElementTypes.SKIP_STMT);

        } else if (at(StanTokenTypes.LBRACE)) {
            parseBlockStatement();

        } else if (at(StanTokenTypes.IF_KW)) {
            parseIfStatement();

        } else if (at(StanTokenTypes.WHILE_KW)) {
            parseWhileStatement();

        } else if (at(StanTokenTypes.FOR_KW)) {
            parseForStatement();

        } else if (at(StanTokenTypes.PROFILE_KW)) {
            parseProfileStatement();

        } else if (at(StanTokenTypes.BREAK_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance();
            expectSemicolon();
            m.done(StanElementTypes.BREAK_STMT);

        } else if (at(StanTokenTypes.CONTINUE_KW)) {
            PsiBuilder.Marker m = builder.mark();
            advance();
            expectSemicolon();
            m.done(StanElementTypes.CONTINUE_STMT);

        } else if (at(StanTokenTypes.RETURN_KW)) {
            parseReturnStatement();

        } else if (at(StanTokenTypes.PRINT_KW)) {
            parsePrintStatement(StanElementTypes.PRINT_STMT);

        } else if (at(StanTokenTypes.REJECT_KW)) {
            parsePrintStatement(StanElementTypes.REJECT_STMT);

        } else if (at(StanTokenTypes.FATAL_ERROR_KW)) {
            parsePrintStatement(StanElementTypes.FATAL_ERROR_STMT);

        } else if (at(StanTokenTypes.TARGET_KW)) {
            parseTargetPlusAssignStatement();

        } else if (at(StanTokenTypes.JACOBIAN_KW)) {
            parseJacobianPlusAssignStatement();

        } else {
            // Could be: assignment, function-call statement, or tilde statement.
            // Use marker rollback to disambiguate.
            parseExpressionStatement();
        }
    }

    /** '{' (varDeclOrStatement)* '}' */
    private void parseBlockStatement() {
        PsiBuilder.Marker m = builder.mark();
        advance(); // '{'
        while (!at(StanTokenTypes.RBRACE) && !eof()) {
            parseVarDeclOrStatement();
        }
        expect(StanTokenTypes.RBRACE, "'}' expected to close block");
        m.done(StanElementTypes.BLOCK_STMT);
    }

    /** IF_KW '(' expr ')' statement ('else' statement)? */
    private void parseIfStatement() {
        PsiBuilder.Marker m = builder.mark();
        advance(); // IF_KW
        expect(StanTokenTypes.LPAREN, "'(' expected after 'if'");
        parseExpr();
        expect(StanTokenTypes.RPAREN, "')' expected after if condition");
        parseStatement();
        if (at(StanTokenTypes.ELSE_KW)) {
            advance();
            parseStatement();
        }
        m.done(StanElementTypes.IF_STMT);
    }

    /** WHILE_KW '(' expr ')' statement */
    private void parseWhileStatement() {
        PsiBuilder.Marker m = builder.mark();
        advance(); // WHILE_KW
        expect(StanTokenTypes.LPAREN, "'(' expected after 'while'");
        parseExpr();
        expect(StanTokenTypes.RPAREN, "')' expected after while condition");
        parseStatement();
        m.done(StanElementTypes.WHILE_STMT);
    }

    /**
     * FOR_KW '(' IDENTIFIER IN_KW expr (':' expr)? ')' statement
     *
     * If ':' follows the in-expr -> FOR_RANGE_STMT
     * Otherwise -> FOR_EACH_STMT
     *
     * Note: ':' is not a binary operator in Stan expressions, so parseExpr() stops before it.
     * Ternary consumes ':' only after '?', so there is no ambiguity.
     */
    private void parseForStatement() {
        PsiBuilder.Marker m = builder.mark();
        advance(); // FOR_KW
        expect(StanTokenTypes.LPAREN, "'(' expected after 'for'");
        expectName("Loop variable name expected");
        expect(StanTokenTypes.IN_KW, "'in' expected in for loop");
        parseExpr();
        if (at(StanTokenTypes.COLON)) {
            advance(); // ':'
            parseExpr();
            expect(StanTokenTypes.RPAREN, "')' expected after for range");
            parseStatement();
            m.done(StanElementTypes.FOR_RANGE_STMT);
        } else {
            expect(StanTokenTypes.RPAREN, "')' expected after for-each expression");
            parseStatement();
            m.done(StanElementTypes.FOR_EACH_STMT);
        }
    }

    /** PROFILE_KW '(' STRING_LITERAL ')' '{' statement* '}' */
    private void parseProfileStatement() {
        PsiBuilder.Marker m = builder.mark();
        advance(); // PROFILE_KW
        expect(StanTokenTypes.LPAREN, "'(' expected after 'profile'");
        expect(StanTokenTypes.STRING_LITERAL, "String literal expected as profile name");
        expect(StanTokenTypes.RPAREN, "')' expected after profile name");
        expect(StanTokenTypes.LBRACE, "'{' expected");
        while (!at(StanTokenTypes.RBRACE) && !eof()) {
            parseStatement();
        }
        expect(StanTokenTypes.RBRACE, "'}' expected to close profile block");
        m.done(StanElementTypes.PROFILE_STMT);
    }

    /** RETURN_KW expr? ';' */
    private void parseReturnStatement() {
        PsiBuilder.Marker m = builder.mark();
        advance(); // RETURN_KW
        if (!at(StanTokenTypes.SEMICOLON) && !eof()) {
            parseExpr();
        }
        expectSemicolon();
        m.done(StanElementTypes.RETURN_STMT);
    }

    /** PRINT_KW / REJECT_KW / FATAL_ERROR_KW '(' printables ')' ';' */
    private void parsePrintStatement(IElementType elementType) {
        PsiBuilder.Marker m = builder.mark();
        advance(); // keyword
        expect(StanTokenTypes.LPAREN, "'(' expected");
        parsePrintables();
        expect(StanTokenTypes.RPAREN, "')' expected");
        expectSemicolon();
        m.done(elementType);
    }

    /** TARGET_KW '+=' expr ';' */
    private void parseTargetPlusAssignStatement() {
        PsiBuilder.Marker m = builder.mark();
        advance(); // TARGET_KW
        expect(StanTokenTypes.PLUS_ASSIGN, "'+=' expected after 'target'");
        parseExpr();
        expectSemicolon();
        m.done(StanElementTypes.TARGET_PLUS_ASSIGN_STMT);
    }

    /** JACOBIAN_KW '+=' expr ';' */
    private void parseJacobianPlusAssignStatement() {
        PsiBuilder.Marker m = builder.mark();
        advance(); // JACOBIAN_KW
        expect(StanTokenTypes.PLUS_ASSIGN, "'+=' expected after 'jacobian'");
        parseExpr();
        expectSemicolon();
        m.done(StanElementTypes.JACOBIAN_PLUS_ASSIGN_STMT);
    }

    /**
     * Handles: assignment ';' | functionCallStmt ';' | tilde statement
     *
     * Strategy:
     * 1. If current token is IDENTIFIER/BUILTIN_FUNCTION followed by '(' -> might be FUN_CALL_STMT
     * 2. Try to parse an lvalue. If an assignment op follows -> ASSIGNMENT_STMT.
     * 3. If TILDE follows after an expression -> TILDE_STMT.
     * 4. Otherwise treat as expression statement (error recovery).
     *
     * We use marker rollback for disambiguation.
     */
    private void parseExpressionStatement() {
        // Try: assignment (lvalue assignOp expr) or tilde or function-call statement.
        // We parse the expression and then check what follows.
        PsiBuilder.Marker outerMark = builder.mark();
        // Parse as lvalue attempt
        boolean parsedLvalue = tryParseLvalue();

        if (parsedLvalue && isAssignOp()) {
            // It's an assignment statement
            advance(); // assignment operator
            parseExpr();
            expectSemicolon();
            outerMark.done(StanElementTypes.ASSIGNMENT_STMT);
            return;
        }

        // Not an assignment. Roll back and try parsing as an expression (for tilde or error)
        outerMark.rollbackTo();

        PsiBuilder.Marker exprMark = builder.mark();
        parseExpr();
        if (at(StanTokenTypes.TILDE)) {
            // expr '~' name '(' args ')' truncation? ';'
            advance(); // '~'
            if (!isName()) {
                builder.error("Distribution name expected after '~'");
            } else {
                advance(); // distribution name
            }
            expect(StanTokenTypes.LPAREN, "'(' expected");
            parseArgList();
            expect(StanTokenTypes.RPAREN, "')' expected");
            if (isTruncationStart()) {
                parseTruncation();
            }
            expectSemicolon();
            exprMark.done(StanElementTypes.TILDE_STMT);
        } else {
            // Not a tilde either — could be some expression statement or error
            expectSemicolon();
            exprMark.done(StanElementTypes.FUN_CALL_STMT); // best-effort
        }
    }

    /**
     * Try to parse an lvalue. Returns true on success.
     * An lvalue is: IDENTIFIER ('[' indices ']')* ('.' INT_LITERAL)*
     *             | '(' lvalue (',' lvalue)+ ')'
     */
    private boolean tryParseLvalue() {
        if (at(StanTokenTypes.LPAREN)) {
            // Tuple lvalue: '(' lvalue (',' lvalue)+ ')'
            PsiBuilder.Marker m = builder.mark();
            advance(); // '('
            if (!tryParseLvalue()) {
                m.rollbackTo();
                return false;
            }
            if (!at(StanTokenTypes.COMMA)) {
                m.rollbackTo();
                return false;
            }
            while (at(StanTokenTypes.COMMA)) {
                advance();
                tryParseLvalue();
            }
            if (!at(StanTokenTypes.RPAREN)) {
                m.rollbackTo();
                return false;
            }
            advance(); // ')'
            m.done(StanElementTypes.TUPLE_DECL_PACK);
            return true;
        }

        if (!isName()) return false;
        advance(); // variable name

        // Optional index subscripts
        while (at(StanTokenTypes.LBRACKET)) {
            advance(); // '['
            parseIndexList();
            expect(StanTokenTypes.RBRACKET, "']' expected");
        }
        // Optional tuple projections: .N
        while (at(StanTokenTypes.DOT)) {
            advance(); // '.'
            if (!at(StanTokenTypes.INT_LITERAL)) {
                builder.error("Integer literal expected after '.' in tuple projection");
            } else {
                advance();
            }
        }
        return true;
    }

    private boolean isAssignOp() {
        return atAny(
            StanTokenTypes.ASSIGN, StanTokenTypes.PLUS_ASSIGN, StanTokenTypes.MINUS_ASSIGN,
            StanTokenTypes.TIMES_ASSIGN, StanTokenTypes.DIVIDE_ASSIGN,
            StanTokenTypes.ELT_TIMES_ASSIGN, StanTokenTypes.ELT_DIVIDE_ASSIGN,
            StanTokenTypes.ARROW
        );
    }

    // -------------------------------------------------------------------------
    // Printables
    // -------------------------------------------------------------------------

    /** printables: printable (',' printable)* */
    private void parsePrintables() {
        parsePrintable();
        while (at(StanTokenTypes.COMMA)) {
            advance();
            parsePrintable();
        }
    }

    /** printable: STRING_LITERAL | expr */
    private void parsePrintable() {
        if (at(StanTokenTypes.STRING_LITERAL)) {
            advance();
        } else {
            parseExpr();
        }
    }

    // -------------------------------------------------------------------------
    // Truncation
    // -------------------------------------------------------------------------

    /** truncation: TRUNCATE_KW '[' expr? ',' expr? ']' */
    private boolean isTruncationStart() {
        return at(StanTokenTypes.TRUNCATE_KW);
    }

    private void parseTruncation() {
        PsiBuilder.Marker m = builder.mark();
        advance(); // TRUNCATE_KW 'T'
        expect(StanTokenTypes.LBRACKET, "'[' expected for truncation");
        // optional lower bound
        if (!at(StanTokenTypes.COMMA)) {
            parseExpr();
        }
        expect(StanTokenTypes.COMMA, "',' expected in truncation");
        // optional upper bound
        if (!at(StanTokenTypes.RBRACKET)) {
            parseExpr();
        }
        expect(StanTokenTypes.RBRACKET, "']' expected to close truncation");
        m.done(StanElementTypes.TRUNCATION);
    }

    // -------------------------------------------------------------------------
    // Expressions — top-level entry
    // -------------------------------------------------------------------------

    private void parseExpr() {
        parseTernary();
    }

    // -------------------------------------------------------------------------
    // Ternary: e ? e : e  (right-associative)
    // -------------------------------------------------------------------------

    private void parseTernary() {
        PsiBuilder.Marker m = builder.mark();
        parseOr();
        if (at(StanTokenTypes.QUESTION)) {
            advance(); // '?'
            parseTernary();
            expect(StanTokenTypes.COLON, "':' expected in ternary expression");
            parseTernary();
            m.done(StanElementTypes.TERNARY_IF_EXPR);
        } else {
            m.drop();
        }
    }

    // -------------------------------------------------------------------------
    // Logical OR: e || e
    // -------------------------------------------------------------------------

    private void parseOr() {
        PsiBuilder.Marker left = builder.mark();
        parseAnd();
        while (at(StanTokenTypes.OR)) {
            advance();
            parseAnd();
            PsiBuilder.Marker newLeft = left.precede();
            left.done(StanElementTypes.BINARY_OP_EXPR);
            left = newLeft;
        }
        left.drop();
    }

    // -------------------------------------------------------------------------
    // Logical AND: e && e
    // -------------------------------------------------------------------------

    private void parseAnd() {
        PsiBuilder.Marker left = builder.mark();
        parseEquality();
        while (at(StanTokenTypes.AND)) {
            advance();
            parseEquality();
            PsiBuilder.Marker newLeft = left.precede();
            left.done(StanElementTypes.BINARY_OP_EXPR);
            left = newLeft;
        }
        left.drop();
    }

    // -------------------------------------------------------------------------
    // Equality: e == e | e != e
    // -------------------------------------------------------------------------

    private void parseEquality() {
        PsiBuilder.Marker left = builder.mark();
        parseComparison();
        while (atAny(StanTokenTypes.EQUALS, StanTokenTypes.NEQUALS)) {
            advance();
            parseComparison();
            PsiBuilder.Marker newLeft = left.precede();
            left.done(StanElementTypes.BINARY_OP_EXPR);
            left = newLeft;
        }
        left.drop();
    }

    // -------------------------------------------------------------------------
    // Comparison: e < e | e <= e | e > e | e >= e
    // -------------------------------------------------------------------------

    private void parseComparison() {
        PsiBuilder.Marker left = builder.mark();
        parseAdditive();
        while (atAny(StanTokenTypes.LESS, StanTokenTypes.LEQ,
                     StanTokenTypes.GREATER, StanTokenTypes.GEQ)) {
            advance();
            parseAdditive();
            PsiBuilder.Marker newLeft = left.precede();
            left.done(StanElementTypes.BINARY_OP_EXPR);
            left = newLeft;
        }
        left.drop();
    }

    // -------------------------------------------------------------------------
    // Additive: e + e | e - e
    // -------------------------------------------------------------------------

    private void parseAdditive() {
        PsiBuilder.Marker left = builder.mark();
        parseMultiplicative();
        while (atAny(StanTokenTypes.PLUS, StanTokenTypes.MINUS)) {
            advance();
            parseMultiplicative();
            PsiBuilder.Marker newLeft = left.precede();
            left.done(StanElementTypes.BINARY_OP_EXPR);
            left = newLeft;
        }
        left.drop();
    }

    // -------------------------------------------------------------------------
    // Multiplicative: e * e | e / e | e %/% e | e % e | e \ e
    // -------------------------------------------------------------------------

    private void parseMultiplicative() {
        PsiBuilder.Marker left = builder.mark();
        parseUnary();
        while (atAny(StanTokenTypes.TIMES, StanTokenTypes.DIVIDE,
                     StanTokenTypes.MODULO, StanTokenTypes.IDIVIDE,
                     StanTokenTypes.LDIVIDE)) {
            advance();
            parseUnary();
            PsiBuilder.Marker newLeft = left.precede();
            left.done(StanElementTypes.BINARY_OP_EXPR);
            left = newLeft;
        }
        left.drop();
    }

    // -------------------------------------------------------------------------
    // Prefix unary: ! e | - e | + e
    // -------------------------------------------------------------------------

    private void parseUnary() {
        if (atAny(StanTokenTypes.BANG, StanTokenTypes.MINUS, StanTokenTypes.PLUS)) {
            PsiBuilder.Marker m = builder.mark();
            advance();
            parseUnary();
            m.done(StanElementTypes.PREFIX_OP_EXPR);
        } else {
            parsePower();
        }
    }

    // -------------------------------------------------------------------------
    // Power: e ^ e  (right-associative; rhs calls parseUnary)
    // -------------------------------------------------------------------------

    private void parsePower() {
        PsiBuilder.Marker left = builder.mark();
        parseEltOp();
        if (at(StanTokenTypes.POW)) {
            advance();
            // right-associative: rhs is parseUnary, which will call parsePower again via parseUnary
            parseUnary();
            PsiBuilder.Marker newLeft = left.precede();
            left.done(StanElementTypes.BINARY_OP_EXPR);
            left = newLeft;
        }
        left.drop();
    }

    // -------------------------------------------------------------------------
    // Element-wise: e .* e | e ./ e | e .^ e
    // -------------------------------------------------------------------------

    private void parseEltOp() {
        PsiBuilder.Marker left = builder.mark();
        parsePostfix();
        while (atAny(StanTokenTypes.ELT_TIMES, StanTokenTypes.ELT_DIVIDE, StanTokenTypes.ELT_POW)) {
            advance();
            parsePostfix();
            PsiBuilder.Marker newLeft = left.precede();
            left.done(StanElementTypes.BINARY_OP_EXPR);
            left = newLeft;
        }
        left.drop();
    }

    // -------------------------------------------------------------------------
    // Postfix / atom with suffixes
    // -------------------------------------------------------------------------

    /**
     * parsePostfix: parseAtom then apply suffixes:
     *   '         -> POSTFIX_OP_EXPR (transpose)
     *   '[' indices ']' -> INDEXED_EXPR
     *   '.' INT_LITERAL -> TUPLE_PROJECTION_EXPR
     */
    private void parsePostfix() {
        PsiBuilder.Marker left = builder.mark();
        parseAtom();
        while (true) {
            if (at(StanTokenTypes.TRANSPOSE)) {
                advance();
                PsiBuilder.Marker newLeft = left.precede();
                left.done(StanElementTypes.POSTFIX_OP_EXPR);
                left = newLeft;
            } else if (at(StanTokenTypes.LBRACKET)) {
                advance(); // '['
                parseIndexList();
                expect(StanTokenTypes.RBRACKET, "']' expected");
                PsiBuilder.Marker newLeft = left.precede();
                left.done(StanElementTypes.INDEXED_EXPR);
                left = newLeft;
            } else if (at(StanTokenTypes.DOT)) {
                advance(); // '.'
                if (!at(StanTokenTypes.INT_LITERAL)) {
                    builder.error("Integer literal expected after '.' for tuple projection");
                } else {
                    advance();
                }
                PsiBuilder.Marker newLeft = left.precede();
                left.done(StanElementTypes.TUPLE_PROJECTION_EXPR);
                left = newLeft;
            } else {
                break;
            }
        }
        left.drop();
    }

    // -------------------------------------------------------------------------
    // Atoms
    // -------------------------------------------------------------------------

    private void parseAtom() {
        if (at(StanTokenTypes.INT_LITERAL)) {
            PsiBuilder.Marker m = builder.mark();
            advance();
            m.done(StanElementTypes.INT_LITERAL_EXPR);

        } else if (at(StanTokenTypes.REAL_LITERAL)) {
            PsiBuilder.Marker m = builder.mark();
            advance();
            m.done(StanElementTypes.REAL_LITERAL_EXPR);

        } else if (at(StanTokenTypes.IMAG_LITERAL)) {
            PsiBuilder.Marker m = builder.mark();
            advance();
            m.done(StanElementTypes.IMAG_LITERAL_EXPR);

        } else if (at(StanTokenTypes.TARGET_KW)) {
            // target() expression
            PsiBuilder.Marker m = builder.mark();
            advance(); // TARGET_KW
            expect(StanTokenTypes.LPAREN, "'(' expected after 'target'");
            expect(StanTokenTypes.RPAREN, "')' expected after 'target('");
            m.done(StanElementTypes.TARGET_EXPR);

        } else if (isName()) {
            parseNameAtom();

        } else if (at(StanTokenTypes.LBRACE)) {
            parseArrayExpr();

        } else if (at(StanTokenTypes.LBRACKET)) {
            parseRowVectorExpr();

        } else if (at(StanTokenTypes.LPAREN)) {
            parseParenOrTupleExpr();

        } else {
            builder.error("Expression expected");
            if (!eof()) advance(); // skip bad token for error recovery
        }
    }

    /**
     * Name-started atoms:
     *   name '(' args '|' args ')' -> COND_DIST_EXPR
     *   name '(' args ')'          -> FUN_CALL_EXPR
     *   name                        -> VARIABLE_EXPR
     */
    private void parseNameAtom() {
        PsiBuilder.Marker m = builder.mark();
        String fnName = builder.getTokenText();
        advance(); // name
        if (at(StanTokenTypes.LPAREN)) {
            advance(); // '('
            parseArgList();
            if (at(StanTokenTypes.BAR)) {
                if (!hasConditioningSuffix(fnName)) {
                    PsiBuilder.Marker errMark = builder.mark();
                    advance(); // '|'
                    errMark.error("'|' notation is only allowed in functions with a conditioning suffix "
                            + "(_lpdf, _lpmf, _cdf, _lcdf, _lccdf)");
                } else {
                    advance(); // '|'
                }
                parseArgList();
                expect(StanTokenTypes.RPAREN, "')' expected");
                m.done(StanElementTypes.COND_DIST_EXPR);
            } else {
                expect(StanTokenTypes.RPAREN, "')' expected");
                m.done(StanElementTypes.FUN_CALL_EXPR);
            }
        } else {
            m.done(StanElementTypes.VARIABLE_EXPR);
        }
    }

    /** '{' (expr (',' expr)*)? '}' */
    private void parseArrayExpr() {
        PsiBuilder.Marker m = builder.mark();
        advance(); // '{'
        if (!at(StanTokenTypes.RBRACE)) {
            parseExpr();
            while (at(StanTokenTypes.COMMA)) {
                advance();
                parseExpr();
            }
        }
        expect(StanTokenTypes.RBRACE, "'}' expected to close array expression");
        m.done(StanElementTypes.ARRAY_EXPR);
    }

    /** '[' (expr (',' expr)*)? ']' */
    private void parseRowVectorExpr() {
        PsiBuilder.Marker m = builder.mark();
        advance(); // '['
        if (!at(StanTokenTypes.RBRACKET)) {
            parseExpr();
            while (at(StanTokenTypes.COMMA)) {
                advance();
                parseExpr();
            }
        }
        expect(StanTokenTypes.RBRACKET, "']' expected to close row vector expression");
        m.done(StanElementTypes.ROW_VECTOR_EXPR);
    }

    /**
     * '(' expr ')'         -> PAREN_EXPR
     * '(' expr (',' expr)+ ')' -> TUPLE_EXPR
     */
    private void parseParenOrTupleExpr() {
        PsiBuilder.Marker m = builder.mark();
        advance(); // '('
        parseExpr();
        if (at(StanTokenTypes.COMMA)) {
            // It's a tuple
            while (at(StanTokenTypes.COMMA)) {
                advance();
                parseExpr();
            }
            expect(StanTokenTypes.RPAREN, "')' expected to close tuple expression");
            m.done(StanElementTypes.TUPLE_EXPR);
        } else {
            expect(StanTokenTypes.RPAREN, "')' expected");
            m.done(StanElementTypes.PAREN_EXPR);
        }
    }

    // -------------------------------------------------------------------------
    // Argument lists
    // -------------------------------------------------------------------------

    /** args: (expr (',' expr)*)? wrapped in ARG_LIST */
    private void parseArgList() {
        PsiBuilder.Marker m = builder.mark();
        // Empty arg list is valid
        if (!at(StanTokenTypes.RPAREN) && !at(StanTokenTypes.BAR) && !eof()) {
            parseExpr();
            while (at(StanTokenTypes.COMMA)) {
                advance();
                parseExpr();
            }
        }
        m.done(StanElementTypes.ARG_LIST);
    }

    // -------------------------------------------------------------------------
    // Index lists
    // -------------------------------------------------------------------------

    /**
     * indices: index (',' index)*   wrapped in INDEX_LIST
     *
     * index:
     *   ':'          -> ALL_INDEX
     *   expr ':'     -> UPFROM_INDEX
     *   ':' expr     -> DOWNFROM_INDEX
     *   expr ':' expr -> BETWEEN_INDEX
     *   expr          -> SINGLE_INDEX
     */
    private void parseIndexList() {
        PsiBuilder.Marker m = builder.mark();
        parseIndex();
        while (at(StanTokenTypes.COMMA)) {
            advance();
            parseIndex();
        }
        m.done(StanElementTypes.INDEX_LIST);
    }

    private void parseIndex() {
        if (at(StanTokenTypes.COLON)) {
            // ':' or ':' expr
            PsiBuilder.Marker m = builder.mark();
            advance(); // ':'
            if (!at(StanTokenTypes.RBRACKET) && !at(StanTokenTypes.COMMA) && !eof()) {
                // DOWNFROM_INDEX: : expr
                parseExpr();
                m.done(StanElementTypes.DOWNFROM_INDEX);
            } else {
                // ALL_INDEX: just ':'
                m.done(StanElementTypes.ALL_INDEX);
            }
        } else {
            // expr followed by ':' or nothing
            PsiBuilder.Marker m = builder.mark();
            parseExpr();
            if (at(StanTokenTypes.COLON)) {
                advance(); // ':'
                if (!at(StanTokenTypes.RBRACKET) && !at(StanTokenTypes.COMMA) && !eof()) {
                    // BETWEEN_INDEX: expr : expr
                    parseExpr();
                    m.done(StanElementTypes.BETWEEN_INDEX);
                } else {
                    // UPFROM_INDEX: expr :
                    m.done(StanElementTypes.UPFROM_INDEX);
                }
            } else {
                // SINGLE_INDEX: just expr
                m.done(StanElementTypes.SINGLE_INDEX);
            }
        }
    }
}
