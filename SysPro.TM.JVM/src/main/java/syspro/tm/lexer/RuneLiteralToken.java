package syspro.tm.lexer;

import syspro.tm.parser.SyntaxKind;

import java.util.Locale;

public final class RuneLiteralToken extends LiteralToken {

    /**
     * Unicode 32-bit scalar value
     */
    public final int value;

    public RuneLiteralToken(int start, int end, int leadingTriviaLength, int trailingTriviaLength, int value) {
        super(start, end, leadingTriviaLength, trailingTriviaLength, BuiltInType.RUNE);
        assert Character.isValidCodePoint(value) : "U+" + Integer.toHexString(value).toUpperCase(Locale.ROOT) + " is invalid";
        this.value = value;
    }

    private boolean isLikelyReadable() {
        return Character.isUnicodeIdentifierStart(value) || Character.isUnicodeIdentifierPart(value) || value == ' ';
    }

    @Override
    public String toString() {
        return isLikelyReadable() ? '\'' + Character.toString(value) + '\'' : "'\\U+" + String.format("%04d", value) + '\'';
    }

    @Override
    public RuneLiteralToken withStart(int start) {
        return this.start == start ? this : new RuneLiteralToken(start, end, leadingTriviaLength, trailingTriviaLength, value);
    }

    @Override
    public RuneLiteralToken withEnd(int end) {
        return this.end == end ? this : new RuneLiteralToken(start, end, leadingTriviaLength, trailingTriviaLength, value);
    }

    @Override
    public RuneLiteralToken withLeadingTriviaLength(int leadingTriviaLength) {
        return this.leadingTriviaLength == leadingTriviaLength ? this : new RuneLiteralToken(start, end, leadingTriviaLength, trailingTriviaLength, value);
    }

    @Override
    public RuneLiteralToken withTrailingTriviaLength(int trailingTriviaLength) {
        return this.trailingTriviaLength == trailingTriviaLength ? this : new RuneLiteralToken(start, end, leadingTriviaLength, trailingTriviaLength, value);
    }

    public RuneLiteralToken withValue(int value) {
        return this.value == value ? this : new RuneLiteralToken(start, end, leadingTriviaLength, trailingTriviaLength, value);
    }

    @Override
    public SyntaxKind toSyntaxKind() {
        return SyntaxKind.RUNE;
    }
}
