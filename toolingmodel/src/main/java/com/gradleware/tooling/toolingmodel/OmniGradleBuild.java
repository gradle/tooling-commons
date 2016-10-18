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
 * Provides detailed information about the Gradle build.
 *
 * @author Etienne Studer
 * @see org.gradle.tooling.model.GradleProject
 */
public interface OmniGradleBuild extends BuildScopedModel {

    /**
     * Returns the root project of the build.
     *
     * @return the root project
     */
    OmniGradleProject getRootProject();

    /**
     * Returns the included build roots from the composite.
     * <p>
     * If called on a non-composite project, the method returns an empty list.
     *
     * @return the included builds' root projects
     */
    @ImmutableCollection
    List<OmniGradleProject> getIncludedRootProjects();

}
