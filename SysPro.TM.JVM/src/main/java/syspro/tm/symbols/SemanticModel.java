package syspro.tm.symbols;

import syspro.tm.parser.Diagnostic;
import syspro.tm.parser.SyntaxNode;
import syspro.tm.parser.SyntaxUtils;
import syspro.tm.parser.TextSpan;

import java.util.Collection;
import java.util.List;

public interface SemanticModel {

    /**
     * The output syntax tree root node.
     */
    SyntaxNode root();

    /**
     * Nodes that might be covered by any of the ranges returned by this method are not required to be present or correct in {@link SemanticModel#root()} result tree.
     */
    Collection<TextSpan> invalidRanges();

    /**
     * All found problems with their locations in the source input.
     */
    Collection<Diagnostic> diagnostics();

    /**
     * A list of all type definitions in the source input. Doesn't include built-in types, such as {@code Int64}.
     */
    List<? extends TypeSymbol> typeDefinitions();

    /**
     * Returns type, found by its name, or {@code null} if type is not defined.
     * Must return non-{@code null} values for all built-in types.
     */
    TypeSymbol lookupType(String name);

    /**
     * Find the most specific syntax tree node at the specified position.
     */
    default SyntaxNode nodeAtPosition(int position) {
        return SyntaxUtils.nodeAtPosition(root(), position);
    }
}
