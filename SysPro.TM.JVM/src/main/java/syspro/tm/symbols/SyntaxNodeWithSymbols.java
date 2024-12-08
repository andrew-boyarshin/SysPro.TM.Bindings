package syspro.tm.symbols;

import syspro.tm.parser.SyntaxNode;

public interface SyntaxNodeWithSymbols extends SyntaxNode {

    /**
     * Defined semantic entity, if any. {@code null} otherwise.
     */
    default SemanticSymbol symbol() {
        return null;
    }
}
