package syspro.tm.parser;

import java.util.List;

public class Diagnostic {
    private final DiagnosticInfo info;
    private final TextSpan location;
    private final List<Diagnostic> hints;

    public Diagnostic(DiagnosticInfo info, TextSpan location, List<Diagnostic> hints) {
        assert info != null;
        assert location != null;
        this.info = info;
        this.location = location;
        this.hints = hints;
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

    /**
     * Source text range that contains the problem.
     */
    public TextSpan location() {
        return location;
    }

    /**
     * Related additional diagnostics. Useful for user-oriented hints.
     * A good example for the case of duplicate variable definition would be "first declared here" to show the conflict.
     */
    public List<Diagnostic> hints() {
        return hints == null ? List.of() : hints;
    }
}
