package syspro.tm.parser;

import java.util.Comparator;

public final class SyntaxUtils {
    private SyntaxUtils() {
    }

    /**
     * Find the most specific syntax subtree node at the specified position.
     */
    public static SyntaxNode nodeAtPosition(SyntaxNode root, int position) {
        if (root == null) {
            return null;
        }

        return root.descendants(true).stream().filter(x -> {
            final var span = x.fullSpan();
            return span != null && span.contains(position);
        }).min(Comparator.comparingInt(SyntaxUtils::nodeSpanLength)).orElse(null);
    }

    private static int nodeSpanLength(SyntaxNode x) {
        final var span = x.fullSpan();
        return span == null ? Integer.MAX_VALUE : span.length;
    }
}
