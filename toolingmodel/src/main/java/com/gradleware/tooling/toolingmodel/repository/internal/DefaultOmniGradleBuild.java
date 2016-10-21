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

import com.google.common.collect.ImmutableSet;
import com.gradleware.tooling.toolingmodel.OmniGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import org.gradle.tooling.model.GradleProject;

import java.util.Collection;
import java.util.Set;

/**
 * Default implementation of the {@link OmniGradleBuild} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniGradleBuild implements OmniGradleBuild {

    private final ImmutableSet<OmniGradleProject> rootProjects;

    private DefaultOmniGradleBuild(Set<OmniGradleProject> rootProjects) {
        this.rootProjects = ImmutableSet.copyOf(rootProjects);
    }

    @Override
    public Set<OmniGradleProject> getRootProjects() {
        return this.rootProjects;
    }

    @Override
    public Set<OmniGradleProject> getAllProjects() {
        ImmutableSet.Builder<OmniGradleProject> result = ImmutableSet.builder();
        for (OmniGradleProject rootProject : this.rootProjects) {
            result.addAll(rootProject.getAll());
        }
        return result.build();
    }

    public static DefaultOmniGradleBuild from(Collection<GradleProject> rootProjects) {
        ImmutableSet.Builder<OmniGradleProject> roots = ImmutableSet.builder();
        for (GradleProject rootProject : rootProjects) {
            roots.add(DefaultOmniGradleProject.from(rootProject));
        }
        return new DefaultOmniGradleBuild(roots.build());
    }
}
