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

package com.gradleware.tooling.toolingmodel;

import com.gradleware.tooling.toolingutils.ImmutableCollection;

import java.util.List;

/**
 * Provides detailed information about the Gradle build, suited for consumption in Eclipse.
 *
 * @author Etienne Studer
 * @see org.gradle.tooling.model.eclipse.EclipseProject
 */
public interface OmniEclipseGradleBuild extends BuildScopedModel {

    /**
     * Returns the root Eclipse project of the build.
     *
     * @return the root Eclipse project
     */
    OmniEclipseProject getRootEclipseProject();

    /**
     * Returns the included root Eclipse projects from the composite build.
     * <p>
     * If called on a non-composite project, the method returns an empty list.
     *
     * @return the included builds' Eclipse models
     */
    @ImmutableCollection
    List<OmniEclipseProject> getIncludedRootProjects();

    /**
     * Convenience method to get the root project and the included root projects in one collection.
     *
     * @return a list of all root projects
     */
    @ImmutableCollection
    List<OmniEclipseProject> getAllRootProjects();
}
