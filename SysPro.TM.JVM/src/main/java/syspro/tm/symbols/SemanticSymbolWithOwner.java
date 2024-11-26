package syspro.tm.symbols;

public sealed interface SemanticSymbolWithOwner extends SemanticSymbol permits MemberSymbol, TypeParameterSymbol {
    SemanticSymbol owner();
}
