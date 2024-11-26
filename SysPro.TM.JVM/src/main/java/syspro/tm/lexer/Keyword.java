package syspro.tm.lexer;

import syspro.tm.parser.AnySyntaxKind;

import java.util.Locale;

public enum Keyword implements AnySyntaxKind {
    THIS,
    SUPER,
    IS,
    IF,
    ELSE,
    FOR,
    IN,
    WHILE,
    DEF,
    VAR,
    VAL,
    RETURN,
    BREAK,
    CONTINUE,
    ABSTRACT,
    VIRTUAL,
    OVERRIDE,
    NATIVE,
    CLASS(true),
    OBJECT(true),
    INTERFACE(true),
    NULL(true),
    ;

    public final String text;
    public final boolean isContextual;

    Keyword() {
        this(false);
    }

    Keyword(boolean isContextual) {
        this.text = name().toLowerCase(Locale.ROOT);
        this.isContextual = isContextual;
    }

    @Override
    public boolean isTerminal() {
        return true;
    }
}
