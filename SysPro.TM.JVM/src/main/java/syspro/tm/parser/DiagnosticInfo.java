package syspro.tm.parser;

public class DiagnosticInfo {
    private final ErrorCode errorCode;
    private final Object[] arguments;

    public DiagnosticInfo(ErrorCode errorCode, Object[] arguments) {
        this.errorCode = errorCode;
        this.arguments = arguments;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public Object[] arguments() {
        return arguments;
    }
}
