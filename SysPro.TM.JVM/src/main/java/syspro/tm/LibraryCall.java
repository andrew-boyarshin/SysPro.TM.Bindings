package syspro.tm;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.IdentityHashMap;
import java.util.List;

abstract class LibraryCall {
    final Arena arena;
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
            Library.fatalError(e);
        }
    }

    public abstract void call() throws Throwable;
}
