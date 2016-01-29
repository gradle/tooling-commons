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

package com.gradleware.tooling.toolingmodel.repository.internal;

import java.util.List;
import java.util.Set;

import org.gradle.api.specs.Spec;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniEclipseWorkspace;

/**
 * Internal implementation of {@link OmniEclipseWorkspace}.
 *
 * @author Stefan Oehme
 */
public final class DefaultOmniEclipseWorkspace implements OmniEclipseWorkspace {

    private final ImmutableList<OmniEclipseProject> eclipseProjects;

    private DefaultOmniEclipseWorkspace(List<OmniEclipseProject> eclipseProjects) {
        this.eclipseProjects = ImmutableList.copyOf(eclipseProjects);
    }

    @Override
    public List<OmniEclipseProject> getOpenEclipseProjects() {
        return this.eclipseProjects;
    }

    @Override
    public Optional<OmniEclipseProject> tryFind(Spec<? super OmniEclipseProject> predicate) {
        return Iterables.tryFind(this.eclipseProjects, toPredicate(predicate));
    }

    @Override
    public List<OmniEclipseProject> filter(Spec<? super OmniEclipseProject> predicate) {
        return FluentIterable.from(this.eclipseProjects).filter(toPredicate(predicate)).toList();
    }

    private <T> Predicate<? super T> toPredicate(final Spec<? super T> spec) {
        return new Predicate<T>() {
            @Override
            public boolean apply(T input) {
                return spec.isSatisfiedBy(input);
            }
        };
    }

    public static OmniEclipseWorkspace from(Set<EclipseProject> eclipseProjects, boolean enforceAllTasksPublic) {
        List<OmniEclipseProject> omniEclipseProjects = Lists.newArrayList();
        for (EclipseProject eclipseProject : eclipseProjects) {
            omniEclipseProjects.add(DefaultOmniEclipseProject.from(eclipseProject, enforceAllTasksPublic));
        }
        return new DefaultOmniEclipseWorkspace(omniEclipseProjects);
    }

}
