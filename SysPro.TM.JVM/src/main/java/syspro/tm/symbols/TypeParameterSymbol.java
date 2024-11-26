package syspro.tm.symbols;

import java.util.List;

public non-sealed interface TypeParameterSymbol extends TypeLikeSymbol, SemanticSymbolWithOwner {
    List<? extends TypeLikeSymbol> bounds();
}
