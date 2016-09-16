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

package com.gradleware.tooling.toolingmodel.repository;

import com.gradleware.tooling.toolingmodel.*;
import org.gradle.tooling.connection.ModelResults;

/**
 * Repository for Gradle build models sourced from a Gradle build.
 *
 * @author Etienne Studer
 */
public interface ModelRepository extends ObservableModelRepository {

    /**
     * Fetches the {@link OmniBuildEnvironment} synchronously and broadcasts it through a {@link BuildEnvironmentUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the build environment, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    OmniBuildEnvironment fetchBuildEnvironment(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

    /**
     * Fetches the {@link OmniGradleBuildStructure} synchronously and broadcasts it through a {@link GradleBuildStructureUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the gradle build, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    OmniGradleBuildStructure fetchGradleBuildStructure(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

    /**
     * Fetches the {@link OmniGradleBuild} synchronously and broadcasts it through a {@link GradleBuildUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the gradle project, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    OmniGradleBuild fetchGradleBuild(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

    /**
     * Fetches the {@link OmniEclipseGradleBuild} synchronously and broadcasts it through a {@link EclipseGradleBuildUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the eclipse project, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    OmniEclipseGradleBuild fetchEclipseGradleBuild(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

    /**
     * Fetches the {@link OmniBuildInvocationsContainer} synchronously and broadcasts it through a {@link BuildInvocationsUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the build invocations container, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    OmniBuildInvocationsContainer fetchBuildInvocations(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

    /**
     * Fetches the {@link OmniEclipseProject} models synchronously.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the result, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is
     * not in the cache
     */
    ModelResults<OmniEclipseProject> fetchEclipseProjects(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

    /**
     * Fetches the {@link OmniBuildEnvironment} models synchronously.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the result, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is
     * not in the cache
     */
    ModelResults<OmniBuildEnvironment> fetchBuildEnvironments(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);
}
