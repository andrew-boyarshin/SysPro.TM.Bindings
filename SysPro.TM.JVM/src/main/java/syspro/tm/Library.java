package syspro.tm;

import syspro.tm.lexer.*;
import syspro.tm.parser.Parser;

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
    private static volatile boolean hasFatalFailures;
    private static volatile Path nativeLibraryLockFile;

    static {
        final var libraryFile = extractNativeLibrary();
        lookup = SymbolLookup.libraryLookup(libraryFile, arena);
    }

    private Library() {
    }

    private static volatile MethodHandle registerTask1Solution;

    private static MethodHandle registerTask1Solution() {
        final var handle = Library.registerTask1Solution;
        if (handle == null) {
            final var fn = findFunction("RegisterTask1Solution");
            final var desc = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
            return Library.registerTask1Solution = linker.downcallHandle(fn, desc);
        }

        return handle;
    }

    private static volatile MethodHandle registerTask1LexResult;

    private static MethodHandle registerTask1LexResult() {
        final var handle = Library.registerTask1LexResult;
        if (handle == null) {
            final var fn = findFunction("RegisterTask1LexResult");
            final var desc = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS);
            return Library.registerTask1LexResult = linker.downcallHandle(fn, desc);
        }

        return handle;
    }

    private static volatile MemorySegment lexImpl;

    private static MemorySegment lexImpl() {
        final var lexImpl = Library.lexImpl;
        if (lexImpl == null) {
            try {
                final var handle = MethodHandles.lookup().findStatic(
                                Library.class, "lexImpl",
                                MethodType.methodType(long.class, long.class, MemorySegment.class)
                );

                final var desc = FunctionDescriptor.of(
                        ValueLayout.JAVA_LONG,
                        ValueLayout.JAVA_LONG,
                        ValueLayout.ADDRESS
                );

                return Library.lexImpl = linker.upcallStub(handle, desc, arena);

            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return lexImpl;
    }

    private static final IdentityHashMap<Object, Integer> objectHandles = new IdentityHashMap<>();
    private static final ArrayList<Object> objectReferences = new ArrayList<>();

    private static int toObjectHandle(Object object) {
        synchronized (objectHandles) {
            final var handle = objectHandles.get(object);
            if (handle == null) {
                final var newHandle = objectReferences.size();
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

    public static void registerTask1Solution(Lexer impl, TestMode mode) {
        try (final var arena = Arena.ofConfined()) {
            final var nativeStruct = arena.allocate(MemoryLayout.structLayout(ValueLayout.ADDRESS, ValueLayout.ADDRESS));

            // TODO: real layout is Address, value is Integer, assignment is Long.
            nativeStruct.set(ValueLayout.JAVA_LONG, 0, toObjectHandle(impl));
            nativeStruct.set(ValueLayout.ADDRESS, ValueLayout.ADDRESS.byteSize(), lexImpl());

            new LibraryCall(arena) {

                @Override
                public void call() throws Throwable {
                    registerTask1Solution().invokeExact(nativeStruct, mode.toMask());
                }
            }.makeCall();
        } finally {
            if (hasFatalFailures) {
                System.err.println("Fatal errors occurred.");
                System.exit(1);
            }
        }
    }

    private static MemorySegment findFunction(String name) {
        return lookup.find(name).orElseThrow(() -> new RuntimeException('`' + name + "` was not found in SysPro.TM.Library, faulty build? Ask the teacher."));
    }

    private static long lexImpl(long impl, MemorySegment codeSegment) {
        final Lexer lexer = fromObjectHandle(Math.toIntExact(impl));
        final var code = codeSegment.reinterpret(Integer.MAX_VALUE).getString(0);
        try (final var arena = Arena.ofConfined()) {
            final var result = new long[1];
            new LibraryCall(arena) {

                @Override
                public void call() throws Throwable {
                    final var lexResult = serialize(this, lexer.lex(code));
                    result[0] = ((MemorySegment)registerTask1LexResult().invokeExact(lexResult)).address();
                }
            }.makeCall();
            return result[0];
        }
    }

    public static void registerTask2Solution(Parser impl) {
        try (final var arena = Arena.ofConfined()) {
            System.err.println("Task 2 checking is not yet allowed");
            hasFatalFailures = true;
        } finally {
            if (hasFatalFailures) {
                System.err.println("Fatal errors occurred.");
                System.exit(1);
            }
        }
    }

    private static MemorySegment serialize(LibraryCall call, List<Token> tokenList) {
        final var nativeDesc = MemoryLayout.structLayout(
                ValueLayout.JAVA_INT,  // kind
                ValueLayout.JAVA_INT,  // start
                ValueLayout.JAVA_INT,  // end
                ValueLayout.JAVA_INT,  // leadingTriviaLength
                ValueLayout.JAVA_INT,  // trailingTriviaLength
                ValueLayout.JAVA_INT,  // type
                ValueLayout.ADDRESS,   // value1
                ValueLayout.JAVA_LONG, // value2
                ValueLayout.JAVA_LONG  // value3
        );
        final var kind = ValueLayout.JAVA_INT.scale(0, 0);
        final var start = ValueLayout.JAVA_INT.scale(0, 1);
        final var end = ValueLayout.JAVA_INT.scale(0, 2);
        final var leadingTriviaLength = ValueLayout.JAVA_INT.scale(0, 3);
        final var trailingTriviaLength = ValueLayout.JAVA_INT.scale(0, 4);
        final var type = ValueLayout.JAVA_INT.scale(0, 5);
        final var value1 = ValueLayout.JAVA_INT.scale(0, 6);
        final var value2 = ValueLayout.JAVA_LONG.scale(value1 + ValueLayout.ADDRESS.byteSize(), 0);
        final var value3 = ValueLayout.JAVA_LONG.scale(value1 + ValueLayout.ADDRESS.byteSize(), 1);
        return new ListSerializer<Token>(nativeDesc) {

            @Override
            public void serializeItem(Token token, MemorySegment segment1) {
                segment1.set(ValueLayout.JAVA_INT, kind, tokenKind(token));
                segment1.set(ValueLayout.JAVA_INT, start, token.start);
                segment1.set(ValueLayout.JAVA_INT, end, token.end);
                segment1.set(ValueLayout.JAVA_INT, leadingTriviaLength, token.leadingTriviaLength);
                segment1.set(ValueLayout.JAVA_INT, trailingTriviaLength, token.trailingTriviaLength);
                switch (token) {
                    case IdentifierToken identifierToken:
                        segment1.set(ValueLayout.ADDRESS, value1, call.serializeString(identifierToken.value));
                        segment1.set(ValueLayout.JAVA_LONG, value3, identifierToken.contextualKeyword != null ? tokenKind(identifierToken.contextualKeyword) : 0);
                        break;
                    case BadToken _, IndentationToken _, KeywordToken _, SymbolToken _:
                        break;
                    case BooleanLiteralToken literalToken:
                        segment1.set(ValueLayout.JAVA_INT, type, literalToken.type.ordinal());
                        segment1.set(ValueLayout.JAVA_LONG, value2, literalToken.value ? 1 : 0);
                        break;
                    case IntegerLiteralToken literalToken:
                        segment1.set(ValueLayout.JAVA_INT, type, literalToken.type.ordinal());
                        segment1.set(ValueLayout.JAVA_LONG, value2, literalToken.value);
                        segment1.set(ValueLayout.JAVA_LONG, value3, literalToken.hasTypeSuffix ? 1 : 0);
                        break;
                    case RuneLiteralToken literalToken:
                        segment1.set(ValueLayout.JAVA_INT, type, literalToken.type.ordinal());
                        segment1.set(ValueLayout.JAVA_LONG, value3, literalToken.value);
                        break;
                    case StringLiteralToken literalToken:
                        segment1.set(ValueLayout.JAVA_INT, type, literalToken.type.ordinal());
                        segment1.set(ValueLayout.ADDRESS, value1, call.serializeString(literalToken.value));
                        break;
                }
            }
        }.serializeList(tokenList);
    }

    private static abstract class LibraryCall {
        private final Arena arena;
        private final IdentityHashMap<String, MemorySegment> strings = new IdentityHashMap<>();

        protected MemorySegment serializeString(String string) {
            final var interned = string.intern();
            var segment = strings.get(interned);
            if (segment == null) {
                segment = arena.allocateFrom(string);
                strings.put(interned, segment);
            }
            return segment;
        }

        public LibraryCall(Arena arena) {
            this.arena = arena;
        }

        public final void makeCall() {
            try {
                call();
            } catch (Throwable e) {
                e.printStackTrace();
                hasFatalFailures = true;
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
