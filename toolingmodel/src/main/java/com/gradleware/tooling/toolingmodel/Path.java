/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gradleware.tooling.toolingmodel;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.gradleware.tooling.toolingmodel.repository.internal.PathComparator;

/**
 * Represents a path in Gradle. The path can point to a project, task, etc.
 *
 * @author Etienne Studer
 */
public final class Path implements Comparable<Path> {

    private static final String PATH_SEPARATOR = ":";
    private static final Path ROOT_PATH = new Path(PATH_SEPARATOR);

    private final String path;

    private Path(String path) {
        this.path = Preconditions.checkNotNull(path);
        Preconditions.checkArgument(path.startsWith(PATH_SEPARATOR));
    }

    public String getPath() {
        return this.path;
    }

    /**
     * Returns a copy of this path with the last segment removed. If this path points to the root,
     * then the root path is returned.
     *
     * @return the new path
     */
    public Path dropLastSegment() {
        int lastPathChar = this.path.lastIndexOf(PATH_SEPARATOR);
        return lastPathChar <= 0 ? ROOT_PATH : new Path(this.path.substring(0, lastPathChar));
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

    /**
     * Singleton comparator to compare {@code Path} instances.
     */
    public enum Comparator implements java.util.Comparator<Path> {

        INSTANCE;

        @Override
        public int compare(Path o1, Path o2) {
            return o1.compareTo(o2);
        }

    }

}
