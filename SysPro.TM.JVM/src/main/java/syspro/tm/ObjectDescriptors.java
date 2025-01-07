package syspro.tm;

import syspro.tm.lexer.*;
import syspro.tm.parser.*;
import syspro.tm.symbols.*;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.*;

import static syspro.tm.Library.*;

final class TokenObjectDescriptor extends ObjectDescriptor<Token> {
    public TokenObjectDescriptor() {
        super(
                MemoryLayout.structLayout(
                        ValueLayout.JAVA_INT.withName("kind"),
                        ValueLayout.JAVA_INT.withName("start"),
                        ValueLayout.JAVA_INT.withName("end"),
                        ValueLayout.JAVA_INT.withName("leadingTriviaLength"),
                        ValueLayout.JAVA_INT.withName("trailingTriviaLength"),
                        ValueLayout.JAVA_INT.withName("type"),
                        STRING_LAYOUT.withName("value1"),
                        ValueLayout.JAVA_LONG.withName("value2"),
                        ValueLayout.JAVA_LONG.withName("value3")
                ),
                Token.class
        );
    }

    private static int tokenKind(Token token) {
        return switch (token) {
            case BadToken _ -> 0;
            case IdentifierToken _ -> 3;
            case IndentationToken indentationToken -> indentationToken.isIndent() ? 1 : 2;
            case KeywordToken keywordToken -> Library.tokenKind(keywordToken.keyword);
            case BooleanLiteralToken _ -> 4;
            case IntegerLiteralToken _ -> 5;
            case RuneLiteralToken _ -> 6;
            case StringLiteralToken _ -> 7;
            case SymbolToken symbolToken -> Library.tokenKind(symbolToken.symbol);
        };
    }

    @Override
    public void serialize(Token token) {
        set("kind", tokenKind(token));
        set("start", token.start);
        set("end", token.end);
        set("leadingTriviaLength", token.leadingTriviaLength);
        set("trailingTriviaLength", token.trailingTriviaLength);
        switch (token) {
            case IdentifierToken identifierToken:
                set("value1", identifierToken.value);
                set("value3", (long) (identifierToken.contextualKeyword != null ? Library.tokenKind(identifierToken.contextualKeyword) : 0));
                break;
            case BadToken _, IndentationToken _, KeywordToken _, SymbolToken _:
                break;
            case BooleanLiteralToken literalToken:
                set("type", literalToken.type);
                set("value2", literalToken.value ? 1L : 0);
                break;
            case IntegerLiteralToken literalToken:
                set("type", literalToken.type);
                set("value2", literalToken.value);
                set("value3", literalToken.hasTypeSuffix ? 1L : 0);
                break;
            case RuneLiteralToken literalToken:
                set("type", literalToken.type);
                set("value3", (long) literalToken.value);
                break;
            case StringLiteralToken literalToken:
                set("type", literalToken.type);
                set("value1", literalToken.value);
                break;
        }
    }
}

