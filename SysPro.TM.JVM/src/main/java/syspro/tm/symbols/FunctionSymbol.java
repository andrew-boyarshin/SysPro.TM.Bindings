package syspro.tm.symbols;

import java.util.List;

public non-sealed interface FunctionSymbol extends MemberSymbol {

    /**
     * Returns {@code true} if the function has {@link syspro.tm.lexer.Keyword#NATIVE} modifier.
     */
    boolean isNative();

    /**
     * Returns {@code true} if the function:
     * <ul>
     *     <li>has {@link syspro.tm.lexer.Keyword#VIRTUAL} modifier, or</li>
     *     <li>{@link FunctionSymbol#isAbstract()} is {@code true}, or</li>
     *     <li>{@link FunctionSymbol#isOverride()} is {@code true}.</li>
     * </ul>
     */
    boolean isVirtual();

    /**
     * Returns {@code true} if the function has {@link syspro.tm.lexer.Keyword#ABSTRACT} modifier,
     * or is defined in the {@link SymbolKind#INTERFACE}.
     */
    boolean isAbstract();

    /**
     * Returns {@code true} if the function has {@link syspro.tm.lexer.Keyword#OVERRIDE} modifier.
     */
    boolean isOverride();

    /**
     * An ordered list of defined parameters.
     */
    List<? extends VariableSymbol> parameters();

    /**
     * A return value type.
     */
    TypeLikeSymbol returnType();

    /**
     * A list of all defined local variables, in arbitrary order.
     */
    List<? extends VariableSymbol> locals();
}
