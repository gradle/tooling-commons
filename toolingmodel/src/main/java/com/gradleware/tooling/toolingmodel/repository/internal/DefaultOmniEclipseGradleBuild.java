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
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import org.gradle.tooling.model.eclipse.EclipseProject;

import java.util.Collection;
import java.util.Set;

/**
 * Default implementation of the {@link OmniEclipseGradleBuild} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniEclipseGradleBuild implements OmniEclipseGradleBuild {

    private final ImmutableSet<OmniEclipseProject> rootProjects;

    private DefaultOmniEclipseGradleBuild(Set<OmniEclipseProject> rootProjects) {
        this.rootProjects = ImmutableSet.copyOf(rootProjects);
    }

    @Override
    public Set<OmniEclipseProject> getRootProjects() {
        return this.rootProjects;
    }

    @Override
    public Set<OmniEclipseProject> getAllProjects() {
        ImmutableSet.Builder<OmniEclipseProject> result = ImmutableSet.builder();
        for (OmniEclipseProject rootProject : this.rootProjects) {
            result.addAll(rootProject.getAll());
        }
        return result.build();
    }

    public static DefaultOmniEclipseGradleBuild from(Collection<EclipseProject> rootProjects) {
        ImmutableSet.Builder<OmniEclipseProject> roots = ImmutableSet.builder();
        for (EclipseProject rootProject : rootProjects) {
            roots.add(DefaultOmniEclipseProject.from(rootProject));
        }
        return new DefaultOmniEclipseGradleBuild(roots.build());
    }
}
