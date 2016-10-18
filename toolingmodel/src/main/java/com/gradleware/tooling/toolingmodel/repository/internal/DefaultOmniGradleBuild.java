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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.gradleware.tooling.toolingmodel.OmniGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import org.gradle.tooling.model.GradleProject;

import java.util.Collections;
import java.util.List;

/**
 * Default implementation of the {@link OmniGradleBuild} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniGradleBuild implements OmniGradleBuild {

    private final OmniGradleProject rootProject;
    private final ImmutableList<OmniGradleProject> includedRootProjects;

    private DefaultOmniGradleBuild(OmniGradleProject rootProject, List<OmniGradleProject> includedRootProjects) {
        this.rootProject = rootProject;
        this.includedRootProjects = ImmutableList.copyOf(includedRootProjects);
    }

    @Override
    public OmniGradleProject getRootProject() {
        return this.rootProject;
    }

    public List<OmniGradleProject> getIncludedRootProjects() {
        return this.includedRootProjects;
    }

    public static DefaultOmniGradleBuild from(GradleProject gradleRootProject) {
        Preconditions.checkState(gradleRootProject.getParent() == null, "Provided Gradle project is not the root project.");
        return new DefaultOmniGradleBuild(DefaultOmniGradleProject.from(gradleRootProject), Collections.<OmniGradleProject>emptyList());
    }

    public static DefaultOmniGradleBuild from(GradleProject gradleRootProject, List<GradleProject> includedRootProjects) {
        Preconditions.checkState(gradleRootProject.getParent() == null, "Provided Gradle project is not the root project.");
        DefaultOmniGradleProject root = DefaultOmniGradleProject.from(gradleRootProject);
        List<OmniGradleProject> included = Lists.newArrayList();
        for (GradleProject includedRootProject : includedRootProjects) {
            Preconditions.checkState(includedRootProject.getParent() == null, "Included Gradle project is not a root project.");
            included.add(DefaultOmniGradleProject.from(includedRootProject));
        }
        return new DefaultOmniGradleBuild(root, included);
    }
}
