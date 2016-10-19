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

import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure;
import com.gradleware.tooling.toolingmodel.OmniGradleProjectStructure;
import org.gradle.tooling.model.gradle.GradleBuild;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of the {@link OmniGradleBuildStructure} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniGradleBuildStructure implements OmniGradleBuildStructure {

    private final OmniGradleProjectStructure rootProject;

    private final ImmutableList<OmniGradleProjectStructure> includedRootProjects;

    private DefaultOmniGradleBuildStructure(OmniGradleProjectStructure rootProject, List<OmniGradleProjectStructure> includedRootProjects) {
        this.rootProject = rootProject;
        this.includedRootProjects = ImmutableList.copyOf(includedRootProjects);
    }

    @Override
    public OmniGradleProjectStructure getRootProject() {
        return this.rootProject;
    }

    @Override
    public List<OmniGradleProjectStructure> getIncludedRootProjects() {
        return this.includedRootProjects;
    }

    @Override
    public List<OmniGradleProjectStructure> getAllRootProjects() {
        ImmutableList.Builder<OmniGradleProjectStructure> result = ImmutableList.builder();
        result.add(this.rootProject);
        result.addAll(this.includedRootProjects);
        return result.build();
    }

    public static DefaultOmniGradleBuildStructure from(GradleBuild gradleBuild) {
        DefaultOmniGradleProjectStructure root = DefaultOmniGradleProjectStructure.from(gradleBuild.getRootProject());
        List<OmniGradleProjectStructure> includedRoots = new ArrayList<OmniGradleProjectStructure>();
        for (GradleBuild included : gradleBuild.getIncludedBuilds()) {
            includedRoots.add(DefaultOmniGradleProjectStructure.from(included.getRootProject()));
        }
        return new DefaultOmniGradleBuildStructure(root, includedRoots);
    }

}
