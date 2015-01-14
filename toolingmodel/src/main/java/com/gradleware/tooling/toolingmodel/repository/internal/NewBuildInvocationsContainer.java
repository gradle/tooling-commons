package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.collect.ImmutableMap;
import com.gradleware.tooling.toolingmodel.BuildInvocationFields;
import com.gradleware.tooling.toolingmodel.generic.Model;

import java.util.Map;

/**
 * Holds the {@code Model&lt;BuildInvocationFields&gt;} for a given set of projects. Each project is identified by its unique full path.
 *
 * The primary advantage of this container is that it allows to work with a generics-free type compared to <code>Map&lt;String, Model&lt;BuildInvocationFields&gt;&gt;</code>.
 */
public final class NewBuildInvocationsContainer {

    private final Map<String, Model<BuildInvocationFields>> buildInvocationsPerProject;

    private NewBuildInvocationsContainer(Map<String, Model<BuildInvocationFields>> buildInvocationsPerProject) {
        this.buildInvocationsPerProject = ImmutableMap.copyOf(buildInvocationsPerProject);
    }

    /**
     * A {@code Map} of {@code Model&lt;BuildInvocationFields&gt;} per project, where each project is identified by its unique full path.
     *
     * @return the mapping of projects to build invocations
     */
    public Map<String, Model<BuildInvocationFields>> asMap() {
        return this.buildInvocationsPerProject;
    }

    /**
     * Create a new instance from the given mapping of projects to build invocations.
     *
     * @param buildInvocationsPerProject the mapping of projects to build invocations
     * @return the new instance
     */
    public static NewBuildInvocationsContainer from(Map<String, Model<BuildInvocationFields>> buildInvocationsPerProject) {
        return new NewBuildInvocationsContainer(buildInvocationsPerProject);
    }

}
