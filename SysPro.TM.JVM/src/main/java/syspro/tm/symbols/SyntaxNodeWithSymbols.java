package syspro.tm.symbols;

import syspro.tm.parser.SyntaxNode;

public interface SyntaxNodeWithSymbols extends SyntaxNode {

    /**
     * Expression type, if any. {@code null} otherwise.
     */
    default TypeLikeSymbol type() {
        return null;
    }

    /**
     * Referenced semantic entity, if any. {@code null} otherwise.
     */
    default SemanticSymbol symbol() {
        return null;
    }
}
