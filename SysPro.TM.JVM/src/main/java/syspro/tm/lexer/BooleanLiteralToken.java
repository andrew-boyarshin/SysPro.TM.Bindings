package syspro.tm.lexer;

import syspro.tm.parser.SyntaxKind;

public final class BooleanLiteralToken extends LiteralToken {

    public final boolean value;

    public BooleanLiteralToken(int start, int end, int leadingTriviaLength, int trailingTriviaLength, boolean value) {
        super(start, end, leadingTriviaLength, trailingTriviaLength, BuiltInType.BOOLEAN);
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public BooleanLiteralToken withStart(int start) {
        return this.start == start ? this : new BooleanLiteralToken(start, end, leadingTriviaLength, trailingTriviaLength, value);
    }

    @Override
    public BooleanLiteralToken withEnd(int end) {
        return this.end == end ? this : new BooleanLiteralToken(start, end, leadingTriviaLength, trailingTriviaLength, value);
    }

    @Override
    public BooleanLiteralToken withLeadingTriviaLength(int leadingTriviaLength) {
        return this.leadingTriviaLength == leadingTriviaLength ? this : new BooleanLiteralToken(start, end, leadingTriviaLength, trailingTriviaLength, value);
    }

    @Override
    public BooleanLiteralToken withTrailingTriviaLength(int trailingTriviaLength) {
        return this.trailingTriviaLength == trailingTriviaLength ? this : new BooleanLiteralToken(start, end, leadingTriviaLength, trailingTriviaLength, value);
    }

    public BooleanLiteralToken withValue(boolean value) {
        return this.value == value ? this : new BooleanLiteralToken(start, end, leadingTriviaLength, trailingTriviaLength, value);
    }

    @Override
    public SyntaxKind toSyntaxKind() {
        return SyntaxKind.Boolean;
    }
}
