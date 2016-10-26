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
import com.google.common.collect.ImmutableSet;
import com.gradleware.tooling.toolingmodel.OmniGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniGradleProjectStructure;
import org.gradle.tooling.model.gradle.GradleBuild;

import java.util.Set;

/**
 * Default implementation of the {@link OmniGradleBuild} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniGradleBuild implements OmniGradleBuild {

    private final OmniGradleProjectStructure rootProject;
    private final ImmutableSet<OmniGradleBuild> includedBuilds;

    private DefaultOmniGradleBuild(OmniGradleProjectStructure rootProject, ImmutableSet<OmniGradleBuild> includedBuilds) {
        this.rootProject = Preconditions.checkNotNull(rootProject);
        this.includedBuilds = ImmutableSet.copyOf(includedBuilds);
    }

    @Override
    public OmniGradleProjectStructure getRootProject() {
        return this.rootProject;
    }

    @Override
    public Set<OmniGradleBuild> getIncludedBuilds() {
        return this.includedBuilds;
    }

    public static DefaultOmniGradleBuild from(GradleBuild gradleBuild) {
        DefaultOmniGradleProjectStructure rootProject = DefaultOmniGradleProjectStructure.from(gradleBuild.getRootProject());
        ImmutableSet.Builder<OmniGradleBuild> includedBuilds = ImmutableSet.builder();
        for (GradleBuild includedBuild : gradleBuild.getIncludedBuilds()) {
            includedBuilds.add(from(includedBuild));
        }
        return new DefaultOmniGradleBuild(rootProject, includedBuilds.build());
    }
}
