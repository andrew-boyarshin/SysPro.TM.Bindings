package syspro.tm.symbols;

import syspro.tm.parser.SyntaxNode;

public sealed interface SemanticSymbol permits SemanticSymbolWithOwner, TypeLikeSymbol {
    SymbolKind kind();

    /**
     * Name of semantic entity, as defined in the source code.
     */
    String name();

    /**
     * Syntax tree node of the definition, {@code null} for the built-in types.
     */
    SyntaxNode definition();
}
