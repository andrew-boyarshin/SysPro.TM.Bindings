package syspro.tm.parser;

public enum SyntaxKind implements AnySyntaxKind {
    // Terminals
    Bad,
    Indent,
    Dedent,
    Identifier,
    Boolean,
    Integer,
    Rune,
    String,
    // @see Keyword
    // @see Symbol

    // Non-terminals
    SourceText, // List[ClassDefinition]
    TypeBound, // Bound SeparatedList[TypeName, Ampersand]
    List,
    SeparatedList,

    TypeDefinition, // Terminal Identifier LessThan? SeparatedList[TypeParameterDefinition, Comma]? GreaterThan? TypeBound? Indent? List[Definition]? Dedent?
    FunctionDefinition, // List[Terminal] Def Terminal OpenParen SeparatedList[ParameterDefinition, Comma]? CloseParen Colon? TypeName? Indent? List[Statement]? Dedent?
    VariableDefinition, // (Var | Val) Identifier Colon? TypeName? Equals? Expression?
    TypeParameterDefinition, // Identifier TypeBound?
    ParameterDefinition, // Identifier Colon TypeName

    VariableDefinitionStatement, // VariableDefinition
    AssignmentStatement, // Primary Equals Expression
    ExpressionStatement, // Expression
    ReturnStatement, // Return Expression?
    BreakStatement, // Break
    ContinueStatement, // Continue
    IfStatement, // If Expression Indent? List[Statement]? Dedent? Else? Indent? List[Statement]? Dedent?
    WhileStatement, // While Expression Indent? List[Statement]? Dedent?
    ForStatement, // For Primary In Expression Indent? List[Statement]? Dedent?

    LogicalAndExpression,
    LogicalOrExpression,
    LogicalNotExpression,
    EqualsExpression,
    NotEqualsExpression,
    LessThanExpression,
    LessThanOrEqualExpression,
    GreaterThanExpression,
    GreaterThanOrEqualExpression,
    IsExpression, // Expression Is TypeName Identifier?
    BitwiseAndExpression,
    BitwiseOrExpression,
    BitwiseExclusiveOrExpression,
    BitwiseLeftShiftExpression,
    BitwiseRightShiftExpression,
    AddExpression,
    SubtractExpression,
    MultiplyExpression,
    DivideExpression,
    ModuloExpression,
    UnaryPlusExpression, // Plus Expression
    UnaryMinusExpression, // Minus Expression
    BitwiseNotExpression, // Tilde Expression
    MemberAccessExpression, // Primary Dot Identifier
    InvocationExpression, // Primary OpenParen SeparatedList[Expression, Comma] CloseParen
    IndexExpression, // Primary OpenBracket Expression CloseBracket
    ThisExpression, // This
    SuperExpression, // Super
    NullLiteralExpression, // Null
    TrueLiteralExpression, // Boolean
    FalseLiteralExpression, // Boolean
    StringLiteralExpression, // String
    RuneLiteralExpression, // Rune
    IntegerLiteralExpression, // Integer
    ParenthesizedExpression, // OpenParen Expression CloseParen
    IdentifierNameExpression, // Identifier
    OptionNameExpression, // Question NameExpression
    GenericNameExpression, // Identifier LessThan? SeparatedList[TypeName, Comma]? GreaterThan?
}
