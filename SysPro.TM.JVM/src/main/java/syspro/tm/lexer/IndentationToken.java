package syspro.tm.lexer;

/**
 * Zero-width synthetic token that signifies change in indentation level.
 * Represents traditional approach to parsing indentation-based languages.
 */
public final class IndentationToken extends Token {

    /**
     * Difference in the number of indentation levels.
     * As an example, -1 might mean that the following code uses 4 spaces fewer than the code before this token.
     */
    public final int difference;

    public IndentationToken(int start, int end, int leadingTriviaLength, int trailingTriviaLength, int difference) {
        super(start, end, leadingTriviaLength, trailingTriviaLength);
        assert difference == 1 || difference == -1 : difference;
        this.difference = difference;
    }

    public boolean isIndent() {
        return difference == 1;
    }

    public boolean isDedent() {
        return difference == -1;
    }

    @Override
    public String toString() {
        return isIndent() ? "<INDENT>" : "<DEDENT>";
    }

    @Override
    public IndentationToken withStart(int start) {
        return this.start == start ? this : new IndentationToken(start, end, leadingTriviaLength, trailingTriviaLength, difference);
    }

    @Override
    public IndentationToken withEnd(int end) {
        return this.end == end ? this : new IndentationToken(start, end, leadingTriviaLength, trailingTriviaLength, difference);
    }

    @Override
    public IndentationToken withLeadingTriviaLength(int leadingTriviaLength) {
        return this.leadingTriviaLength == leadingTriviaLength ? this : new IndentationToken(start, end, leadingTriviaLength, trailingTriviaLength, difference);
    }

    @Override
    public IndentationToken withTrailingTriviaLength(int trailingTriviaLength) {
        return this.trailingTriviaLength == trailingTriviaLength ? this : new IndentationToken(start, end, leadingTriviaLength, trailingTriviaLength, difference);
    }

    public IndentationToken withDifference(int difference) {
        return this.difference == difference ? this : new IndentationToken(start, end, leadingTriviaLength, trailingTriviaLength, difference);
    }
}
