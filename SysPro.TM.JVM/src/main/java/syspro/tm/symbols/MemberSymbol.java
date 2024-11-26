package syspro.tm.symbols;

public sealed interface MemberSymbol extends SemanticSymbolWithOwner permits FunctionSymbol, VariableSymbol {
}