final class IterableObjectDescriptor extends ObjectDescriptor<Iterable<Object>> {
    public IterableObjectDescriptor() {
        //noinspection unchecked
        super(
                MemoryLayout.structLayout(
                        ValueLayout.JAVA_INT.withName("size"),
                        ValueLayout.JAVA_INT, // padding
                        ValueLayout.ADDRESS.withName("data")
                ),
                (Class<Iterable<Object>>) (Class) Iterable.class
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public void serialize(Iterable<Object> items) {
        List<Object> list;
        if (items instanceof List<?> itemsList) {
            list = (List<Object>) itemsList;
        } else if (items instanceof Collection<?> collection) {
            list = Arrays.asList(collection.toArray());
        } else {
            list = new ArrayList<>();
            for (final var item : items) {
                list.add(item);
            }
        }

        set("size", list.size());
        setArray("data", list);
    }
}

final class LexerObjectDescriptor extends ObjectDescriptor<Lexer> {
    private static volatile MemorySegment lexImpl;

    public LexerObjectDescriptor() {
        super(
                MemoryLayout.structLayout(
                        JVM_HANDLE_LAYOUT.withName("impl"),
                        VMT_STUB_LAYOUT.withName("lex")
                ),
                Lexer.class
        );
    }

    private static MemorySegment lexImpl() {
        final var stub = LexerObjectDescriptor.lexImpl;
        if (stub == null) {
            try {
                return LexerObjectDescriptor.lexImpl = linker.upcallStub(
                        MethodHandles.lookup().findStatic(
                                LexerObjectDescriptor.class, "lexImpl",
                                MethodType.methodType(JVM_HANDLE_CLASS, JVM_HANDLE_CLASS, STRING_CLASS)
                        ),
                        FunctionDescriptor.of(JVM_HANDLE_LAYOUT, JVM_HANDLE_LAYOUT, STRING_LAYOUT),
                        callStubArena
                );
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return stub;
    }

    private static long lexImpl(long impl, MemorySegment codeSegment) {
        try {
            final Lexer lexer = fromObjectHandle(impl);
            final var code = codeSegment.reinterpret(Integer.MAX_VALUE).getString(0);
            return toObjectHandle(List.copyOf(lexer.lex(code)));
        } catch (Throwable e) {
            fatalError(e);
            return toObjectHandle(null);
        }
    }

    @Override
    public void serialize(Lexer lexer) {
        set("impl", lexer);
        set("lex", lexImpl());
    }
}

final class ParserObjectDescriptor extends ObjectDescriptor<Parser> {
    private static volatile MemorySegment parseImpl;

    public ParserObjectDescriptor() {
        super(
                MemoryLayout.structLayout(
                        JVM_HANDLE_LAYOUT.withName("impl"),
                        VMT_STUB_LAYOUT.withName("parse")
                ),
                Parser.class
        );
    }

    private static long parseImpl(long impl, MemorySegment codeSegment) {
        try {
            final Parser parser = fromObjectHandle(impl);
            final var code = codeSegment.reinterpret(Integer.MAX_VALUE).getString(0);
            return toObjectHandle(parser.parse(code));
        } catch (Throwable e) {
            fatalError(e);
            return toObjectHandle(null);
        }
    }

    private static MemorySegment parseImpl() {
        final var stub = ParserObjectDescriptor.parseImpl;
        if (stub == null) {
            try {
                return ParserObjectDescriptor.parseImpl = linker.upcallStub(
                        MethodHandles.lookup().findStatic(
                                ParserObjectDescriptor.class, "parseImpl",
                                MethodType.methodType(JVM_HANDLE_CLASS, JVM_HANDLE_CLASS, STRING_CLASS)
                        ),
                        FunctionDescriptor.of(JVM_HANDLE_LAYOUT, JVM_HANDLE_LAYOUT, STRING_LAYOUT),
                        callStubArena
                );
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return stub;
    }

    @Override
    public void serialize(Parser parser) {
        set("impl", parser);
        set("parse", parseImpl());
    }
}

final class ParseResultObjectDescriptor extends ObjectDescriptor<ParseResult> {
    public ParseResultObjectDescriptor() {
        super(
                MemoryLayout.structLayout(
                        JVM_HANDLE_LAYOUT.withName("root"),
                        JVM_HANDLE_LAYOUT.withName("invalidRanges"),
                        JVM_HANDLE_LAYOUT.withName("diagnostics")
                ),
                ParseResult.class
        );
    }

    @Override
    public void serialize(ParseResult result) {
        set("root", result.root());
        set("invalidRanges", List.copyOf(result.invalidRanges()));
        set("diagnostics", List.copyOf(result.diagnostics()));
    }
}

final class SyntaxNodeObjectDescriptor extends ObjectDescriptor<SyntaxNode> {
    public SyntaxNodeObjectDescriptor() {
        super(
                MemoryLayout.structLayout(
                        ValueLayout.JAVA_INT.withName("kind"),
                        ValueLayout.JAVA_INT.withName("position"),
                        ValueLayout.JAVA_INT.withName("fullLength"),
                        ValueLayout.JAVA_INT.withName("leadingTriviaLength"),
                        ValueLayout.JAVA_INT.withName("trailingTriviaLength"),
                        ValueLayout.JAVA_INT.withName("slotCount"),
                        JVM_HANDLE_LAYOUT.withName("token"),
                        JVM_HANDLE_LAYOUT.withName("slots"),
                        JVM_HANDLE_LAYOUT.withName("symbol")
                ),
                SyntaxNode.class
        );
    }

    @Override
    public void serialize(SyntaxNode node) {
        final var nodeSlotCount = node.slotCount();
        final List<SyntaxNode> nodeSlots;
        if (nodeSlotCount == 0) {
            nodeSlots = List.of();
        } else {
            nodeSlots = Arrays.asList(new SyntaxNode[nodeSlotCount]);
            for (var i = 0; i < nodeSlotCount; i++) {
                nodeSlots.set(i, node.slot(i));
            }
        }

        set("kind", syntaxKind(node.kind()));
        int position;
        try {
            position = node.position();
        } catch (Throwable e) {
            e.printStackTrace();
            position = -1;
        }
        int fullLength;
        try {
            fullLength = node.fullLength();
        } catch (Throwable e) {
            e.printStackTrace();
            fullLength = -1;
        }
        int leadingTriviaLength;
        try {
            leadingTriviaLength = node.leadingTriviaLength();
        } catch (Throwable e) {
            e.printStackTrace();
            leadingTriviaLength = -1;
        }
        int trailingTriviaLength;
        try {
            trailingTriviaLength = node.trailingTriviaLength();
        } catch (Throwable e) {
            e.printStackTrace();
            trailingTriviaLength = -1;
        }
        set("position", position);
        set("fullLength", fullLength);
        set("leadingTriviaLength", leadingTriviaLength);
        set("trailingTriviaLength", trailingTriviaLength);
        set("slotCount", nodeSlotCount);
        set("token", node.token());
        set("slots", nodeSlots);

        if (node instanceof SyntaxNodeWithSymbols withSymbols) {
            set("symbol", withSymbols.symbol());
        } else {
            setNull("symbol");
        }
    }
}

final class TextSpanObjectDescriptor extends ObjectDescriptor<TextSpan> {
    public TextSpanObjectDescriptor() {
        super(
                MemoryLayout.structLayout(
                        ValueLayout.JAVA_INT.withName("start"),
                        ValueLayout.JAVA_INT.withName("length")
                ),
                TextSpan.class
        );
    }

    @Override
    public void serialize(TextSpan span) {
        set("start", span.start);
        set("length", span.length);
    }
}

final class DiagnosticObjectDescriptor extends ObjectDescriptor<Diagnostic> {
    public DiagnosticObjectDescriptor() {
        super(
                MemoryLayout.structLayout(
                        JVM_HANDLE_LAYOUT.withName("errorCode"),
                        JVM_HANDLE_LAYOUT.withName("arguments"),
                        JVM_HANDLE_LAYOUT.withName("location"),
                        JVM_HANDLE_LAYOUT.withName("hints")
                ),
                Diagnostic.class
        );
    }

    @Override
    public void serialize(Diagnostic diagnostic) {
        final var argumentsArray = diagnostic.arguments();
        final var diagnosticArguments = new DiagnosticArgument[argumentsArray.length];
        for (var i = 0; i < argumentsArray.length; i++) {
            diagnosticArguments[i] = new DiagnosticArgument(argumentsArray[i]);
        }
        set("errorCode", diagnostic.errorCode());
        set("arguments", Arrays.asList(diagnosticArguments));
        set("location", diagnostic.location());
        set("hints", diagnostic.hints());
    }
}

final class ErrorCodeObjectDescriptor extends ObjectDescriptor<ErrorCode> {
    public ErrorCodeObjectDescriptor() {
        super(
                MemoryLayout.structLayout(
                        ValueLayout.ADDRESS.withName("name")
                ),
                ErrorCode.class
        );
    }

    @Override
    public void serialize(ErrorCode errorCode) {
        set("name", errorCode.name());
    }
}

final class DiagnosticArgumentObjectDescriptor extends ObjectDescriptor<DiagnosticArgument> {
    public DiagnosticArgumentObjectDescriptor() {
        super(
                MemoryLayout.structLayout(
                        ValueLayout.ADDRESS.withName("stringValue"),
                        JVM_HANDLE_LAYOUT.withName("nodeValue")
                ),
                DiagnosticArgument.class
        );
    }

    @Override
    public void serialize(DiagnosticArgument argument) {
        final var object = argument.object;
        set("stringValue", Objects.toString(object));
        set("nodeValue", object instanceof SyntaxNode ? object : null);
    }
}

final class DiagnosticArgument {
    final Object object;

    DiagnosticArgument(Object object) {
        this.object = object;
    }
}

final class LanguageServerObjectDescriptor extends ObjectDescriptor<LanguageServer> {
    private static volatile MemorySegment buildModelImpl;

    public LanguageServerObjectDescriptor() {
        super(
                MemoryLayout.structLayout(
                        JVM_HANDLE_LAYOUT.withName("impl"),
                        VMT_STUB_LAYOUT.withName("buildModel")
                ),
                LanguageServer.class
        );
    }

    private static MemorySegment buildModelImpl() {
        final var stub = LanguageServerObjectDescriptor.buildModelImpl;
        if (stub == null) {
            try {
                return LanguageServerObjectDescriptor.buildModelImpl = linker.upcallStub(
                        MethodHandles.lookup().findStatic(
                                LanguageServerObjectDescriptor.class, "buildModelImpl",
                                MethodType.methodType(JVM_HANDLE_CLASS, JVM_HANDLE_CLASS, STRING_CLASS)
                        ),
                        FunctionDescriptor.of(JVM_HANDLE_LAYOUT, JVM_HANDLE_LAYOUT, STRING_LAYOUT),
                        callStubArena
                );
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return stub;
    }

    private static long buildModelImpl(long impl, MemorySegment codeSegment) {
        try {
            final LanguageServer server = fromObjectHandle(impl);
            final var code = codeSegment.reinterpret(Integer.MAX_VALUE).getString(0);
            return toObjectHandle(server.buildModel(code));
        } catch (Throwable e) {
            fatalError(e);
            return toObjectHandle(null);
        }
    }

    @Override
    public void serialize(LanguageServer server) {
        set("impl", server);
        set("buildModel", buildModelImpl());
    }
}

final class SemanticModelObjectDescriptor extends ObjectDescriptor<SemanticModel> {
    private static volatile MemorySegment lookupTypeImpl;

    public SemanticModelObjectDescriptor() {
        super(
                MemoryLayout.structLayout(
                        JVM_HANDLE_LAYOUT.withName("impl"),
                        JVM_HANDLE_LAYOUT.withName("root"),
                        JVM_HANDLE_LAYOUT.withName("invalidRanges"),
                        JVM_HANDLE_LAYOUT.withName("diagnostics"),
                        JVM_HANDLE_LAYOUT.withName("typeDefinitions"),
                        VMT_STUB_LAYOUT.withName("lookupType")
                ),
                SemanticModel.class
        );
    }

    private static MemorySegment lookupTypeImpl() {
        final var stub = SemanticModelObjectDescriptor.lookupTypeImpl;
        if (stub == null) {
            try {
                return SemanticModelObjectDescriptor.lookupTypeImpl = linker.upcallStub(
                        MethodHandles.lookup().findStatic(
                                SemanticModelObjectDescriptor.class, "lookupTypeImpl",
                                MethodType.methodType(JVM_HANDLE_CLASS, JVM_HANDLE_CLASS, STRING_CLASS)
                        ),
                        FunctionDescriptor.of(JVM_HANDLE_LAYOUT, JVM_HANDLE_LAYOUT, STRING_LAYOUT),
                        callStubArena
                );
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return stub;
    }

    private static long lookupTypeImpl(long impl, MemorySegment codeSegment) {
        try {
            final SemanticModel model = fromObjectHandle(impl);
            final var name = codeSegment.reinterpret(Integer.MAX_VALUE).getString(0);
            return toObjectHandle(model.lookupType(name));
        } catch (Throwable e) {
            fatalError(e);
            return toObjectHandle(null);
        }
    }

    @Override
    public void serialize(SemanticModel model) {
        set("impl", model);
        set("root", model.root());
        set("invalidRanges", List.copyOf(model.invalidRanges()));
        set("diagnostics", List.copyOf(model.diagnostics()));
        set("typeDefinitions", List.copyOf(model.typeDefinitions()));
        set("lookupType", lookupTypeImpl());
    }
}

final class SemanticSymbolObjectDescriptor extends ObjectDescriptor<SemanticSymbol> {
    private static volatile MemorySegment constructImpl;

    public SemanticSymbolObjectDescriptor() {
        super(
                MemoryLayout.structLayout(
                        JVM_HANDLE_LAYOUT.withName("impl"),
                        JVM_HANDLE_LAYOUT.withName("definition"),
                        JVM_HANDLE_LAYOUT.withName("owner"),
                        JVM_HANDLE_LAYOUT.withName("param1"),
                        JVM_HANDLE_LAYOUT.withName("param2"),
                        JVM_HANDLE_LAYOUT.withName("param3"),
                        JVM_HANDLE_LAYOUT.withName("param4"),
                        STRING_LAYOUT.withName("name"),
                        VMT_STUB_LAYOUT.withName("construct"),
                        ValueLayout.JAVA_INT.withName("kind"),
                        ValueLayout.JAVA_INT.withName("flags")
                ),
                SemanticSymbol.class
        );
    }

    private static MemorySegment constructImpl() {
        final var stub = SemanticSymbolObjectDescriptor.constructImpl;
        if (stub == null) {
            try {
                return SemanticSymbolObjectDescriptor.constructImpl = linker.upcallStub(
                        MethodHandles.lookup().findStatic(
                                SemanticSymbolObjectDescriptor.class, "constructImpl",
                                MethodType.methodType(JVM_HANDLE_CLASS, JVM_HANDLE_CLASS, int.class, ARRAY_CLASS)
                        ),
                        FunctionDescriptor.of(JVM_HANDLE_LAYOUT, JVM_HANDLE_LAYOUT, ValueLayout.JAVA_INT, ARRAY_LAYOUT),
                        callStubArena
                );
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return stub;
    }

    private static long constructImpl(long impl, int size, MemorySegment handleSegment) {
        try {
            final TypeSymbol symbol = fromObjectHandle(impl);
            final List<TypeLikeSymbol> objects = List.of(readHandleArray(size, handleSegment));
            return toObjectHandle(symbol.construct(objects));
        } catch (Throwable e) {
            fatalError(e);
            return toObjectHandle(null);
        }
    }

    @Override
    public void serialize(SemanticSymbol symbol) {
        set("impl", symbol);
        set("kind", symbolKind(symbol.kind()));
        set("name", symbol.name());
        set("definition", symbol.definition());
        if (symbol instanceof SemanticSymbolWithOwner withOwner) {
            set("owner", withOwner.owner());
        } else {
            setNull("owner");
        }
        switch (symbol) {
            case FunctionSymbol functionSymbol -> {
                set("flags", functionSymbol.isNative(), functionSymbol.isVirtual(), functionSymbol.isAbstract(), functionSymbol.isOverride());
                set("param1", functionSymbol.parameters());
                set("param2", functionSymbol.returnType());
                set("param3", functionSymbol.locals());
            }
            case VariableSymbol variableSymbol -> {
                set("param1", variableSymbol.type());
            }
            case TypeSymbol typeSymbol -> {
                set("flags", typeSymbol.isAbstract());
                set("param1", typeSymbol.baseTypes());
                set("param2", typeSymbol.typeArguments());
                set("param3", typeSymbol.originalDefinition());
                set("param4", typeSymbol.members());
                set("construct", constructImpl());
            }
            case TypeParameterSymbol typeParameterSymbol -> {
                set("param1", typeParameterSymbol.bounds());
            }
        }
    }
}
