package syspro.tm.parser;

import syspro.tm.lexer.Token;

public interface SyntaxNode {

    /**
     * @return one of {@link syspro.tm.lexer.Keyword}, {@link syspro.tm.lexer.Symbol} or {@link SyntaxKind}.
     */
    AnySyntaxKind kind();

    /**
     * Start position of {@link SyntaxNode#fullSpan()}.
     */
    default int position() {
        return firstTerminal().token().start;
    }

    /**
     * Syntax node source text interval, excluding trivia.
     * @see SyntaxNode#fullSpan()
     */
    default TextSpan span() {
        var start = position();
        var length = fullLength();

        var leadingTriviaWidth = leadingTriviaLength();
        start += leadingTriviaWidth;
        length -= leadingTriviaWidth;

        length -= trailingTriviaLength();

        assert length >= 0;
        return new TextSpan(start, length);
    }

    /**
     * Syntax node source text interval, including trivia.
     * @see SyntaxNode#span()
     */
    default TextSpan fullSpan() {
        final var firstTerminal = firstTerminal().token();
        final var lastTerminal = lastTerminal().token();
        final var fullLength = lastTerminal.end - firstTerminal.start + 1;
        return new TextSpan(firstTerminal.start, fullLength);
    }

    /**
     * @see SyntaxNode#length()
     */
    default int fullLength() {
        final var firstTerminal = firstTerminal().token();
        final var lastTerminal = lastTerminal().token();
        return lastTerminal.end - firstTerminal.start + 1;
    }

    /**
     * @see SyntaxNode#fullLength()
     */
    default int length() {
        final var firstTerminal = firstTerminal().token();
        final var lastTerminal = lastTerminal().token();
        final var fullLength = lastTerminal.end - firstTerminal.start + 1;
        return fullLength - firstTerminal.leadingTriviaLength - lastTerminal.trailingTriviaLength;
    }

    default int leadingTriviaLength() {
        final var firstTerminal = firstTerminal().token();
        return firstTerminal.leadingTriviaLength;
    }

    default int trailingTriviaLength() {
        final var lastTerminal = lastTerminal().token();
        return lastTerminal.trailingTriviaLength;
    }

    /**
     * Number of slots defined for this {@link SyntaxNode#kind()}.
     */
    int slotCount();

    /**
     * Get slot for a direct syntax tree child, with slot index
     * in the range from {@code 0} (inclusive) to {@link SyntaxNode#slotCount()} (exclusive).
     */
    SyntaxNode slot(int index);

    default boolean isTerminal() {
        return token() != null;
    }

    /**
     * Valid only when {@link SyntaxNode#isTerminal()} is {@code true}.
     */
    Token token();

    default SyntaxNode firstTerminal() {
        if (isTerminal()) {
            return this;
        }

        for (int i = 0, n = slotCount(); i < n; i++) {
            var child = slot(i);
            if (child != null) {
                final var firstTerminal = child.firstTerminal();
                if (firstTerminal != null) {
                    return firstTerminal;
                }
            }
        }

        return null;
    }

    default SyntaxNode lastTerminal() {
        if (isTerminal()) {
            return this;
        }

        for (int i = slotCount() - 1; i >= 0; i--) {
            var child = slot(i);
            if (child != null) {
                final var lastTerminal = child.lastTerminal();
                if (lastTerminal != null) {
                    return lastTerminal;
                }
            }
        }

        return null;
    }
}
