package syspro.tm;

import syspro.tm.lexer.TestMode;

public final class Tasks {

    private Tasks() {
    }

    /**
     * Only run tests with names that match the specified regular expression.
     * <p>
     * Note: multiple filters are applied as INTERSECTION operation, not union.
     *
     * @param regex Regular expression for test name
     * @see <a href="https://learn.microsoft.com/en-us/dotnet/standard/base-types/regular-expression-language-quick-reference">.NET regular expression language syntax reference</a>
     */
    public static void addTestIncludeFilter(String regex) {
        if (regex == null) {
            throw new IllegalArgumentException("Filter regular expression cannot be null");
        }

        Library.addTestFilter(regex, 1);
    }

    /**
     * Only run tests with names that DON'T match the specified regular expression.
     * <p>
     * Note: multiple filters are applied as INTERSECTION operation, not union.
     *
     * @param regex Regular expression for test name
     * @see <a href="https://learn.microsoft.com/en-us/dotnet/standard/base-types/regular-expression-language-quick-reference">.NET regular expression language syntax reference</a>
     */
    public static void addTestExcludeFilter(String regex) {
        if (regex == null) {
            throw new IllegalArgumentException("Filter regular expression cannot be null");
        }

        Library.addTestFilter(regex, 2);
    }

    /**
     * Remove all previously added test filters.
     */
    public static void clearTestFilters() {
        Library.clearTestFilters();
    }

    /**
     * Task #1: Lexical analysis
     */
    public static final class Lexer {

        private Lexer() {
        }

        public static void registerSolution(syspro.tm.lexer.Lexer impl, TestMode mode) {
            if (impl == null) {
                throw new IllegalArgumentException("Lexer implementation cannot be null.");
            }

            if (mode == null) {
                mode = new TestMode();
            }

            Library.registerTask1Solution(impl, mode);
        }

        public static void registerSolution(syspro.tm.lexer.Lexer impl) {
            registerSolution(impl, new TestMode());
        }
    }

    /**
     * Task #2: Syntax analysis
     */
    public static final class Parser {

        private Parser() {
        }

        public static void registerSolution(syspro.tm.parser.Parser impl) {
            if (impl == null) {
                throw new IllegalArgumentException("Parser implementation cannot be null.");
            }

            Library.registerTask2Solution(impl);
        }
    }

    /**
     * Task #3: Semantic analysis
     */
    public static final class LanguageServer {

        private LanguageServer() {
        }

        public static void registerSolution(syspro.tm.symbols.LanguageServer impl) {
            if (impl == null) {
                throw new IllegalArgumentException("LanguageServer implementation cannot be null.");
            }

            Library.registerTask3Solution(impl);
        }
    }
}
