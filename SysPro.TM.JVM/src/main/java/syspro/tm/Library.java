package syspro.tm;

import syspro.tm.lexer.*;
import syspro.tm.parser.*;
import syspro.tm.symbols.*;

import java.io.IOException;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

final class Library {

    private static final Linker linker = Linker.nativeLinker();
    private static final Arena callStubArena = Arena.global();
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
            new DiagnosticArgumentObjectDescriptor(),
            new LanguageServerObjectDescriptor(),
            new SemanticModelObjectDescriptor(),
            new SemanticSymbolObjectDescriptor()
    );
    private static volatile boolean hasFatalFailures;
    private static volatile boolean isFatalFailureExit;
    private static volatile Path nativeLibraryLockFile;
    private static volatile MethodHandle registerObjectResult;
    private static volatile MethodHandle registerTask1Solution;
    private static volatile MethodHandle registerTask2Solution;
    private static volatile MethodHandle registerTask3Solution;
    private static volatile MethodHandle startWebServer;
    private static volatile MethodHandle stopWebServer;
    private static volatile MethodHandle waitForWebServerExit;
    private static volatile MethodHandle waitForWebServerExitWithTimeout;
    private static volatile MethodHandle addTestFilter;
    private static volatile MethodHandle clearTestFilters;


    static {
        final var libraryFile = extractNativeLibrary();
        lookup = SymbolLookup.libraryLookup(libraryFile, Arena.global());
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
                                            LIB_HANDLE_LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ARRAY_LAYOUT
                                    ),
                                    Library.callStubArena
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

    private static MethodHandle registerTask3Solution() {
        final var handle = Library.registerTask3Solution;
        if (handle == null) {
            return Library.registerTask3Solution = linker.downcallHandle(
                    findFunction("RegisterTask3Solution"),
                    FunctionDescriptor.ofVoid(JVM_HANDLE_LAYOUT)
            );
        }

        return handle;
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

    private static MethodHandle addTestFilter() {
        final var handle = Library.addTestFilter;
        if (handle == null) {
            return Library.addTestFilter = linker.downcallHandle(
                    findFunction("AddTestFilter"),
                    FunctionDescriptor.ofVoid(STRING_LAYOUT, ValueLayout.JAVA_INT)
            );
        }

        return handle;
    }

    private static MethodHandle clearTestFilters0() {
        final var handle = Library.clearTestFilters;
        if (handle == null) {
            return Library.clearTestFilters = linker.downcallHandle(
                    findFunction("ClearTestFilters"),
                    FunctionDescriptor.ofVoid()
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
        final var list = Arrays.asList(readHandleArray(count, handleArraySegment));

        final var desc = (ObjectDescriptor<Object>) layouts.get(kind);
        try (final var arena = Arena.ofConfined()) {
            new LibraryCall(arena) {

                @Override
                public void call() throws Throwable {
                    registerObjectResult().invokeExact(request, serializeFlatObjects(list, desc));
                }
            }.makeCall();
        }
    }

    private static <T> T[] readHandleArray(int count, MemorySegment handleArraySegment) {
        handleArraySegment = handleArraySegment.reinterpret(count * JVM_HANDLE_LAYOUT.byteSize());

        final var array = (T[]) new Object[count];
        for (int i = 0; i < count; i++) {
            array[i] = fromObjectHandle(handleArraySegment.getAtIndex(JVM_HANDLE_LAYOUT, i));
        }

        return array;
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

    static void registerTask3Solution(LanguageServer impl) {
        try (final var arena = Arena.ofConfined()) {
            new LibraryCall(arena) {

                @Override
                public void call() throws Throwable {
                    registerTask3Solution().invokeExact(toObjectHandle(impl));
                }
            }.makeCall();
        } finally {
            exitOnFatalErrors();
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

    static void addTestFilter(String filter, int flags) {
        try (final var arena = Arena.ofConfined()) {
            new LibraryCall(arena) {

                @Override
                public void call() throws Throwable {
                    addTestFilter().invokeExact(serializeString(filter), flags);
                }
            }.makeCall();
        } finally {
            exitOnFatalErrors();
        }
    }

    static void clearTestFilters() {
        try (final var arena = Arena.ofConfined()) {
            new LibraryCall(arena) {

                @Override
                public void call() throws Throwable {
                    clearTestFilters0().invokeExact();
                }
            }.makeCall();
        } finally {
            exitOnFatalErrors();
        }
    }

    private static void exitOnFatalErrors() {
        if (hasFatalFailures && !isFatalFailureExit) {
            System.err.println("Fatal errors occurred.");

            // Set the flag, so that we will never deadlock with shutdown hooks or have infinite recursion.
            isFatalFailureExit = true;

            if (WebServer.shouldWaitForWebServerExitOnFatalErrors) {
                System.err.println("Waiting for Dev Tools web server to stop.");
                System.err.println("Warning: the internal state might be inconsistent after fatal errors. Be sceptical.");

                waitForWebServerExit();
            }

            System.exit(1);
        }
    }

    private static void fatalError(Throwable e) {
        e.printStackTrace();
        hasFatalFailures = true;
    }

    private static int tokenKind(Keyword keyword) {
        return 100 + keyword.ordinal();
    }

    private static int tokenKind(Symbol symbol) {
        return 200 + symbol.ordinal();
    }

    private static int syntaxKind(SyntaxKind kind) {
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
            case SyntaxKind nonTerminalKind -> syntaxKind(nonTerminalKind);
            default -> throw new IllegalStateException("Unexpected value: " + kind + ". You can only return Keyword, Symbol or SyntaxKind from SyntaxNode.kind().");
        };
    }

    private static int symbolKind(SymbolKind kind) {
        return kind.ordinal();
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
        private final ThreadLocal<LibraryCall> call = ThreadLocal.withInitial(() -> null);
        private final ThreadLocal<MemorySegment> segment = ThreadLocal.withInitial(() -> null);
        private final MemoryLayout layout;
        private final Class<T> type;

        protected ObjectDescriptor(MemoryLayout layout, Class<T> type) {
            this.layout = layout;
            this.type = type;
        }

        private boolean supports(Class<?> type) {
            return this.type.isAssignableFrom(type);
        }

        private long offset(String name) {
            return layout.byteOffset(MemoryLayout.PathElement.groupElement(name));
        }

        private MemoryLayout layout(String name) {
            return layout.select(MemoryLayout.PathElement.groupElement(name));
        }

        public final void serialize(T object, MemorySegment segment, LibraryCall call) {
            final var oldCall = this.call.get();
            final var oldSegment = this.segment.get();
            try {
                this.call.set(call);
                this.segment.set(segment);
                serialize(object);
            } finally {
                this.call.set(oldCall);
                this.segment.set(oldSegment);
            }
        }

        protected final void set(String name, boolean flag) {
            set(name, new boolean[] {flag});
        }

        protected final void set(String name, boolean... flags) {
            var result = 0;
            var bit = 0;
            assert flags.length > 0 && flags.length < 32 : flags.length;
            for (final var flag : flags) {
                if (flag) {
                    result |= (1 << bit);
                }
                bit++;
            }
            set(name, result);
        }

        protected final void set(String name, int value) {
            segment().set((ValueLayout.OfInt) layout(name), offset(name), value);
        }

        protected final void set(String name, long value) {
            segment().set((ValueLayout.OfLong) layout(name), offset(name), value);
        }

        protected final void set(String name, Enum<?> value) {
            assert Objects.equals(layout(name).withoutName(), ValueLayout.JAVA_INT) : name + ": " + layout(name);
            assert value != null : name;
            set(name, value.ordinal());
        }

        protected final void set(String name, String value) {
            assert Objects.equals(layout(name).withoutName(), STRING_LAYOUT) : name + ": " + layout(name);
            segment().set(STRING_LAYOUT, offset(name), call().serializeString(value));
        }

        protected final void set(String name, MemorySegment stub) {
            assert Objects.equals(layout(name).withoutName(), VMT_STUB_LAYOUT) : name + ": " + layout(name);
            segment().set(VMT_STUB_LAYOUT, offset(name), stub);
        }

        protected final void setArray(String name, List<?> items) {
            final var offset = offset(name);
            assert Objects.equals(layout(name).withoutName(), ValueLayout.ADDRESS) : name + ": " + layout(name);

            if (items == null) {
                assert toObjectHandle(null) == 0;
                segment().set(ValueLayout.ADDRESS, offset, MemorySegment.NULL);
                return;
            }

            final var size = items.size();
            final var segment = call().arena.allocate(JVM_HANDLE_LAYOUT, size);
            for (var i = 0; i < size; i++) {
                segment.setAtIndex(JVM_HANDLE_LAYOUT, i, toObjectHandle(items.get(i)));
            }
            segment().set(ValueLayout.ADDRESS, offset, segment);
        }

        protected final void set(String name, Object value) {
            final var offset = offset(name);
            assert Objects.equals(layout(name).withoutName(), JVM_HANDLE_LAYOUT) : name + ": " + layout(name);

            if (value == null) {
                assert toObjectHandle(null) == 0;
                segment().set(JVM_HANDLE_LAYOUT, offset, 0);
                return;
            }

            if (value instanceof String || value instanceof MemorySegment || value instanceof Enum<?>) {
                throw new RuntimeException("Wrong overload");
            }

            final var type = value.getClass();
            for (final var descriptor : layouts) {
                if (descriptor.supports(type)) {
                    segment().set(JVM_HANDLE_LAYOUT, offset, toObjectHandle(value));
                    return;
                }
            }

            throw new RuntimeException("Unsupported type " + type);
        }

        protected final void setNull(String name) {
            final var offset = offset(name);
            assert Objects.equals(layout(name).withoutName(), JVM_HANDLE_LAYOUT) : name + ": " + layout(name);

            assert toObjectHandle(null) == 0;
            segment().set(JVM_HANDLE_LAYOUT, offset, 0);
        }

        private MemorySegment segment() {
            final var result = this.segment.get();
            if (result == null) throw new NullPointerException("Segment is null");
            return result;
        }

        private LibraryCall call() {
            final var result = this.call.get();
            if (result == null) throw new NullPointerException("LibraryCall is null");
            return result;
        }

        public abstract void serialize(T object);
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

        protected <T> MemorySegment serializeFlatObjects(List<? extends T> list, ObjectDescriptor<T> desc) {
            final var size = list.size();
            final var itemLayout = desc.layout;
            final var totalByteCount = itemLayout.scale(ValueLayout.JAVA_LONG.byteSize(), size);
            final var segment = arena.allocate(totalByteCount);
            segment.set(ValueLayout.JAVA_LONG, 0, size);
            for (int i = 0; i < size; i++) {
                final var slice = segment.asSlice(itemLayout.scale(ValueLayout.JAVA_LONG.byteSize(), i), itemLayout);
                desc.serialize(list.get(i), slice, this);
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

    private static final class TokenObjectDescriptor extends ObjectDescriptor<Token> {
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

    private static final class IterableObjectDescriptor extends ObjectDescriptor<Iterable<Object>> {
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

    private static final class LexerObjectDescriptor extends ObjectDescriptor<Lexer> {
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

    private static final class ParserObjectDescriptor extends ObjectDescriptor<Parser> {
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

    private static final class ParseResultObjectDescriptor extends ObjectDescriptor<ParseResult> {
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

    private static final class SyntaxNodeObjectDescriptor extends ObjectDescriptor<SyntaxNode> {
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

    private static final class TextSpanObjectDescriptor extends ObjectDescriptor<TextSpan> {
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

    private static final class DiagnosticObjectDescriptor extends ObjectDescriptor<Diagnostic> {
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

    private static final class ErrorCodeObjectDescriptor extends ObjectDescriptor<ErrorCode> {
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

    private static final class DiagnosticArgumentObjectDescriptor extends ObjectDescriptor<DiagnosticArgument> {
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

    private static final class DiagnosticArgument {
        final Object object;

        DiagnosticArgument(Object object) {
            this.object = object;
        }
    }

    private static final class LanguageServerObjectDescriptor extends ObjectDescriptor<LanguageServer> {
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

    private static final class SemanticModelObjectDescriptor extends ObjectDescriptor<SemanticModel> {
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

    private static final class SemanticSymbolObjectDescriptor extends ObjectDescriptor<SemanticSymbol> {
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
}
