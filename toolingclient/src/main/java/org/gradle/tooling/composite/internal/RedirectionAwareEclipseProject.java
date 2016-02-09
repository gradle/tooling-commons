/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.tooling.composite.internal;

import java.util.Set;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.eclipse.EclipseProjectDependency;
import org.gradle.tooling.model.internal.ImmutableDomainObjectSet;

import com.google.common.collect.Sets;

/**
 * Presents a consistent project hierarchy even when projects are renamed or dependency-substituted.
 * @author Stefan Oehme
 */
public class RedirectionAwareEclipseProject extends DelegatingEclipseProject {
    private final RedirectedProjectLookup redirectedProjectLookup;

    public RedirectionAwareEclipseProject(EclipseProject original, RedirectedProjectLookup redirectedProjectLookup) {
        super(original);
        this.redirectedProjectLookup = redirectedProjectLookup;
    }

    @Override
    public DomainObjectSet<? extends EclipseProject> getChildren() {
        Set<? extends EclipseProject> children = super.getChildren();
        Set<EclipseProject> rendirectedChildren = Sets.newHashSet();
        for (EclipseProject child : children) {
            rendirectedChildren.add(this.redirectedProjectLookup.getRedirectedProject(child));
        }
        return ImmutableDomainObjectSet.of(rendirectedChildren);
    }

    @Override
    public EclipseProject getParent() {
        return this.redirectedProjectLookup.getRedirectedProject(super.getParent());
    }

    @Override
    public DomainObjectSet<? extends EclipseProjectDependency> getProjectDependencies() {
        Set<? extends EclipseProjectDependency> projectDependencies = super.getProjectDependencies();
        Set<EclipseProjectDependency> redirectedDependencies = Sets.newHashSet();
        for (EclipseProjectDependency projectDependency : projectDependencies) {
            redirectedDependencies.add(new RedirectionAwareEclipseProjectDependency(projectDependency, this.redirectedProjectLookup));
        }
        return ImmutableDomainObjectSet.of(redirectedDependencies);
    }

}
