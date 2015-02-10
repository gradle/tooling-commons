package com.gradleware.tooling.toolingmodel;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.gradleware.tooling.toolingmodel.repository.internal.PathComparator;

/**
 * Represents a path in Gradle. The path can point to a project, task, etc.
 */
public final class Path implements Comparable<Path> {

    private final String path;

    private Path(String path) {
        this.path = Preconditions.checkNotNull(path);
    }

    public String getPath() {
        return this.path;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Path other) {
        Preconditions.checkNotNull(other);
        return PathComparator.INSTANCE.compare(this.path, other.path);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Path that = (Path) other;
        return Objects.equal(this.path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.path);
    }

    public static Path from(String path) {
        return new Path(path);
    }

    public static final class Comparator implements java.util.Comparator<Path> {

        public static final Comparator INSTANCE = new Comparator();

        private Comparator() {
        }

        @Override
        public int compare(Path o1, Path o2) {
            return o1.compareTo(o2);
        }

    }

}
