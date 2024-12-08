package syspro.tm.symbols;

public enum SymbolKind {
    CLASS,
    OBJECT,
    INTERFACE,
    FIELD,
    LOCAL,
    PARAMETER,
    FUNCTION,
    TYPE_PARAMETER,
    ;

    public boolean isType() {
        return switch (this) {
            case CLASS, OBJECT, INTERFACE -> true;
            case FIELD, LOCAL, PARAMETER, FUNCTION, TYPE_PARAMETER -> false;
        };
    }

    public boolean isTypeLike() {
        return switch (this) {
            case CLASS, OBJECT, INTERFACE, TYPE_PARAMETER -> true;
            case FIELD, LOCAL, PARAMETER, FUNCTION -> false;
        };
    }

    public boolean isVariable() {
        return switch (this) {
            case FIELD, LOCAL, PARAMETER -> true;
            case CLASS, OBJECT, INTERFACE, FUNCTION, TYPE_PARAMETER -> false;
        };
    }

    public boolean isMember() {
        return switch (this) {
            case FIELD, FUNCTION -> true;
            case CLASS, OBJECT, INTERFACE, LOCAL, PARAMETER, TYPE_PARAMETER -> false;
        };
    }
}
