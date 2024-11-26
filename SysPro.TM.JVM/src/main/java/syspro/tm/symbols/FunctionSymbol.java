package syspro.tm.symbols;

import java.util.List;

public non-sealed interface FunctionSymbol extends MemberSymbol {
    boolean isNative();
    boolean isVirtual(); // true if virtual, abstract or override
    boolean isAbstract();
    boolean isOverride();
    FunctionSymbol overriddenFunction();
    List<? extends VariableSymbol> parameters();
    TypeLikeSymbol returnType();
    List<? extends VariableSymbol> locals();
}
