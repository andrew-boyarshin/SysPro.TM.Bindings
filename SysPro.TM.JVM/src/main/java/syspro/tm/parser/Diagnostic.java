package syspro.tm.parser;

public class Diagnostic {
    private final DiagnosticInfo info;
    private final TextSpan location;

    public Diagnostic(DiagnosticInfo info, TextSpan location) {
        this.info = info;
        this.location = location;
    }

    public DiagnosticInfo info() {
        return info;
    }

    public ErrorCode errorCode() {
        return info().errorCode();
    }

    public Object[] arguments() {
        return info().arguments();
    }

    public TextSpan location() {
        return location;
    }
}
