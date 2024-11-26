package syspro.tm.lexer;

import syspro.tm.parser.AnySyntaxKind;

public enum Symbol implements AnySyntaxKind {
    DOT("."),
    COLON(":"),
    COMMA(","),
    PLUS("+"),
    MINUS("-"),
    ASTERISK("*"),
    SLASH("/"),
    PERCENT("%"),
    EXCLAMATION("!"),
    TILDE("~"),
    AMPERSAND("&"),
    BAR("|"),
    AMPERSAND_AMPERSAND("&&"),
    BAR_BAR("||"),
    CARET("^"),
    LESS_THAN("<"),
    LESS_THAN_EQUALS("<="),
    GREATER_THAN(">"),
    GREATER_THAN_EQUALS(">="),
    LESS_THAN_LESS_THAN("<<"),
    GREATER_THAN_GREATER_THAN(">>"),
    OPEN_BRACKET("["),
    CLOSE_BRACKET("]"),
    OPEN_PAREN("("),
    CLOSE_PAREN(")"),
    EQUALS("="),
    EQUALS_EQUALS("=="),
    EXCLAMATION_EQUALS("!="),
    QUESTION("?"),
    BOUND("<:"),
    ;

    public final String text;

    Symbol(String text) {
        this.text = text;
    }

    @Override
    public boolean isTerminal() {
        return true;
    }
}
