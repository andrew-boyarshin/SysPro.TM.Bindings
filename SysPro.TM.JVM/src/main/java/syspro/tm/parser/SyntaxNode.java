package syspro.tm.parser;

public interface SyntaxNode {

    /**
     * @return one of {@link syspro.tm.lexer.Keyword}, {@link syspro.tm.lexer.Symbol} or {@link SyntaxKind}.
     */
    AnySyntaxKind kind();

    int position();

    default TextSpan span() {
        var start = position();
        var width = fullWidth();

        var leadingTriviaWidth = leadingTriviaWidth();
        start += leadingTriviaWidth;
        width -= leadingTriviaWidth;

        width -= trailingTriviaWidth();

        assert width >= 0;
        return new TextSpan(start, width);
    }

    default TextSpan fullSpan() {
        return new TextSpan(position(), fullWidth());
    }

    int fullWidth();

    default int width() {
        return fullWidth() - this.leadingTriviaWidth() - this.trailingTriviaWidth();
    }

    default int leadingTriviaWidth() {
        return this.fullWidth() != 0 ? this.firstTerminal().leadingTriviaWidth() : 0;
    }

    default int trailingTriviaWidth() {
        return this.fullWidth() != 0 ? this.lastTerminal().trailingTriviaWidth() : 0;
    }

    int slotCount();

    SyntaxNode slot(int index);

    default SyntaxNode firstTerminal() {
        SyntaxNode node = this;

        do {
            SyntaxNode firstChild = null;
            for (int i = 0, n = node.slotCount(); i < n; i++) {
                var child = node.slot(i);
                if (child != null) {
                    firstChild = child;
                    break;
                }
            }
            node = firstChild;
        }
        while (node != null && node.slotCount() > 0);

        return node;
    }

    default SyntaxNode lastTerminal() {
        SyntaxNode node = this;

        do {
            SyntaxNode lastChild = null;
            for (int i = node.slotCount() - 1; i >= 0; i--) {
                var child = node.slot(i);
                if (child != null) {
                    lastChild = child;
                    break;
                }
            }
            node = lastChild;
        }
        while (node != null && node.slotCount() > 0);

        return node;
    }
}
