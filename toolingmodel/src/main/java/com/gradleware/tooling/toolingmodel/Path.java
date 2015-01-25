package com.gradleware.tooling.toolingmodel;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Represents a path in Gradle. The path can point to a project, task, etc.
 */
public final class Path {

    private final String path;

    private Path(String path) {
        this.path = Preconditions.checkNotNull(path);
    }

    public String getPath() {
        return this.path;
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

}
