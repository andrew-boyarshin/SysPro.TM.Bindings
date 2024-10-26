package syspro.tm.lexer;

import syspro.tm.parser.AnySyntaxKind;

public final class KeywordToken extends Token {

    public final Keyword keyword;

    public KeywordToken(int start, int end, int leadingTriviaLength, int trailingTriviaLength, Keyword keyword) {
        super(start, end, leadingTriviaLength, trailingTriviaLength);
        assert keyword != null;
        this.keyword = keyword;
    }

    @Override
    public String toString() {
        return keyword.text;
    }

    @Override
    public KeywordToken withStart(int start) {
        return this.start == start ? this : new KeywordToken(start, end, leadingTriviaLength, trailingTriviaLength, keyword);
    }

    @Override
    public KeywordToken withEnd(int end) {
        return this.end == end ? this : new KeywordToken(start, end, leadingTriviaLength, trailingTriviaLength, keyword);
    }

    @Override
    public KeywordToken withLeadingTriviaLength(int leadingTriviaLength) {
        return this.leadingTriviaLength == leadingTriviaLength ? this : new KeywordToken(start, end, leadingTriviaLength, trailingTriviaLength, keyword);
    }

    @Override
    public KeywordToken withTrailingTriviaLength(int trailingTriviaLength) {
        return this.trailingTriviaLength == trailingTriviaLength ? this : new KeywordToken(start, end, leadingTriviaLength, trailingTriviaLength, keyword);
    }

    public KeywordToken withKeyword(Keyword keyword) {
        return this.keyword == keyword ? this : new KeywordToken(start, end, leadingTriviaLength, trailingTriviaLength, keyword);
    }

    @Override
    public AnySyntaxKind toSyntaxKind() {
        return keyword;
    }
}
