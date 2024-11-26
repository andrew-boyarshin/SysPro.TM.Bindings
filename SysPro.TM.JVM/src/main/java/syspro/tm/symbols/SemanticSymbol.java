package syspro.tm.symbols;

import syspro.tm.parser.SyntaxNode;

import java.util.List;

public sealed interface SemanticSymbol permits SemanticSymbolWithOwner, TypeLikeSymbol {
    SymbolKind kind();
    String name();
    SyntaxNode definition();
    List<? extends SyntaxNode> references(); // aka “find usages”
}
