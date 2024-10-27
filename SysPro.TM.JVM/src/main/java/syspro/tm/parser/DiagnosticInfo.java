package syspro.tm.parser;

public class DiagnosticInfo {
    private static final Object[] EMPTY_ARGUMENTS = new Object[0];

    private final ErrorCode errorCode;
    private final Object[] arguments;

    public DiagnosticInfo(ErrorCode errorCode, Object[] arguments) {
        assert errorCode != null;
        this.errorCode = errorCode;
        this.arguments = arguments;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public Object[] arguments() {
        return arguments == null ? EMPTY_ARGUMENTS : arguments;
    }
}
