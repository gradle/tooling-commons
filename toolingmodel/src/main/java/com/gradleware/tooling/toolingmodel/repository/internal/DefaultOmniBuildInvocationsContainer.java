package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.gradleware.tooling.toolingmodel.BuildInvocationFields;
import com.gradleware.tooling.toolingmodel.OmniBuildInvocations;
import com.gradleware.tooling.toolingmodel.OmniBuildInvocationsContainer;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import com.gradleware.tooling.toolingmodel.generic.Model;
import org.gradle.tooling.model.gradle.BuildInvocations;

import java.util.Map;
import java.util.SortedMap;

/**
 * Default implementation of the {@link OmniBuildInvocationsContainer} interface.
 */
public final class DefaultOmniBuildInvocationsContainer implements OmniBuildInvocationsContainer {

    private final ImmutableSortedMap<String, OmniBuildInvocations> buildInvocationsPerProject;

    private DefaultOmniBuildInvocationsContainer(SortedMap<String, OmniBuildInvocations> buildInvocationsPerProject) {
        this.buildInvocationsPerProject = ImmutableSortedMap.copyOfSorted(buildInvocationsPerProject);
    }

    @Override
    public Optional<OmniBuildInvocations> get(String projectPath) {
        return Optional.fromNullable(this.buildInvocationsPerProject.get(projectPath));
    }

    @Override
    public ImmutableSortedMap<String, OmniBuildInvocations> asMap() {
        return this.buildInvocationsPerProject;
    }

    public static OmniBuildInvocationsContainer from(Map<String, BuildInvocations> buildInvocationsPerProject) {
        BuildInvocationsContainer buildInvocationsContainer = BuildInvocationsContainer.from(buildInvocationsPerProject);
        return convert(buildInvocationsContainer);
    }

    private static OmniBuildInvocationsContainer convert(BuildInvocationsContainer buildInvocationsContainer) {
        ImmutableSortedMap.Builder<String, OmniBuildInvocations> result = ImmutableSortedMap.orderedBy(PathComparator.INSTANCE);
        Map<String, Model<BuildInvocationFields>> buildInvocationsPerProject = buildInvocationsContainer.asMap();
        for (String projectPath : buildInvocationsPerProject.keySet()) {
            Model<BuildInvocationFields> buildInvocations = buildInvocationsPerProject.get(projectPath);
            result.put(projectPath, DefaultOmniBuildInvocations.from(buildInvocations));
        }
        return new DefaultOmniBuildInvocationsContainer(result.build());
    }

    public static OmniBuildInvocationsContainer from(OmniGradleProject gradleProject) {
        ImmutableSortedMap.Builder<String, OmniBuildInvocations> result = ImmutableSortedMap.orderedBy(PathComparator.INSTANCE);
        collectBuildInvocations(gradleProject, result);
        return new DefaultOmniBuildInvocationsContainer(result.build());
    }

    private static void collectBuildInvocations(OmniGradleProject project, ImmutableSortedMap.Builder<String, OmniBuildInvocations> result) {
        result.put(project.getPath(), DefaultOmniBuildInvocations.from(project.getProjectTasks(), project.getTaskSelectors()));

        ImmutableList<OmniGradleProject> children = project.getChildren();
        for (OmniGradleProject child : children) {
            collectBuildInvocations(child, result);
        }
    }

}
