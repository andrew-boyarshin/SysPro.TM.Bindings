package syspro.tm.symbols;

public interface LanguageServer {
    SemanticModel buildModel(String code);
}
