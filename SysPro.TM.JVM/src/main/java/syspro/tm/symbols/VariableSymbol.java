package syspro.tm.symbols;

public non-sealed interface VariableSymbol extends MemberSymbol {

    /**
     * Variable value type. Can be {@code null} if it depends on the type of initializer.
     */
    TypeLikeSymbol type();
}
