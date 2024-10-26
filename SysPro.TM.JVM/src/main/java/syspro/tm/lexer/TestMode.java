package syspro.tm.lexer;

public final class TestMode {
    private boolean repeated, shuffled, parallel;
    private TestLineTerminators lineTerminators = TestLineTerminators.Native;

    public TestMode repeated(boolean value) {
        this.repeated = value;
        return this;
    }

    public TestMode shuffled(boolean value) {
        this.shuffled = value;
        return this;
    }

    public TestMode parallel(boolean value) {
        this.parallel = value;
        return this;
    }

    public TestMode forceLineTerminators(TestLineTerminators value) {
        assert value != null;
        this.lineTerminators = value;
        return this;
    }

    public int toMask() {
        int result = 0;
        if (repeated) result |= 1 << 0;
        if (shuffled) result |= 1 << 1;
        if (parallel) result |= 1 << 2;
        switch (lineTerminators) {
            case CarriageReturnLineFeed:
                result |= 1 << 3;
                break;
            case LineFeed:
                result |= 1 << 4;
                break;
            case Mixed:
                result |= 1 << 3;
                result |= 1 << 4;
                break;
        }
        return result;
    }
}
