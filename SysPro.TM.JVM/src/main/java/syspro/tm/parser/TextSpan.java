package syspro.tm.parser;

import java.util.Objects;

public final class TextSpan implements Comparable<TextSpan> {
    public final int start;
    public final int length;

    public TextSpan(int start, int length) {
        if (start < 0) {
            throw new IllegalArgumentException("start");
        }
        if (start + length < start) {
            throw new IllegalArgumentException("length");
        }
        this.start = start;
        this.length = length;
    }

    public static TextSpan fromBounds(int start, int end) {
        return new TextSpan(start, end - start);
    }

    public int start() {
        return start;
    }

    public int length() {
        return length;
    }

    public int end() {
        return start + length;
    }

    public boolean isEmpty() {
        return length == 0;
    }

    public boolean contains(int position) {
        return start <= position && position < end();
    }

    public boolean contains(TextSpan span) {
        return this.start <= span.start && span.end() <= this.end();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof TextSpan other && start == other.start && length == other.length;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, length);
    }

    @Override
    public String toString() {
        return "[" + start + ", " + end() + ')';
    }

    @Override
    public int compareTo(TextSpan span) {
        final var diff = this.start - span.start;
        if (diff != 0) {
            return diff;
        }
        return this.length - span.length;
    }
}
