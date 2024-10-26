package syspro.tm.lexer;

import syspro.tm.parser.AnySyntaxKind;

public final class SymbolToken extends Token {

    public final Symbol symbol;

    public SymbolToken(int start, int end, int leadingTriviaLength, int trailingTriviaLength, Symbol symbol) {
        super(start, end, leadingTriviaLength, trailingTriviaLength);
        assert symbol != null;
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol.text;
    }

    @Override
    public SymbolToken withStart(int start) {
        return this.start == start ? this : new SymbolToken(start, end, leadingTriviaLength, trailingTriviaLength, symbol);
    }

    @Override
    public SymbolToken withEnd(int end) {
        return this.end == end ? this : new SymbolToken(start, end, leadingTriviaLength, trailingTriviaLength, symbol);
    }

    @Override
    public SymbolToken withLeadingTriviaLength(int leadingTriviaLength) {
        return this.leadingTriviaLength == leadingTriviaLength ? this : new SymbolToken(start, end, leadingTriviaLength, trailingTriviaLength, symbol);
    }

    @Override
    public SymbolToken withTrailingTriviaLength(int trailingTriviaLength) {
        return this.trailingTriviaLength == trailingTriviaLength ? this : new SymbolToken(start, end, leadingTriviaLength, trailingTriviaLength, symbol);
    }

    public SymbolToken withSymbol(Symbol symbol) {
        return this.symbol == symbol ? this : new SymbolToken(start, end, leadingTriviaLength, trailingTriviaLength, symbol);
    }

    @Override
    public AnySyntaxKind toSyntaxKind() {
        return symbol;
    }
}
