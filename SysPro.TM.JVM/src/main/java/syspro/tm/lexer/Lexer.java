package syspro.tm.lexer;

import java.util.List;

public interface Lexer {
    List<Token> lex(String code);
}
