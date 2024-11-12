package syspro.tm;

import syspro.tm.lexer.*;
import syspro.tm.parser.*;

import java.io.IOException;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public final class Library {

    private static final Linker linker = Linker.nativeLinker();
    private static final Arena arena = Arena.global();
    private static final SymbolLookup lookup;
    private static final String NATIVE_LIBRARY_TEMP_DIRECTORY_PREFIX = "SysPro.TM.JVM";
    private static final String LOCK_FILE_PREFIX = "lock-";
    private static final IdentityHashMap<Object, Long> objectHandles = new IdentityHashMap<>();
    private static final ArrayList<Object> objectReferences = new ArrayList<>();
    private static final ValueLayout.OfLong JVM_HANDLE_LAYOUT = ValueLayout.JAVA_LONG;
    private static final ValueLayout.OfLong LIB_HANDLE_LAYOUT = ValueLayout.JAVA_LONG;
    private static final AddressLayout VMT_STUB_LAYOUT = ValueLayout.ADDRESS;
    private static final AddressLayout ARRAY_LAYOUT = ValueLayout.ADDRESS;
    private static final AddressLayout STRING_LAYOUT = ValueLayout.ADDRESS;
    private static final Class<Long> JVM_HANDLE_CLASS = long.class;
    private static final Class<Long> LIB_HANDLE_CLASS = long.class;
    private static final Class<MemorySegment> ARRAY_CLASS = MemorySegment.class;
    private static final Class<MemorySegment> STRING_CLASS = MemorySegment.class;
    private static final List<ObjectDescriptor<?>> layouts = List.of(
            new TokenObjectDescriptor(),
            new IterableObjectDescriptor(),
            new LexerObjectDescriptor(),
            new ParserObjectDescriptor(),
            new ParseResultObjectDescriptor(),
            new SyntaxNodeObjectDescriptor(),
            new TextSpanObjectDescriptor(),
            new DiagnosticObjectDescriptor(),
            new ErrorCodeObjectDescriptor(),
            new DiagnosticArgumentObjectDescriptor()
    );
    private static volatile boolean hasFatalFailures;
    private static volatile Path nativeLibraryLockFile;
    private static volatile MethodHandle registerObjectResult;
    private static volatile MethodHandle registerTask1Solution;
    private static volatile MemorySegment lexImpl;
    private static volatile MethodHandle registerTask2Solution;
    private static volatile MemorySegment parseImpl;
    private static volatile MethodHandle startWebServer;
    private static volatile MethodHandle stopWebServer;
    private static volatile MethodHandle waitForWebServerExit;
    private static volatile MethodHandle waitForWebServerExitWithTimeout;


    static {
        final var libraryFile = extractNativeLibrary();
        lookup = SymbolLookup.libraryLookup(libraryFile, arena);
        final var nullHandle = toObjectHandle(null);
        assert nullHandle == 0;

        final var registerRegistrarHandle = linker.downcallHandle(
                findFunction("RegisterRegistrar"),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );

        try (Arena arena = Arena.ofConfined()) {
            new LibraryCall(arena) {

                @Override
                public void call() throws Throwable {
                    registerRegistrarHandle.invokeExact(
                            linker.upcallStub(
                                    MethodHandles.lookup().findStatic(
                                            Library.class, "registerObjectImpl",
                                            MethodType.methodType(void.class, LIB_HANDLE_CLASS, int.class, int.class, ARRAY_CLASS)
                                    ),
                                    FunctionDescriptor.ofVoid(
                                            LIB_HANDLE_LAYOUT,
                                            ValueLayout.JAVA_INT,
                                            ValueLayout.JAVA_INT,
                                            ARRAY_LAYOUT
                                    ),
                                    Library.arena
                            )
                    );
                }
            }.makeCall();
        }
    }

    private Library() {
    }

    private static MethodHandle registerObjectResult() {
        final var handle = Library.registerObjectResult;
        if (handle == null) {
            return Library.registerObjectResult = linker.downcallHandle(
                    findFunction("RegisterObjectResult"),
                    FunctionDescriptor.ofVoid(LIB_HANDLE_LAYOUT, ARRAY_LAYOUT)
            );
        }

        return handle;
    }

    private static MethodHandle registerTask1Solution() {
        final var handle = Library.registerTask1Solution;
        if (handle == null) {
            return Library.registerTask1Solution = linker.downcallHandle(
                    findFunction("RegisterTask1Solution"),
                    FunctionDescriptor.ofVoid(JVM_HANDLE_LAYOUT, ValueLayout.JAVA_INT)
            );
        }

        return handle;
    }

    private static MemorySegment lexImpl() {
        final var lexImpl = Library.lexImpl;
        if (lexImpl == null) {
            try {
                return Library.lexImpl = linker.upcallStub(
                        MethodHandles.lookup().findStatic(
                                Library.class, "lexImpl",
                                MethodType.methodType(JVM_HANDLE_CLASS, JVM_HANDLE_CLASS, STRING_CLASS)
                        ),
                        FunctionDescriptor.of(JVM_HANDLE_LAYOUT, JVM_HANDLE_LAYOUT, STRING_LAYOUT),
                        arena
                );
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return lexImpl;
    }

    private static MethodHandle registerTask2Solution() {
        final var handle = Library.registerTask2Solution;
        if (handle == null) {
            return Library.registerTask2Solution = linker.downcallHandle(
                    findFunction("RegisterTask2Solution"),
                    FunctionDescriptor.ofVoid(JVM_HANDLE_LAYOUT)
            );
        }

        return handle;
    }

    private static MemorySegment parseImpl() {
        final var parseImpl = Library.parseImpl;
        if (parseImpl == null) {
            try {
                return Library.parseImpl = linker.upcallStub(
                        MethodHandles.lookup().findStatic(
                                Library.class, "parseImpl",
                                MethodType.methodType(JVM_HANDLE_CLASS, JVM_HANDLE_CLASS, STRING_CLASS)
                        ),
                        FunctionDescriptor.of(JVM_HANDLE_LAYOUT, JVM_HANDLE_LAYOUT, STRING_LAYOUT),
                        arena
                );
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return parseImpl;
    }

    private static MethodHandle startWebServer() {
        final var handle = Library.startWebServer;
        if (handle == null) {
            return Library.startWebServer = linker.downcallHandle(
                    findFunction("StartWebServer"),
                    FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT)
            );
        }

        return handle;
    }

    private static MethodHandle stopWebServer0() {
        final var handle = Library.stopWebServer;
        if (handle == null) {
            return Library.stopWebServer = linker.downcallHandle(
                    findFunction("StopWebServer"),
                    FunctionDescriptor.ofVoid()
            );
        }

        return handle;
    }

    private static MethodHandle waitForWebServerExit0() {
        final var handle = Library.waitForWebServerExit;
        if (handle == null) {
            return Library.waitForWebServerExit = linker.downcallHandle(
                    findFunction("WaitForWebServerExit"),
                    FunctionDescriptor.ofVoid()
            );
        }

        return handle;
    }

    private static MethodHandle waitForWebServerExitWithTimeout() {
        final var handle = Library.waitForWebServerExitWithTimeout;
        if (handle == null) {
            return Library.waitForWebServerExitWithTimeout = linker.downcallHandle(
                    findFunction("WaitForWebServerExitWithTimeout"),
                    FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG)
            );
        }

        return handle;
    }

    private static long toObjectHandle(Object object) {
        synchronized (objectHandles) {
            final var handle = objectHandles.get(object);
            if (handle == null) {
                final long newHandle = objectReferences.size();
                objectReferences.add(object);
                objectHandles.put(object, newHandle);
                return newHandle;
            }

            return handle;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T fromObjectHandle(int handle) {
        synchronized (objectHandles) {
            return (T) objectReferences.get(handle);
        }
    }

    private static <T> T fromObjectHandle(long handle) {
        return fromObjectHandle(Math.toIntExact(handle));
    }

    @SuppressWarnings("unchecked")
    private static void registerObjectImpl(long request, int kind, int count, MemorySegment handleArraySegment) {
        handleArraySegment = handleArraySegment.reinterpret(count * JVM_HANDLE_LAYOUT.byteSize());
        final var desc = (ObjectDescriptor<Object>) layouts.get(kind);
        try (final var arena = Arena.ofConfined()) {
            final var list = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                list.add(fromObjectHandle(handleArraySegment.getAtIndex(JVM_HANDLE_LAYOUT, i)));
            }

            new LibraryCall(arena) {

                @Override
                public void call() throws Throwable {
                    final var call = this;
                    final var lexResult = new ListSerializer<>(desc.layout) {

                        @Override
                        public void serializeItem(Object token, MemorySegment segment) {
                            desc.serialize(token, segment, call);
                        }
                    }.serializeList(list);

                    registerObjectResult().invokeExact(request, lexResult);
                }
            }.makeCall();
        }
    }

    static void registerTask1Solution(Lexer impl, TestMode mode) {
        try (final var arena = Arena.ofConfined()) {
            new LibraryCall(arena) {

                @Override
                public void call() throws Throwable {
                    registerTask1Solution().invokeExact(toObjectHandle(impl), mode.toMask());
                }
            }.makeCall();
        } finally {
            exitOnFatalErrors();
        }
    }

    private static MemorySegment findFunction(String name) {
        return lookup.find(name).orElseThrow(() -> new RuntimeException('`' + name + "` was not found in SysPro.TM.Library, faulty build? Ask the teacher."));
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

    static void registerTask2Solution(Parser impl) {
        try (final var arena = Arena.ofConfined()) {
            new LibraryCall(arena) {

                @Override
                public void call() throws Throwable {
                    registerTask2Solution().invokeExact(toObjectHandle(impl));
                }
            }.makeCall();
        } finally {
            exitOnFatalErrors();
        }
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

    static void startWebServer(int port) {
        try (final var arena = Arena.ofConfined()) {
            new LibraryCall(arena) {

                @Override
                public void call() throws Throwable {
                    startWebServer().invokeExact(port);
                }
            }.makeCall();
        } finally {
            exitOnFatalErrors();
        }
    }

    static void stopWebServer() {
        try (final var arena = Arena.ofConfined()) {
            new LibraryCall(arena) {

                @Override
                public void call() throws Throwable {
                    stopWebServer0().invokeExact();
                }
            }.makeCall();
        } finally {
            exitOnFatalErrors();
        }
    }

    static void waitForWebServerExit() {
        try (final var arena = Arena.ofConfined()) {
            new LibraryCall(arena) {

                @Override
                public void call() throws Throwable {
                    waitForWebServerExit0().invokeExact();
                }
            }.makeCall();
        } finally {
            exitOnFatalErrors();
        }
    }

    static void waitForWebServerExitWithTimeout(long timeoutMillis) {
        try (final var arena = Arena.ofConfined()) {
            new LibraryCall(arena) {

                @Override
                public void call() throws Throwable {
                    waitForWebServerExitWithTimeout().invokeExact(timeoutMillis);
                }
            }.makeCall();
        } finally {
            exitOnFatalErrors();
        }
    }

    private static void exitOnFatalErrors() {
        if (hasFatalFailures) {
            System.err.println("Fatal errors occurred.");
            System.exit(1);
        }
    }

    private static void fatalError(Throwable e) {
        e.printStackTrace();
        hasFatalFailures = true;
    }

    private static int tokenKind(Token token) {
        return switch (token) {
            case BadToken _ -> 0;
            case IdentifierToken _ -> 3;
            case IndentationToken indentationToken -> indentationToken.isIndent() ? 1 : 2;
            case KeywordToken keywordToken -> tokenKind(keywordToken.keyword);
            case BooleanLiteralToken _ -> 4;
            case IntegerLiteralToken _ -> 5;
            case RuneLiteralToken _ -> 6;
            case StringLiteralToken _ -> 7;
            case SymbolToken symbolToken -> tokenKind(symbolToken.symbol);
        };
    }

    private static int tokenKind(Keyword keyword) {
        return 100 + keyword.ordinal();
    }

    private static int tokenKind(Symbol symbol) {
        return 200 + symbol.ordinal();
    }

    private static int tokenKind(SyntaxKind kind) {
        final var ordinal = kind.ordinal();
        if (ordinal < SyntaxKind.SOURCE_TEXT.ordinal()) {
            return ordinal;
        }
        return 2000 + ordinal - SyntaxKind.SOURCE_TEXT.ordinal();
    }

    private static int syntaxKind(AnySyntaxKind kind) {
        return switch (kind) {
            case Keyword keyword -> tokenKind(keyword);
            case Symbol symbol -> tokenKind(symbol);
            case SyntaxKind nonTerminalKind -> tokenKind(nonTerminalKind);
            default -> throw new IllegalStateException("Unexpected value: " + kind);
        };
    }

    private static Path extractNativeLibrary() {
        final var property = System.getProperty("syspro.tm.library");
        if (property != null) {
            final var path = Paths.get(property);
            if (path.toFile().exists()) {
                return path;
            }

            System.err.println("Native library `" + path + "` doesn't exist");
            System.exit(1);
        }

        final var platform = platform();

        try {
            final var libraryFileSuffix = platform.isWindows() ? ".dll" : ".so";
            final var sourceFileName = "SysPro.TM.Library" + libraryFileSuffix;
            final var destinationDirectory = Files.createTempDirectory(NATIVE_LIBRARY_TEMP_DIRECTORY_PREFIX);
            final var destinationFile = destinationDirectory.resolve(sourceFileName);
            final var currentProcess = ProcessHandle.current();
            nativeLibraryLockFile = destinationDirectory.resolve(LOCK_FILE_PREFIX + currentProcess.pid());
            if (!destinationFile.toFile().exists()) {
                final var sourceFilePath = "/" + platform.runtimeIdentifier + "/" + sourceFileName;
                final var link = Library.class.getResourceAsStream(sourceFilePath);
                if (link == null) {
                    System.err.println('`' + sourceFilePath + "` was not found in SysPro.TM.JVM.jar, faulty build? Ask the teacher.");
                    System.exit(1);
                }

                Files.copy(link, destinationFile);
            }
            Files.writeString(nativeLibraryLockFile, lockFileContents(currentProcess));
            Runtime.getRuntime().addShutdownHook(Thread.ofPlatform().name("SysPro.TM.Library Native Library Lock Release Thread").unstarted(Library::releaseNativeLibraryLock));
            Thread.ofPlatform().name("SysPro.TM.Library Native Library Clean-Up Thread").priority(Thread.MIN_PRIORITY).start(Library::cleanUpNativeLibrary);
            return destinationFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String lockFileContents(ProcessHandle handle) {
        return handle.info().command().orElse("<null>").strip();
    }

    private static void releaseNativeLibraryLock() {
        try {
            final var nativeLibraryLockFile = Library.nativeLibraryLockFile;
            if (nativeLibraryLockFile == null) {
                return;
            }

            Files.deleteIfExists(nativeLibraryLockFile);
        } catch (Throwable _) {
        }
    }

    private static void cleanUpNativeLibrary() {
        try {
            final var nativeLibraryLockFile = Library.nativeLibraryLockFile;
            if (nativeLibraryLockFile == null) {
                return;
            }

            final List<Path> tempPathList;
            try (var tempPathStream = Files.list(nativeLibraryLockFile.getParent().getParent())) {
                tempPathList = tempPathStream.toList();
            }

            for (final var tempPathEntry : tempPathList) {
                if (!Files.isDirectory(tempPathEntry)) {
                    continue;
                }

                if (!tempPathEntry.getFileName().toString().startsWith(NATIVE_LIBRARY_TEMP_DIRECTORY_PREFIX)) {
                    continue;
                }

                if (isLocked(tempPathEntry)) {
                    continue;
                }

                final List<Path> list;
                try (var dirStream = Files.walk(tempPathEntry)) {
                    list = new ArrayList<>(dirStream.toList());
                }

                Collections.reverse(list);
                for (var path : list) {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Throwable _) {
                    }
                }
            }
        } catch (Throwable _) {
        }
    }

    private static boolean isLocked(Path tempPathEntry) throws IOException {
        try (var libDirectoryStream = Files.list(tempPathEntry)) {
            final Iterable<Path> libDirectoryIterable = libDirectoryStream::iterator;
            for (final var libDirectoryEntry : libDirectoryIterable) {
                if (!Files.isRegularFile(libDirectoryEntry)) {
                    continue;
                }

                final var fileName = libDirectoryEntry.getFileName().toString();
                if (!fileName.startsWith(LOCK_FILE_PREFIX)) {
                    continue;
                }

                final var fileNameSuffix = fileName.substring(LOCK_FILE_PREFIX.length());
                final var pid = Long.parseLong(fileNameSuffix);
                final var handleOptional = ProcessHandle.of(pid);
                if (handleOptional.isEmpty()) {
                    continue;
                }

                final var expectedLockFileContents = lockFileContents(handleOptional.get());
                final var actualLockFileContents = Files.readString(libDirectoryEntry).strip();

                if (!expectedLockFileContents.equals(actualLockFileContents)) {
                    continue;
                }

                return true;
            }

            return false;
        }
    }

    private static Platform platform() {
        final var name = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        final var arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
        final var isWindows = name.contains("win");
        final var isLinux = name.contains("linux");

        if (arch.contains("aarch64")) {
            if (isLinux) {
                return Platform.LINUX_AARCH64;
            }
        }

        if ((arch.contains("86") || arch.contains("amd")) && arch.contains("64")) {
            if (isWindows) {
                return Platform.WINDOWS_AMD64;
            }
            if (isLinux) {
                return Platform.LINUX_AMD64;
            }
        }

        throw new RuntimeException("Only " + Arrays.toString(Platform.values()) + " platforms are supported, OS is \"" + name + "\", architecture is \"" + arch + '"');
    }

    private enum Platform {
        WINDOWS_AMD64("Windows AMD64", "win-x64"), LINUX_AMD64("Linux AMD64", "linux-x64"), LINUX_AARCH64("Linux AArch64", "linux-arm64"),
        ;

        public final String readableName;
        public final String runtimeIdentifier;

        Platform(String readableName, String runtimeIdentifier) {
            this.readableName = readableName;
            this.runtimeIdentifier = runtimeIdentifier;
        }

        public boolean isWindows() {
            return switch (this) {
                case WINDOWS_AMD64 -> true;
                case LINUX_AMD64, LINUX_AARCH64 -> false;
            };
        }

        public boolean isLinux() {
            return switch (this) {
                case LINUX_AMD64, LINUX_AARCH64 -> true;
                case WINDOWS_AMD64 -> false;
            };
        }

        public boolean isAMD64() {
            return switch (this) {
                case WINDOWS_AMD64, LINUX_AMD64 -> true;
                case LINUX_AARCH64 -> false;
            };
        }

        public boolean isAArch64() {
            return switch (this) {
                case LINUX_AARCH64 -> true;
                case WINDOWS_AMD64, LINUX_AMD64 -> false;
            };
        }
    }

    private static abstract class ObjectDescriptor<T> {
        private final MemoryLayout layout;

        protected ObjectDescriptor(MemoryLayout layout) {
            this.layout = layout;
        }

        public abstract void serialize(T object, MemorySegment segment, LibraryCall call);
    }

    private static abstract class LibraryCall {
        private final Arena arena;
        private final IdentityHashMap<String, MemorySegment> strings = new IdentityHashMap<>();

        public LibraryCall(Arena arena) {
            assert arena != null;
            this.arena = arena;
        }

        protected MemorySegment serializeString(String string) {
            final var interned = string.intern();
            var segment = strings.get(interned);
            if (segment == null) {
                segment = arena.allocateFrom(string);
                strings.put(interned, segment);
            }
            return segment;
        }

        protected MemorySegment serializeObjectHandles(List<Long> items) {
            final var size = items.size();
            final var segment = arena.allocate(JVM_HANDLE_LAYOUT, size);
            for (var i = 0; i < size; i++) {
                segment.setAtIndex(JVM_HANDLE_LAYOUT, i, items.get(i));
            }
            return segment;
        }

        public final void makeCall() {
            try {
                call();
            } catch (Throwable e) {
                fatalError(e);
            }
        }

        public abstract void call() throws Throwable;
    }

    private static abstract class ListSerializer<T> {
        private final MemoryLayout itemLayout;

        public ListSerializer(MemoryLayout itemLayout) {
            assert itemLayout != null;
            this.itemLayout = itemLayout;
        }

        public MemorySegment serializeList(List<T> list) {
            final var size = list.size();
            final var totalByteCount = itemLayout.scale(ValueLayout.JAVA_LONG.byteSize(), size);
            final var segment = arena.allocate(totalByteCount);
            segment.set(ValueLayout.JAVA_LONG, 0, size);
            for (int i = 0; i < size; i++) {
                final var slice = segment.asSlice(itemLayout.scale(ValueLayout.JAVA_LONG.byteSize(), i), itemLayout);
                serializeItem(list.get(i), slice);
            }
            return segment;
        }

        public abstract void serializeItem(T token, MemorySegment segment);
    }

    private static final class TokenObjectDescriptor extends ObjectDescriptor<Token> {
        final long kind = ValueLayout.JAVA_INT.scale(0, 0);
        final long start = ValueLayout.JAVA_INT.scale(0, 1);
        final long end = ValueLayout.JAVA_INT.scale(0, 2);
        final long leadingTriviaLength = ValueLayout.JAVA_INT.scale(0, 3);
        final long trailingTriviaLength = ValueLayout.JAVA_INT.scale(0, 4);
        final long type = ValueLayout.JAVA_INT.scale(0, 5);
        final long value1 = ValueLayout.JAVA_INT.scale(0, 6);
        final long value2 = ValueLayout.JAVA_LONG.scale(value1 + ValueLayout.ADDRESS.byteSize(), 0);
        final long value3 = ValueLayout.JAVA_LONG.scale(value1 + ValueLayout.ADDRESS.byteSize(), 1);

        public TokenObjectDescriptor() {
            super(
                    MemoryLayout.structLayout(
                            ValueLayout.JAVA_INT,  // kind
                            ValueLayout.JAVA_INT,  // start
                            ValueLayout.JAVA_INT,  // end
                            ValueLayout.JAVA_INT,  // leadingTriviaLength
                            ValueLayout.JAVA_INT,  // trailingTriviaLength
                            ValueLayout.JAVA_INT,  // type
                            ValueLayout.ADDRESS,   // value1
                            ValueLayout.JAVA_LONG, // value2
                            ValueLayout.JAVA_LONG  // value3
                    )
            );
        }

        @Override
        public void serialize(Token token, MemorySegment segment, LibraryCall call) {
            segment.set(ValueLayout.JAVA_INT, kind, tokenKind(token));
            segment.set(ValueLayout.JAVA_INT, start, token.start);
            segment.set(ValueLayout.JAVA_INT, end, token.end);
            segment.set(ValueLayout.JAVA_INT, leadingTriviaLength, token.leadingTriviaLength);
            segment.set(ValueLayout.JAVA_INT, trailingTriviaLength, token.trailingTriviaLength);
            switch (token) {
                case IdentifierToken identifierToken:
                    segment.set(ValueLayout.ADDRESS, value1, call.serializeString(identifierToken.value));
                    segment.set(ValueLayout.JAVA_LONG, value3, identifierToken.contextualKeyword != null ? tokenKind(identifierToken.contextualKeyword) : 0);
                    break;
                case BadToken _, IndentationToken _, KeywordToken _, SymbolToken _:
                    break;
                case BooleanLiteralToken literalToken:
                    segment.set(ValueLayout.JAVA_INT, type, literalToken.type.ordinal());
                    segment.set(ValueLayout.JAVA_LONG, value2, literalToken.value ? 1 : 0);
                    break;
                case IntegerLiteralToken literalToken:
                    segment.set(ValueLayout.JAVA_INT, type, literalToken.type.ordinal());
                    segment.set(ValueLayout.JAVA_LONG, value2, literalToken.value);
                    segment.set(ValueLayout.JAVA_LONG, value3, literalToken.hasTypeSuffix ? 1 : 0);
                    break;
                case RuneLiteralToken literalToken:
                    segment.set(ValueLayout.JAVA_INT, type, literalToken.type.ordinal());
                    segment.set(ValueLayout.JAVA_LONG, value3, literalToken.value);
                    break;
                case StringLiteralToken literalToken:
                    segment.set(ValueLayout.JAVA_INT, type, literalToken.type.ordinal());
                    segment.set(ValueLayout.ADDRESS, value1, call.serializeString(literalToken.value));
                    break;
            }
        }
    }

    private static final class IterableObjectDescriptor extends ObjectDescriptor<Iterable<Object>> {
        final long size = ValueLayout.JAVA_INT.scale(0, 0);
        final long data = ValueLayout.JAVA_INT.scale(0, 2);

        public IterableObjectDescriptor() {
            super(
                    MemoryLayout.structLayout(
                            ValueLayout.JAVA_INT, // size
                            ValueLayout.JAVA_INT, // padding
                            ValueLayout.ADDRESS   // data
                    )
            );
        }

        @Override
        public void serialize(Iterable<Object> items, MemorySegment segment, LibraryCall call) {
            final var handles = new ArrayList<Long>(items instanceof Collection<?> collection ? collection.size() : 10);
            for (var item : items) {
                handles.add(toObjectHandle(item));
            }

            segment.set(ValueLayout.JAVA_INT, size, handles.size());
            segment.set(ValueLayout.ADDRESS, data, call.serializeObjectHandles(handles));
        }
    }

    private static final class LexerObjectDescriptor extends ObjectDescriptor<Lexer> {
        final long impl = JVM_HANDLE_LAYOUT.scale(0, 0);
        final long lex = JVM_HANDLE_LAYOUT.scale(0, 1);

        public LexerObjectDescriptor() {
            super(
                    MemoryLayout.structLayout(
                            JVM_HANDLE_LAYOUT,   // impl
                            VMT_STUB_LAYOUT  // lex
                    )
            );
        }

        @Override
        public void serialize(Lexer lexer, MemorySegment segment, LibraryCall call) {
            segment.set(JVM_HANDLE_LAYOUT, impl, toObjectHandle(lexer));
            segment.set(VMT_STUB_LAYOUT, lex, lexImpl());
        }
    }

    private static final class ParserObjectDescriptor extends ObjectDescriptor<Parser> {
        final long impl = JVM_HANDLE_LAYOUT.scale(0, 0);
        final long parse = JVM_HANDLE_LAYOUT.scale(0, 1);

        public ParserObjectDescriptor() {
            super(
                    MemoryLayout.structLayout(
                            JVM_HANDLE_LAYOUT,   // impl
                            VMT_STUB_LAYOUT  // parse
                    )
            );
        }

        @Override
        public void serialize(Parser parser, MemorySegment segment, LibraryCall call) {
            segment.set(JVM_HANDLE_LAYOUT, impl, toObjectHandle(parser));
            segment.set(VMT_STUB_LAYOUT, parse, parseImpl());
        }
    }

    private static final class ParseResultObjectDescriptor extends ObjectDescriptor<ParseResult> {
        final long root = JVM_HANDLE_LAYOUT.scale(0, 0);
        final long invalidRanges = JVM_HANDLE_LAYOUT.scale(0, 1);
        final long diagnostics = JVM_HANDLE_LAYOUT.scale(0, 2);

        public ParseResultObjectDescriptor() {
            super(
                    MemoryLayout.structLayout(
                            JVM_HANDLE_LAYOUT, // root
                            JVM_HANDLE_LAYOUT, // invalidRanges
                            JVM_HANDLE_LAYOUT  // diagnostics
                    )
            );
        }

        @Override
        public void serialize(ParseResult result, MemorySegment segment, LibraryCall call) {
            segment.set(JVM_HANDLE_LAYOUT, root, toObjectHandle(result.root()));
            segment.set(JVM_HANDLE_LAYOUT, invalidRanges, toObjectHandle(result.invalidRanges()));
            segment.set(JVM_HANDLE_LAYOUT, diagnostics, toObjectHandle(result.diagnostics()));
        }
    }

    private static final class SyntaxNodeObjectDescriptor extends ObjectDescriptor<SyntaxNode> {
        final long kind = ValueLayout.JAVA_INT.scale(0, 0);
        final long position = ValueLayout.JAVA_INT.scale(0, 1);
        final long fullLength = ValueLayout.JAVA_INT.scale(0, 2);
        final long leadingTriviaLength = ValueLayout.JAVA_INT.scale(0, 3);
        final long trailingTriviaLength = ValueLayout.JAVA_INT.scale(0, 4);
        final long slotCount = ValueLayout.JAVA_INT.scale(0, 5);
        final long token = ValueLayout.JAVA_INT.scale(0, 6);
        final long slots = token + JVM_HANDLE_LAYOUT.scale(0, 1);

        public SyntaxNodeObjectDescriptor() {
            super(
                    MemoryLayout.structLayout(
                            ValueLayout.JAVA_INT,  // kind
                            ValueLayout.JAVA_INT,  // position
                            ValueLayout.JAVA_INT,  // fullLength
                            ValueLayout.JAVA_INT,  // leadingTriviaLength
                            ValueLayout.JAVA_INT,  // trailingTriviaLength
                            ValueLayout.JAVA_INT,  // slotCount
                            JVM_HANDLE_LAYOUT,     // token
                            JVM_HANDLE_LAYOUT      // slots
                    )
            );
        }

        @Override
        public void serialize(SyntaxNode node, MemorySegment segment, LibraryCall call) {
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

            segment.set(ValueLayout.JAVA_INT, kind, syntaxKind(node.kind()));
            segment.set(ValueLayout.JAVA_INT, position, node.position());
            segment.set(ValueLayout.JAVA_INT, fullLength, node.fullLength());
            segment.set(ValueLayout.JAVA_INT, leadingTriviaLength, node.leadingTriviaLength());
            segment.set(ValueLayout.JAVA_INT, trailingTriviaLength, node.trailingTriviaLength());
            segment.set(ValueLayout.JAVA_INT, slotCount, nodeSlotCount);
            segment.set(JVM_HANDLE_LAYOUT, token, toObjectHandle(node.token()));
            segment.set(JVM_HANDLE_LAYOUT, slots, toObjectHandle(nodeSlots));
        }
    }

    private static final class TextSpanObjectDescriptor extends ObjectDescriptor<TextSpan> {
        final long start = ValueLayout.JAVA_INT.scale(0, 0);
        final long length = ValueLayout.JAVA_INT.scale(0, 1);

        public TextSpanObjectDescriptor() {
            super(
                    MemoryLayout.structLayout(
                            ValueLayout.JAVA_INT,  // start
                            ValueLayout.JAVA_INT   // length
                    )
            );
        }

        @Override
        public void serialize(TextSpan span, MemorySegment segment, LibraryCall call) {
            segment.set(ValueLayout.JAVA_INT, start, span.start);
            segment.set(ValueLayout.JAVA_INT, length, span.length);
        }
    }

    private static final class DiagnosticObjectDescriptor extends ObjectDescriptor<Diagnostic> {
        final long errorCode = JVM_HANDLE_LAYOUT.scale(0, 0);
        final long arguments = JVM_HANDLE_LAYOUT.scale(0, 1);
        final long location = JVM_HANDLE_LAYOUT.scale(0, 2);
        final long hints = JVM_HANDLE_LAYOUT.scale(0, 3);

        public DiagnosticObjectDescriptor() {
            super(
                    MemoryLayout.structLayout(
                            JVM_HANDLE_LAYOUT,  // errorCode
                            JVM_HANDLE_LAYOUT,  // arguments
                            JVM_HANDLE_LAYOUT,  // location
                            JVM_HANDLE_LAYOUT   // hints
                    )
            );
        }

        @Override
        public void serialize(Diagnostic diagnostic, MemorySegment segment, LibraryCall call) {
            final var argumentsArray = diagnostic.arguments();
            final var diagnosticArguments = new DiagnosticArgument[argumentsArray.length];
            for (var i = 0; i < argumentsArray.length; i++) {
                diagnosticArguments[i] = new DiagnosticArgument(argumentsArray[i]);
            }
            segment.set(JVM_HANDLE_LAYOUT, errorCode, toObjectHandle(diagnostic.errorCode()));
            segment.set(JVM_HANDLE_LAYOUT, arguments, toObjectHandle(diagnosticArguments));
            segment.set(JVM_HANDLE_LAYOUT, location, toObjectHandle(diagnostic.location()));
            segment.set(JVM_HANDLE_LAYOUT, hints, toObjectHandle(diagnostic.hints()));
        }
    }

    private static final class ErrorCodeObjectDescriptor extends ObjectDescriptor<ErrorCode> {
        final long name = ValueLayout.ADDRESS.scale(0, 0);

        public ErrorCodeObjectDescriptor() {
            super(
                    MemoryLayout.structLayout(
                            ValueLayout.ADDRESS  // name
                    )
            );
        }

        @Override
        public void serialize(ErrorCode errorCode, MemorySegment segment, LibraryCall call) {
            segment.set(ValueLayout.ADDRESS, name, call.serializeString(errorCode.name()));
        }
    }

    private static final class DiagnosticArgumentObjectDescriptor extends ObjectDescriptor<DiagnosticArgument> {
        final long stringValue = ValueLayout.ADDRESS.scale(0, 0);
        final long nodeValue = ValueLayout.ADDRESS.scale(0, 1);

        public DiagnosticArgumentObjectDescriptor() {
            super(
                    MemoryLayout.structLayout(
                            ValueLayout.ADDRESS,  // stringValue
                            JVM_HANDLE_LAYOUT     // nodeValue
                    )
            );
        }

        @Override
        public void serialize(DiagnosticArgument argument, MemorySegment segment, LibraryCall call) {
            final var object = argument.object;
            segment.set(ValueLayout.ADDRESS, stringValue, call.serializeString(Objects.toString(object)));
            segment.set(JVM_HANDLE_LAYOUT, nodeValue, toObjectHandle(object instanceof SyntaxNode ? object : null));
        }
    }

    private static final class DiagnosticArgument {
        final Object object;

        DiagnosticArgument(Object object) {
            this.object = object;
        }
    }
}
