package syspro.tm.parser;

public interface AnySyntaxKind {
    boolean isTerminal();

    default boolean isNonTerminal() {
        return !isTerminal();
    }

    default boolean isDefinition() {
        return false;
    }

    default boolean isMemberDefinition() {
        return false;
    }

    default boolean isStatement() {
        return false;
    }

    default boolean isExpression() {
        return false;
    }

    default boolean isPrimaryExpression() {
        return false;
    }

    default boolean isNameExpression() {
        return false;
    }
}
