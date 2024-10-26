package syspro.tm.lexer;

public final class TestMode {
    private boolean repeated, shuffled, parallel;

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

    public int toMask() {
        int result = 0;
        if (repeated) result |= 1 << 0;
        if (shuffled) result |= 1 << 1;
        if (parallel) result |= 1 << 2;
        return result;
    }
}
