package com.gradleware.tooling.domain.repository;

import com.google.common.collect.ImmutableMap;
import org.gradle.tooling.model.gradle.BuildInvocations;

import java.util.Map;

/**
 * Holds the {@link BuildInvocations} for a given set of projects. Each project is identified by its unique full path.
 * <p>
 * The primary advantage of this container is that it allows to work with a generics-free type compared to <code>Map&lt;String, BuildInvocations&gt;</code>.
 */
public final class BuildInvocationsContainer {

    private final Map<String, BuildInvocations> buildInvocationsPerProject;

    private BuildInvocationsContainer(Map<String, BuildInvocations> buildInvocationsPerProject) {
        this.buildInvocationsPerProject = ImmutableMap.copyOf(buildInvocationsPerProject);
    }

    /**
     * A {@code Map} of {@code BuildInvocations} per project, where each project is identified by its unique full path.
     *
     * @return the mapping of projects to build invocations
     */
    public Map<String, BuildInvocations> asMap() {
        return this.buildInvocationsPerProject;
    }

    /**
     * Create a new instance from the given mapping of projects to build invocations.
     *
     * @param buildInvocationsPerProject the mapping of projects to build invocations
     * @return the new instance
     */
    public static BuildInvocationsContainer from(Map<String, BuildInvocations> buildInvocationsPerProject) {
        return new BuildInvocationsContainer(buildInvocationsPerProject);
    }

}
