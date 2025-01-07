package syspro.tm;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.List;
import java.util.Objects;

abstract class ObjectDescriptor<T> {
    private final ThreadLocal<LibraryCall> call = ThreadLocal.withInitial(() -> null);
    private final ThreadLocal<MemorySegment> segment = ThreadLocal.withInitial(() -> null);
    final MemoryLayout layout;
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
        set(name, new boolean[]{flag});
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
        assert Objects.equals(layout(name).withoutName(), Library.STRING_LAYOUT) : name + ": " + layout(name);
        segment().set(Library.STRING_LAYOUT, offset(name), call().serializeString(value));
    }

    protected final void set(String name, MemorySegment stub) {
        assert Objects.equals(layout(name).withoutName(), Library.VMT_STUB_LAYOUT) : name + ": " + layout(name);
        segment().set(Library.VMT_STUB_LAYOUT, offset(name), stub);
    }

    protected final void setArray(String name, List<?> items) {
        final var offset = offset(name);
        assert Objects.equals(layout(name).withoutName(), ValueLayout.ADDRESS) : name + ": " + layout(name);

        if (items == null) {
            assert Library.toObjectHandle(null) == 0;
            segment().set(ValueLayout.ADDRESS, offset, MemorySegment.NULL);
            return;
        }

        final var size = items.size();
        final var segment = call().arena.allocate(Library.JVM_HANDLE_LAYOUT, size);
        for (var i = 0; i < size; i++) {
            segment.setAtIndex(Library.JVM_HANDLE_LAYOUT, i, Library.toObjectHandle(items.get(i)));
        }
        segment().set(ValueLayout.ADDRESS, offset, segment);
    }

    protected final void set(String name, Object value) {
        final var offset = offset(name);
        assert Objects.equals(layout(name).withoutName(), Library.JVM_HANDLE_LAYOUT) : name + ": " + layout(name);

        if (value == null) {
            assert Library.toObjectHandle(null) == 0;
            segment().set(Library.JVM_HANDLE_LAYOUT, offset, 0);
            return;
        }

        if (value instanceof String || value instanceof MemorySegment || value instanceof Enum<?>) {
            throw new RuntimeException("Wrong overload");
        }

        final var type = value.getClass();
        for (final var descriptor : Library.layouts) {
            if (descriptor.supports(type)) {
                segment().set(Library.JVM_HANDLE_LAYOUT, offset, Library.toObjectHandle(value));
                return;
            }
        }

        throw new RuntimeException("Unsupported type " + type);
    }

    protected final void setNull(String name) {
        final var offset = offset(name);
        assert Objects.equals(layout(name).withoutName(), Library.JVM_HANDLE_LAYOUT) : name + ": " + layout(name);

        assert Library.toObjectHandle(null) == 0;
        segment().set(Library.JVM_HANDLE_LAYOUT, offset, 0);
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
