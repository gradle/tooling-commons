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
 * Provides information about the Gradle build structure.
 *
 * @author Etienne Studer
 * @see org.gradle.tooling.model.gradle.GradleBuild
 */
public interface OmniGradleBuildStructure extends BuildScopedModel {

    /**
     * Returns the root project of the build.
     *
     * @return the root project
     */
    OmniGradleProjectStructure getRootProject();

    /**
     * Returns the root projects of the included builds.
     * <p>
     * If called on a non-composite project, the method returns an empty list.
     *
     * @return the included builds' root projects
     */
    @ImmutableCollection
    List<OmniGradleProjectStructure> getIncludedRootProjects();

}
