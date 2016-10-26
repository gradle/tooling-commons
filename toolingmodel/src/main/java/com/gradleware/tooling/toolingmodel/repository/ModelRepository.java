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

import java.util.Set;

/**
 * Repository for Gradle build models sourced from a Gradle build.
 * <p/>
 * Listeners can be registered to get notified about model updates. It is left to the implementation through which
 * channel the events are broadcast.
 *
 * @author Etienne Studer
 */
public interface ModelRepository {

    /**
     *
     * @param listener listener to subscribe to receiving events
     */
    void register(Object listener);

    /**
     * Unregisters the given {@code listener} from receiving model change events.
     *
     * @param listener listener to unsubscribe from receiving events
     */
    void unregister(Object listener);

    /**
     * Fetches the {@link OmniBuildEnvironment} synchronously and broadcasts it through a {@link BuildEnvironmentUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy              the fetch strategy
     * @return the build environment, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    OmniBuildEnvironment fetchBuildEnvironment(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

    /**
     * Fetches the {@link OmniGradleBuild} synchronously and broadcasts it through a {@link GradleBuildUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy              the fetch strategy
     * @return the gradle build, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    OmniGradleBuild fetchGradleBuild(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

    /**
     * Fetches the {@link OmniGradleProject} synchronously and broadcasts it through a {@link GradleProjectUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy              the fetch strategy
     * @return the gradle projects, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    Set<OmniGradleProject> fetchGradleProjects(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

    /**
     * Fetches the {@link OmniEclipseProject} synchronously and broadcasts it through a {@link EclipseProjectUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy              the fetch strategy
     * @return the eclipse projects, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    Set<OmniEclipseProject> fetchEclipseGradleProjects(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);
}
