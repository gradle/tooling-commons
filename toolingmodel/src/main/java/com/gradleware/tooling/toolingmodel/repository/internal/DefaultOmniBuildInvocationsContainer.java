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

package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSortedMap;
import com.gradleware.tooling.toolingmodel.OmniBuildInvocations;
import com.gradleware.tooling.toolingmodel.OmniBuildInvocationsContainer;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import com.gradleware.tooling.toolingmodel.Path;
import org.gradle.tooling.model.gradle.BuildInvocations;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * Default implementation of the {@link OmniBuildInvocationsContainer} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniBuildInvocationsContainer implements OmniBuildInvocationsContainer {

    private final ImmutableSortedMap<Path, OmniBuildInvocations> buildInvocationsPerProject;

    private DefaultOmniBuildInvocationsContainer(SortedMap<Path, OmniBuildInvocations> buildInvocationsPerProject) {
        this.buildInvocationsPerProject = ImmutableSortedMap.copyOfSorted(buildInvocationsPerProject);
    }

    @Override
    public Optional<OmniBuildInvocations> get(Path projectPath) {
        return Optional.fromNullable(this.buildInvocationsPerProject.get(projectPath));
    }

    @Override
    public ImmutableSortedMap<Path, OmniBuildInvocations> asMap() {
        return this.buildInvocationsPerProject;
    }

    public static OmniBuildInvocationsContainer from(Map<String, BuildInvocations> buildInvocationsPerProject) {
        ImmutableSortedMap.Builder<Path, OmniBuildInvocations> buildInvocationsMap = ImmutableSortedMap.orderedBy(Path.Comparator.INSTANCE);
        for (String projectPath : buildInvocationsPerProject.keySet()) {
            buildInvocationsMap.put(Path.from(projectPath), DefaultOmniBuildInvocations.from(buildInvocationsPerProject.get(projectPath), Path.from(projectPath)));
        }
        return new DefaultOmniBuildInvocationsContainer(buildInvocationsMap.build());
    }

    public static DefaultOmniBuildInvocationsContainer from(SortedMap<Path, OmniBuildInvocations> buildInvocationsPerProject) {
        return new DefaultOmniBuildInvocationsContainer(buildInvocationsPerProject);
    }

    public static OmniBuildInvocationsContainer from(OmniGradleProject gradleProject) {
        ImmutableSortedMap.Builder<Path, OmniBuildInvocations> result = ImmutableSortedMap.orderedBy(Path.Comparator.INSTANCE);
        collectBuildInvocations(gradleProject, result);
        return new DefaultOmniBuildInvocationsContainer(result.build());
    }

    private static void collectBuildInvocations(OmniGradleProject project, ImmutableSortedMap.Builder<Path, OmniBuildInvocations> result) {
        result.put(project.getPath(), DefaultOmniBuildInvocations.from(project.getProjectTasks(), project.getTaskSelectors()));

        List<OmniGradleProject> children = project.getChildren();
        for (OmniGradleProject child : children) {
            collectBuildInvocations(child, result);
        }
    }

}
