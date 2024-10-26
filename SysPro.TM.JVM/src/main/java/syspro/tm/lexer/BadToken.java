package syspro.tm.lexer;

public final class BadToken extends Token {

    public BadToken(int start, int end, int leadingTriviaLength, int trailingTriviaLength) {
        super(start, end, leadingTriviaLength, trailingTriviaLength);
    }

    @Override
    public String toString() {
        return "<BAD>";
    }

    @Override
    public BadToken withStart(int start) {
        return this.start == start ? this : new BadToken(start, end, leadingTriviaLength, trailingTriviaLength);
    }

    @Override
    public BadToken withEnd(int end) {
        return this.end == end ? this : new BadToken(start, end, leadingTriviaLength, trailingTriviaLength);
    }

    @Override
    public BadToken withLeadingTriviaLength(int leadingTriviaLength) {
        return this.leadingTriviaLength == leadingTriviaLength ? this : new BadToken(start, end, leadingTriviaLength, trailingTriviaLength);
    }

    @Override
    public BadToken withTrailingTriviaLength(int trailingTriviaLength) {
        return this.trailingTriviaLength == trailingTriviaLength ? this : new BadToken(start, end, leadingTriviaLength, trailingTriviaLength);
    }
}
