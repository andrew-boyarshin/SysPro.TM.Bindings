package syspro.tm.symbols;

public enum SymbolKind {
    Class,
    Object,
    Interface,
    Field,
    Local,
    Parameter,
    Function,
    TypeParameter,
    ;

    public boolean isType() {
        return switch (this) {
            case Class, Object, Interface -> true;
            case Field, Local, Parameter, Function, TypeParameter -> false;
        };
    }

    public boolean isTypeLike() {
        return switch (this) {
            case Class, Object, Interface, TypeParameter -> true;
            case Field, Local, Parameter, Function -> false;
        };
    }

    public boolean isVariable() {
        return switch (this) {
            case Field, Local, Parameter -> true;
            case Class, Object, Interface, Function, TypeParameter -> false;
        };
    }

    public boolean isMember() {
        return switch (this) {
            case Field, Function -> true;
            case Class, Object, Interface, Local, Parameter, TypeParameter -> false;
        };
    }
}
