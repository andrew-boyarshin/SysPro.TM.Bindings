package syspro.tm.symbols;

import java.util.List;

public non-sealed interface TypeParameterSymbol extends TypeLikeSymbol, SemanticSymbolWithOwner {

    /**
     * A list of upper bounds for this type parameter. All valid instantiations of
     * {@link SemanticSymbolWithOwner#owner()} type must be subtypes of all types in this list.
     */
    List<? extends TypeLikeSymbol> bounds();
}
