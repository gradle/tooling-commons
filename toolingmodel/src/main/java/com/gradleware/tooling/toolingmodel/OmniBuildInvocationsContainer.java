package com.gradleware.tooling.toolingmodel;

import com.google.common.base.Optional;
import com.gradleware.tooling.utils.ImmutableCollection;

import java.util.SortedMap;

/**
 * Holds the {@link OmniBuildInvocations} for a given set of projects. Each project is identified by its unique full path.
 *
 * The primary advantage of this container is that it allows to work with a generics-free type compared to <code>Map&lt;String, OmniBuildInvocations&gt;</code>.
 */
public interface OmniBuildInvocationsContainer {

    /**
     * Returns the {@code OmniBuildInvocations} for the given project, where the project is identified by its unique full path.
     *
     * @param projectPath the full path of the project for which to get the build invocations
     * @return the build invocations, if present
     */
    Optional<OmniBuildInvocations> get(Path projectPath);

    /**
     * A {@code Map} of {@code OmniBuildInvocations} per project, where each project is identified by its unique full path.
     *
     * @return the mapping of projects to build invocations
     */
    @ImmutableCollection
    SortedMap<Path, OmniBuildInvocations> asMap();

}
