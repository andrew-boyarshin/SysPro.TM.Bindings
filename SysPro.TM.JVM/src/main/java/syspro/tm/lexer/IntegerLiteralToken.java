package syspro.tm.lexer;

public final class IntegerLiteralToken extends LiteralToken {

    public final boolean hasTypeSuffix;

    /**
     * Always positive ({@code -42} is 2 tokens: unary minus and integer literal 42),
     * except for values greater than {@link Long#MAX_VALUE}, which use wrap-around semantics:
     * {@code Long.MAX_VALUE + 1} is {@link Long#MIN_VALUE}, and max value of {@code UInt64} (= 2^64 - 1) is {@code -1}.
     */
    public final long value;

    public IntegerLiteralToken(int start, int end, int leadingTriviaLength, int trailingTriviaLength, BuiltInType type, boolean hasTypeSuffix, long value) {
        super(start, end, leadingTriviaLength, trailingTriviaLength, type);
        assert type.isIntegral() : type;
        assert type == BuiltInType.UINT64 || value >= 0 : value + " is invalid for " + value;
        this.hasTypeSuffix = hasTypeSuffix;
        this.value = value;
    }

    @Override
    public String toString() {
        final var base = Long.toUnsignedString(value);
        if (hasTypeSuffix) {
            return base + switch (type) {
                case INT32 -> "i32";
                case INT64 -> "i64";
                case UINT32 -> "u32";
                case UINT64 -> "u64";
                case BOOLEAN, RUNE, STRING -> throw new IllegalStateException(type + " is invalid");
            };
        }
        return base;
    }

    @Override
    public IntegerLiteralToken withStart(int start) {
        return this.start == start ? this : new IntegerLiteralToken(start, end, leadingTriviaLength, trailingTriviaLength, type, hasTypeSuffix, value);
    }

    @Override
    public IntegerLiteralToken withEnd(int end) {
        return this.end == end ? this : new IntegerLiteralToken(start, end, leadingTriviaLength, trailingTriviaLength, type, hasTypeSuffix, value);
    }

    @Override
    public IntegerLiteralToken withLeadingTriviaLength(int leadingTriviaLength) {
        return this.leadingTriviaLength == leadingTriviaLength ? this : new IntegerLiteralToken(start, end, leadingTriviaLength, trailingTriviaLength, type, hasTypeSuffix, value);
    }

    @Override
    public IntegerLiteralToken withTrailingTriviaLength(int trailingTriviaLength) {
        return this.trailingTriviaLength == trailingTriviaLength ? this : new IntegerLiteralToken(start, end, leadingTriviaLength, trailingTriviaLength, type, hasTypeSuffix, value);
    }

    public IntegerLiteralToken withType(BuiltInType type) {
        return this.type == type ? this : new IntegerLiteralToken(start, end, leadingTriviaLength, trailingTriviaLength, type, hasTypeSuffix, value);
    }

    public IntegerLiteralToken withHasTypeSuffix(boolean hasTypeSuffix) {
        return this.hasTypeSuffix == hasTypeSuffix ? this : new IntegerLiteralToken(start, end, leadingTriviaLength, trailingTriviaLength, type, hasTypeSuffix, value);
    }

    public IntegerLiteralToken withValue(long value) {
        return this.value == value ? this : new IntegerLiteralToken(start, end, leadingTriviaLength, trailingTriviaLength, type, hasTypeSuffix, value);
    }
}
