package syspro.tm.lexer;

public sealed abstract class LiteralToken extends Token permits BooleanLiteralToken, IntegerLiteralToken, RuneLiteralToken, StringLiteralToken {

    public final BuiltInType type;

    protected LiteralToken(int start, int end, int leadingTriviaLength, int trailingTriviaLength, BuiltInType type) {
        super(start, end, leadingTriviaLength, trailingTriviaLength);
        assert type != null;
        this.type = type;
    }

    @Override
    public abstract LiteralToken withStart(int start);

    @Override
    public abstract LiteralToken withEnd(int end);

    @Override
    public abstract LiteralToken withLeadingTriviaLength(int leadingTriviaLength);

    @Override
    public abstract LiteralToken withTrailingTriviaLength(int trailingTriviaLength);
}
