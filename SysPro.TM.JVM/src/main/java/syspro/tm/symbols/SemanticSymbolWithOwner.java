package syspro.tm.symbols;

public sealed interface SemanticSymbolWithOwner extends SemanticSymbol permits MemberSymbol, TypeParameterSymbol {

    /**
     * The immediately containing symbol: the symbol which owns this symbol.
     * It is related to definition location, rather than the use.
     * For example, it returns {@code A<T>} for owner of {@code T}, even if it is used as a return type of {@code B<V>}
     * function {@code foo(): V} by means of {@code B<T>().foo()} function call.
     */
    SemanticSymbol owner();
}
