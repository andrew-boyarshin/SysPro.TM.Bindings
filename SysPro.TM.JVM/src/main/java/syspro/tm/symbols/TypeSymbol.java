package syspro.tm.symbols;

import java.util.List;

public non-sealed interface TypeSymbol extends TypeLikeSymbol {
    boolean isAbstract();
    List<? extends TypeSymbol> baseTypes();
    List<? extends TypeLikeSymbol> typeArguments();
    TypeSymbol originalDefinition();
    TypeSymbol construct(List<? extends TypeLikeSymbol> typeArguments);
    List<? extends MemberSymbol> members();
}
