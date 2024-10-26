package syspro.tm.lexer;

public enum BuiltInType {
    INT32,
    INT64,
    UINT32,
    UINT64,
    BOOLEAN,
    RUNE,
    STRING,
    ;

    public boolean isIntegral() {
        return isSignedIntegral() || isUnsignedIntegral();
    }

    public boolean isSignedIntegral() {
        return switch (this) {
            case INT32, INT64 -> true;
            case UINT32, UINT64, BOOLEAN, RUNE, STRING -> false;
        };
    }

    public boolean isUnsignedIntegral() {
        return switch (this) {
            case UINT32, UINT64 -> true;
            case INT32, INT64, BOOLEAN, RUNE, STRING -> false;
        };
    }
}
