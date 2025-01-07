package syspro.tm;

import syspro.tm.lexer.Keyword;
import syspro.tm.lexer.Lexer;
import syspro.tm.lexer.Symbol;
import syspro.tm.lexer.TestMode;
import syspro.tm.parser.AnySyntaxKind;
import syspro.tm.parser.Parser;
import syspro.tm.parser.SyntaxKind;
import syspro.tm.symbols.LanguageServer;
import syspro.tm.symbols.SymbolKind;

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

    static final Linker linker = Linker.nativeLinker();
    static final Arena callStubArena = Arena.global();
    private static final SymbolLookup lookup;
    private static final String NATIVE_LIBRARY_TEMP_DIRECTORY_PREFIX = "SysPro.TM.JVM";
    private static final String LOCK_FILE_PREFIX = "lock-";
    private static final IdentityHashMap<Object, Long> objectHandles = new IdentityHashMap<>();
    private static final ArrayList<Object> objectReferences = new ArrayList<>();
    static final ValueLayout.OfLong JVM_HANDLE_LAYOUT = ValueLayout.JAVA_LONG;
    private static final ValueLayout.OfLong LIB_HANDLE_LAYOUT = ValueLayout.JAVA_LONG;
    static final AddressLayout VMT_STUB_LAYOUT = ValueLayout.ADDRESS;
    static final AddressLayout ARRAY_LAYOUT = ValueLayout.ADDRESS;
    static final AddressLayout STRING_LAYOUT = ValueLayout.ADDRESS;
    static final Class<Long> JVM_HANDLE_CLASS = long.class;
    private static final Class<Long> LIB_HANDLE_CLASS = long.class;
    static final Class<MemorySegment> ARRAY_CLASS = MemorySegment.class;
    static final Class<MemorySegment> STRING_CLASS = MemorySegment.class;
    static final List<ObjectDescriptor<?>> layouts = List.of(
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

    static long toObjectHandle(Object object) {
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

    static <T> T fromObjectHandle(long handle) {
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

    static <T> T[] readHandleArray(int count, MemorySegment handleArraySegment) {
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

    static void fatalError(Throwable e) {
        e.printStackTrace();
        hasFatalFailures = true;
    }

    static int tokenKind(Keyword keyword) {
        return 100 + keyword.ordinal();
    }

    static int tokenKind(Symbol symbol) {
        return 200 + symbol.ordinal();
    }

    private static int syntaxKind(SyntaxKind kind) {
        final var ordinal = kind.ordinal();
        if (ordinal < SyntaxKind.SOURCE_TEXT.ordinal()) {
            return ordinal;
        }
        return 2000 + ordinal - SyntaxKind.SOURCE_TEXT.ordinal();
    }

    static int syntaxKind(AnySyntaxKind kind) {
        return switch (kind) {
            case Keyword keyword -> tokenKind(keyword);
            case Symbol symbol -> tokenKind(symbol);
            case SyntaxKind nonTerminalKind -> syntaxKind(nonTerminalKind);
            default -> throw new IllegalStateException("Unexpected value: " + kind + ". You can only return Keyword, Symbol or SyntaxKind from SyntaxNode.kind().");
        };
    }

    static int symbolKind(SymbolKind kind) {
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
}
