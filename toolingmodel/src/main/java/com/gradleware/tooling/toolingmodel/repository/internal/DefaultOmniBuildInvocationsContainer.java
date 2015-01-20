package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.gradleware.tooling.toolingmodel.BuildInvocationFields;
import com.gradleware.tooling.toolingmodel.OmniBuildInvocations;
import com.gradleware.tooling.toolingmodel.OmniBuildInvocationsContainer;
import com.gradleware.tooling.toolingmodel.generic.Model;
import org.gradle.tooling.model.gradle.BuildInvocations;

import java.util.Map;

/**
 * Default implementation of the {@link OmniBuildInvocationsContainer} interface.
 */
public final class DefaultOmniBuildInvocationsContainer implements OmniBuildInvocationsContainer {

    private final ImmutableMap<String, OmniBuildInvocations> buildInvocationsPerProject;

    private DefaultOmniBuildInvocationsContainer(Map<String, OmniBuildInvocations> buildInvocationsPerProject) {
        this.buildInvocationsPerProject = ImmutableMap.copyOf(buildInvocationsPerProject);
    }

    @Override
    public Optional<OmniBuildInvocations> get(String projectPath) {
        return Optional.fromNullable(this.buildInvocationsPerProject.get(projectPath));
    }

    @Override
    public ImmutableMap<String, OmniBuildInvocations> asMap() {
        return this.buildInvocationsPerProject;
    }

    public static OmniBuildInvocationsContainer from(Map<String, BuildInvocations> buildInvocationsPerProject) {
        BuildInvocationsContainer buildInvocationsContainer = BuildInvocationsContainer.from(buildInvocationsPerProject);
        return convert(buildInvocationsContainer);
    }

    private static OmniBuildInvocationsContainer convert(BuildInvocationsContainer buildInvocationsContainer) {
        ImmutableMap.Builder<String, OmniBuildInvocations> result = ImmutableMap.builder();
        Map<String, Model<BuildInvocationFields>> buildInvocationsPerProject = buildInvocationsContainer.asMap();
        for (String projectPath : buildInvocationsPerProject.keySet()) {
            Model<BuildInvocationFields> buildInvocations = buildInvocationsPerProject.get(projectPath);
            result.put(projectPath, DefaultOmniBuildInvocations.from(buildInvocations));
        }
        return new DefaultOmniBuildInvocationsContainer(result.build());
    }

}
