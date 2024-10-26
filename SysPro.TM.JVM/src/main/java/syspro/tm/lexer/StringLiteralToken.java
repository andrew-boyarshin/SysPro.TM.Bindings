package syspro.tm.lexer;

import syspro.tm.parser.SyntaxKind;

import java.util.Objects;

public final class StringLiteralToken extends LiteralToken {

    public final String value;

    public StringLiteralToken(int start, int end, int leadingTriviaLength, int trailingTriviaLength, String value) {
        super(start, end, leadingTriviaLength, trailingTriviaLength, BuiltInType.STRING);
        assert value != null;
        this.value = value;
    }

    @Override
    public String toString() {
        return '"' + value + '"';
    }

    @Override
    public StringLiteralToken withStart(int start) {
        return this.start == start ? this : new StringLiteralToken(start, end, leadingTriviaLength, trailingTriviaLength, value);
    }

    @Override
    public StringLiteralToken withEnd(int end) {
        return this.end == end ? this : new StringLiteralToken(start, end, leadingTriviaLength, trailingTriviaLength, value);
    }

    @Override
    public StringLiteralToken withLeadingTriviaLength(int leadingTriviaLength) {
        return this.leadingTriviaLength == leadingTriviaLength ? this : new StringLiteralToken(start, end, leadingTriviaLength, trailingTriviaLength, value);
    }

    @Override
    public StringLiteralToken withTrailingTriviaLength(int trailingTriviaLength) {
        return this.trailingTriviaLength == trailingTriviaLength ? this : new StringLiteralToken(start, end, leadingTriviaLength, trailingTriviaLength, value);
    }

    public StringLiteralToken withValue(String value) {
        return Objects.equals(this.value, value) ? this : new StringLiteralToken(start, end, leadingTriviaLength, trailingTriviaLength, value);
    }

    @Override
    public SyntaxKind toSyntaxKind() {
        return SyntaxKind.String;
    }
}
