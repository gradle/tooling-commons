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

package com.gradleware.tooling.toolingmodel;

import java.util.List;

import org.gradle.api.specs.Spec;

import com.google.common.base.Optional;

import com.gradleware.tooling.toolingutils.ImmutableCollection;

/**
 * The workspace consists of all the {@link OmniEclipseProject}s in a composition of one or more
 * Gradle builds.
 *
 * @author Stefan Oehme
 */
// TODO add description of de-duplication and dependency substitution as soon as it is implemented
public interface OmniEclipseWorkspace extends OmniCompositeModel<OmniEclipseProject> {

    /**
     * Searches the open projects for a project matching the given specification.
     *
     * @param predicate the criteria to match
     * @return the matching project, if any
     */
    Optional<OmniEclipseProject> tryFind(Spec<? super OmniEclipseProject> predicate);

    /**
     * Returns a view of all open projects that match the given specification.
     *
     * @param predicate the criteria to match
     * @return the matching projects
     */
    @ImmutableCollection
    List<OmniEclipseProject> filter(Spec<? super OmniEclipseProject> predicate);
}
