package syspro.tm.lexer;

import syspro.tm.parser.AnySyntaxKind;
import syspro.tm.parser.TextSpan;

public abstract sealed class Token permits BadToken, IdentifierToken, IndentationToken, KeywordToken, LiteralToken, SymbolToken {

    public final int start;
    public final int end;
    public final int leadingTriviaLength;
    public final int trailingTriviaLength;

    protected Token(int start, int end, int leadingTriviaLength, int trailingTriviaLength) {
        this.start = start;
        this.end = end;
        this.leadingTriviaLength = leadingTriviaLength;
        this.trailingTriviaLength = trailingTriviaLength;
    }

    /**
     * Informative representation of the token for debugging purposes.
     * Doesn't have to match source code, doesn't contain trivia, doesn't print lexical attributes,
     * doesn't have to be valid SysPro syntax.
     */
    @Override
    public abstract String toString();

    /**
     * Make a copy of the current token with a different {@link Token#start} value.
     */
    public abstract Token withStart(int start);

    /**
     * Make a copy of the current token with a different {@link Token#end} value.
     */
    public abstract Token withEnd(int end);

    /**
     * Make a copy of the current token with a different {@link Token#leadingTriviaLength} value.
     */
    public abstract Token withLeadingTriviaLength(int leadingTriviaLength);

    /**
     * Make a copy of the current token with a different {@link Token#trailingTriviaLength} value.
     */
    public abstract Token withTrailingTriviaLength(int trailingTriviaLength);

    public abstract AnySyntaxKind toSyntaxKind();

    /**
     * Token source text interval, excluding trivia.
     *
     * @see Token#fullSpan()
     */
    public final TextSpan span() {
        var start = this.start;
        var length = end - start + 1;

        var leadingTriviaWidth = leadingTriviaLength;
        start += leadingTriviaWidth;
        length -= leadingTriviaWidth;

        length -= trailingTriviaLength;

        assert length >= 0;
        return new TextSpan(start, length);
    }

    /**
     * Token source text interval, including trivia.
     *
     * @see Token#span()
     */
    public final TextSpan fullSpan() {
        final var fullLength = end - start + 1;
        return new TextSpan(start, fullLength);
    }
}
