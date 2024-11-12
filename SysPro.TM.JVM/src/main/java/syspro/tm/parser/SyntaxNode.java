package syspro.tm.parser;

import syspro.tm.lexer.Token;

import java.util.*;

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
     * @see SyntaxNode#fullSpan()
     */
    default int fullLength() {
        final var firstTerminal = firstTerminal().token();
        final var lastTerminal = lastTerminal().token();
        return lastTerminal.end - firstTerminal.start + 1;
    }

    /**
     * @see SyntaxNode#fullLength()
     * @see SyntaxNode#span()
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

    /**
     * Predicate to determine if the current syntax tree node represents elementary symbol in SysPro formal grammar.
     */
    default boolean isTerminal() {
        return token() != null;
    }

    /**
     * Valid only when {@link SyntaxNode#isTerminal()} is {@code true}, otherwise returns {@code null}.
     */
    Token token();

    /**
     * Find the first descendant node, for which {@link SyntaxNode#isTerminal()} is {@code true}.
     * <p>
     * The implementation doesn't sort all descendant terminals, only finds the first in tree traversal order.
     * For any correct syntax tree it is equivalent, but faster.
     */
    default SyntaxNode firstTerminal() {
        if (isTerminal()) {
            return this;
        }

        final var descendants = descendants(false);

        for (var descendant : descendants) {
            if (descendant.isTerminal()) {
                return descendant;
            }
        }

        return null;
    }

    /**
     * Find the last descendant node, for which {@link SyntaxNode#isTerminal()} is {@code true}.
     * <p>
     * The implementation doesn't sort all descendant terminals, only finds the last in tree traversal order.
     * For any correct syntax tree it is equivalent, but faster.
     */
    default SyntaxNode lastTerminal() {
        if (isTerminal()) {
            return this;
        }

        final var descendants = descendants(false);

        for (var descendant : descendants.reversed()) {
            if (descendant.isTerminal()) {
                return descendant;
            }
        }

        return null;
    }

    /**
     * Perform tree traversal, returns a list of all found descendants in pre-order: [node, child 0, child 1, ...].
     * There are no {@code null} entries in the output list.
     * <p>
     * If {@code includeSelf} is {@code false}, the output might contain {@code this} anyway,
     * if the tree is malformed (contains a loop with {@code this}).
     * <p>
     * The behavior in the presence of malformed trees (e.g. containing loops) should be considered implementation-defined.
     * This implementation tries to be resilient to malformed trees (e.g. containing arbitrary loops), therefore it skips all malformed subtrees.
     * The checker module doesn't check this method, except via default implementations of
     * {@link SyntaxNode#firstTerminal()} and {@link SyntaxNode#lastTerminal()}.
     * <p>
     * The algorithm in the current implementation is an iterative DFS with order correction.
     * <p>
     * @param includeSelf if {@code true}, the output list will include {@code this} as the first element
     */
    default List<SyntaxNode> descendants(boolean includeSelf) {
        final var result = new ArrayList<SyntaxNode>();
        final var visited = new IdentityHashMap<SyntaxNode, Void>(); // there is no IdentityHashSet
        final var stack = new ArrayDeque<SyntaxNode>();

        if (includeSelf) {
            stack.push(this);
        } else {
            for (int i = slotCount() - 1; i >= 0; i--) {
                var child = slot(i);
                if (child != null) {
                    stack.push(child);
                }
            }
        }

        while (!stack.isEmpty()) {
            final var node = stack.pop();
            if (!visited.containsKey(node)) {
                visited.put(node, null);
                result.add(node);
                for (int i = node.slotCount() - 1; i >= 0; i--) {
                    var child = node.slot(i);
                    if (child != null) {
                        stack.push(child);
                    }
                }
            }
        }

        return result;
    }
}
