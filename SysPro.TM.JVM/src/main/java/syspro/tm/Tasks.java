package syspro.tm;

import syspro.tm.lexer.TestMode;

public final class Tasks {

    private Tasks() {
    }

    public static final class Lexer {

        private Lexer() {
        }

        public static void registerSolution(syspro.tm.lexer.Lexer impl, TestMode mode) {
            Library.registerTask1Solution(impl, mode);
        }

        public static void registerSolution(syspro.tm.lexer.Lexer impl) {
            registerSolution(impl, new TestMode());
        }
    }

    public static final class Parser {

        private Parser() {
        }

        public static void registerSolution(syspro.tm.parser.Parser impl) {
            Library.registerTask2Solution(impl);
        }
    }

    public static final class LanguageServer {

        private LanguageServer() {
        }

        public static void registerSolution(syspro.tm.symbols.LanguageServer impl) {
            Library.registerTask3Solution(impl);
        }
    }
}
