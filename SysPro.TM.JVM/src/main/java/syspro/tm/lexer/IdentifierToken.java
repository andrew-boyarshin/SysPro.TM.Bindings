package syspro.tm.lexer;

import java.util.Objects;

public final class IdentifierToken extends Token {

    public final String value;
    public final Keyword contextualKeyword;

    public IdentifierToken(int start, int end, int leadingTriviaLength, int trailingTriviaLength, String value, Keyword contextualKeyword) {
        super(start, end, leadingTriviaLength, trailingTriviaLength);
        assert value != null;
        assert contextualKeyword == null || contextualKeyword.isContextual : contextualKeyword;
        this.value = value;
        this.contextualKeyword = contextualKeyword;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public IdentifierToken withStart(int start) {
        return this.start == start ? this : new IdentifierToken(start, end, leadingTriviaLength, trailingTriviaLength, value, contextualKeyword);
    }

    @Override
    public IdentifierToken withEnd(int end) {
        return this.end == end ? this : new IdentifierToken(start, end, leadingTriviaLength, trailingTriviaLength, value, contextualKeyword);
    }

    @Override
    public IdentifierToken withLeadingTriviaLength(int leadingTriviaLength) {
        return this.leadingTriviaLength == leadingTriviaLength ? this : new IdentifierToken(start, end, leadingTriviaLength, trailingTriviaLength, value, contextualKeyword);
    }

    @Override
    public IdentifierToken withTrailingTriviaLength(int trailingTriviaLength) {
        return this.trailingTriviaLength == trailingTriviaLength ? this : new IdentifierToken(start, end, leadingTriviaLength, trailingTriviaLength, value, contextualKeyword);
    }

    public IdentifierToken withValue(String value) {
        return Objects.equals(this.value, value) ? this : new IdentifierToken(start, end, leadingTriviaLength, trailingTriviaLength, value, contextualKeyword);
    }

    public IdentifierToken withContextualKeyword(Keyword contextualKeyword) {
        return this.contextualKeyword == contextualKeyword ? this : new IdentifierToken(start, end, leadingTriviaLength, trailingTriviaLength, value, contextualKeyword);
    }
}
