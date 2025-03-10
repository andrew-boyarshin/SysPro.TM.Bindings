package syspro.tm.parser;

public enum SyntaxKind implements AnySyntaxKind {
    // Terminals
    BAD,
    INDENT,
    DEDENT,
    IDENTIFIER,
    BOOLEAN,
    INTEGER,
    RUNE,
    STRING,
    // @see Keyword
    // @see Symbol

    // Non-terminals
    SOURCE_TEXT, // LIST[TYPE_DEFINITION]
    TYPE_BOUND, // BOUND SEPARATED_LIST[NameExpression, AMPERSAND]
    LIST, // any number of nodes
    SEPARATED_LIST, // any number of nodes (separator terminals are expected to be at odd indices)

    // Definition:
    TYPE_DEFINITION, // Terminal IDENTIFIER LESS_THAN? SEPARATED_LIST[TYPE_PARAMETER_DEFINITION, COMMA]? GREATER_THAN? TYPE_BOUND? INDENT? LIST[Definition]? DEDENT?
    FUNCTION_DEFINITION, // LIST[Terminal] DEF Terminal OPEN_PAREN SEPARATED_LIST[PARAMETER_DEFINITION, COMMA]? CLOSE_PAREN COLON? NameExpression? INDENT? LIST[Statement]? DEDENT?
    VARIABLE_DEFINITION, // (VAR | VAL) IDENTIFIER COLON? NameExpression? EQUALS? Expression?
    TYPE_PARAMETER_DEFINITION, // IDENTIFIER TYPE_BOUND?
    PARAMETER_DEFINITION, // IDENTIFIER COLON NameExpression

    // Statement:
    VARIABLE_DEFINITION_STATEMENT, // VARIABLE_DEFINITION
    ASSIGNMENT_STATEMENT, // Primary EQUALS Expression
    EXPRESSION_STATEMENT, // Expression
    RETURN_STATEMENT, // RETURN Expression?
    BREAK_STATEMENT, // BREAK
    CONTINUE_STATEMENT, // CONTINUE
    IF_STATEMENT, // IF Expression INDENT? LIST[Statement]? DEDENT? ELSE? INDENT? LIST[Statement]? DEDENT?
    WHILE_STATEMENT, // WHILE Expression INDENT? LIST[Statement]? DEDENT?
    FOR_STATEMENT, // FOR Primary IN Expression INDENT? LIST[Statement]? DEDENT?

    // Expression:
    LOGICAL_AND_EXPRESSION, // Expression AMPERSAND_AMPERSAND Expression
    LOGICAL_OR_EXPRESSION, // Expression BAR_BAR Expression
    LOGICAL_NOT_EXPRESSION, // EXCLAMATION Expression
    EQUALS_EXPRESSION, // Expression EQUALS_EQUALS Expression
    NOT_EQUALS_EXPRESSION, // Expression EXCLAMATION_EQUALS Expression
    LESS_THAN_EXPRESSION, // Expression LESS_THAN Expression
    LESS_THAN_OR_EQUAL_EXPRESSION, // Expression LESS_THAN_EQUALS Expression
    GREATER_THAN_EXPRESSION, // Expression GREATER_THAN Expression
    GREATER_THAN_OR_EQUAL_EXPRESSION, // Expression GREATER_THAN_EQUALS Expression
    IS_EXPRESSION, // Expression IS NameExpression IDENTIFIER?
    BITWISE_AND_EXPRESSION, // Expression AMPERSAND Expression
    BITWISE_OR_EXPRESSION, // Expression BAR Expression
    BITWISE_EXCLUSIVE_OR_EXPRESSION, // Expression CARET Expression
    BITWISE_LEFT_SHIFT_EXPRESSION, // Expression LESS_THAN_LESS_THAN Expression
    BITWISE_RIGHT_SHIFT_EXPRESSION, // Expression GREATER_THAN_GREATER_THAN Expression
    ADD_EXPRESSION, // Expression PLUS Expression
    SUBTRACT_EXPRESSION, // Expression MINUS Expression
    MULTIPLY_EXPRESSION, // Expression ASTERISK Expression
    DIVIDE_EXPRESSION, // Expression SLASH Expression
    MODULO_EXPRESSION, // Expression PERCENT Expression
    UNARY_PLUS_EXPRESSION, // PLUS Expression
    UNARY_MINUS_EXPRESSION, // MINUS Expression
    BITWISE_NOT_EXPRESSION, // TILDE Expression
    // Primary:
    MEMBER_ACCESS_EXPRESSION, // Primary DOT IDENTIFIER
    INVOCATION_EXPRESSION, // Primary OPEN_PAREN SEPARATED_LIST[Expression, COMMA] CLOSE_PAREN
    INDEX_EXPRESSION, // Primary OPEN_BRACKET Expression CLOSE_BRACKET
    THIS_EXPRESSION, // THIS
    SUPER_EXPRESSION, // SUPER
    NULL_LITERAL_EXPRESSION, // NULL
    TRUE_LITERAL_EXPRESSION, // BOOLEAN
    FALSE_LITERAL_EXPRESSION, // BOOLEAN
    STRING_LITERAL_EXPRESSION, // STRING
    RUNE_LITERAL_EXPRESSION, // RUNE
    INTEGER_LITERAL_EXPRESSION, // INTEGER
    PARENTHESIZED_EXPRESSION, // OPEN_PAREN Expression CLOSE_PAREN
    // NameExpression:
    IDENTIFIER_NAME_EXPRESSION, // IDENTIFIER
    OPTION_NAME_EXPRESSION, // QUESTION NameExpression
    GENERIC_NAME_EXPRESSION, // IDENTIFIER LESS_THAN SEPARATED_LIST[NameExpression, COMMA] GREATER_THAN
    ;

    @Override
    public boolean isTerminal() {
        final var ordinal = this.ordinal();
        return ordinal < SOURCE_TEXT.ordinal();
    }

    @Override
    public boolean isNonTerminal() {
        final var ordinal = this.ordinal();
        return ordinal >= SOURCE_TEXT.ordinal();
    }

    @Override
    public boolean isDefinition() {
        final var ordinal = this.ordinal();
        return ordinal >= TYPE_DEFINITION.ordinal() && ordinal <= PARAMETER_DEFINITION.ordinal();
    }

    @Override
    public boolean isMemberDefinition() {
        return this == FUNCTION_DEFINITION || this == VARIABLE_DEFINITION;
    }

    @Override
    public boolean isStatement() {
        final var ordinal = this.ordinal();
        return ordinal >= VARIABLE_DEFINITION_STATEMENT.ordinal() && ordinal <= FOR_STATEMENT.ordinal();
    }

    @Override
    public boolean isExpression() {
        final var ordinal = this.ordinal();
        return ordinal >= LOGICAL_AND_EXPRESSION.ordinal();
    }

    @Override
    public boolean isPrimaryExpression() {
        final var ordinal = this.ordinal();
        return ordinal >= MEMBER_ACCESS_EXPRESSION.ordinal();
    }

    @Override
    public boolean isNameExpression() {
        final var ordinal = this.ordinal();
        return ordinal >= IDENTIFIER_NAME_EXPRESSION.ordinal();
    }
}
