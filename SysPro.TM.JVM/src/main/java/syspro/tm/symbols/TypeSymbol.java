package syspro.tm.symbols;

import java.util.List;

public non-sealed interface TypeSymbol extends TypeLikeSymbol {

    /**
     * Returns {@code true} if there are abstract functions in this type,
     * or if there are non-overridden abstract functions in its base type closure.
     */
    boolean isAbstract();

    /**
     * Immediate base types of the current type. Doesn't include implicit {@code Object} base type.
     */
    List<? extends TypeSymbol> baseTypes();

    /**
     * A list of generic type arguments. For the original definition it consists of {@link TypeParameterSymbol}.
     */
    List<? extends TypeLikeSymbol> typeArguments();

    /**
     * For generic types, a canonical definition (the one from the definition in the source code),
     * as opposed to any constructed one (used as a type elsewhere).
     */
    TypeSymbol originalDefinition();

    /**
     * Optional operation: construct a derived symbol with a given mapping of type parameters to argument types.
     */
    TypeSymbol construct(List<? extends TypeLikeSymbol> typeArguments);

    /**
     * A list of members, declared in this type. Doesn't include members of base types.
     */
    List<? extends MemberSymbol> members();
}
