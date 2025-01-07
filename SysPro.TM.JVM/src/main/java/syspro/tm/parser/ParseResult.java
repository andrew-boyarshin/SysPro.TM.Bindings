package syspro.tm.parser;

import java.util.Collection;

public interface ParseResult {
    /**
     * The output syntax tree root node.
     */
    SyntaxNode root();

    /**
     * Nodes that might be covered by any of the ranges returned by this method are not required to be present or correct in {@link ParseResult#root()} result tree.
     */
    Collection<TextSpan> invalidRanges();

    /**
     * All found problems with their locations in the source input.
     */
    Collection<Diagnostic> diagnostics();

    /**
     * Find the most specific syntax tree node at the specified position.
     */
    default SyntaxNode nodeAtPosition(int position) {
        return SyntaxUtils.nodeAtPosition(root(), position);
    }
}
