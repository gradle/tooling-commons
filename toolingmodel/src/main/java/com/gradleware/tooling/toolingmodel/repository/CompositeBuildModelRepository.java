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

import org.gradle.tooling.connection.ModelResults;

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;

/**
 * Repository for Gradle build models sourced from a composition of several Gradle builds.
 *
 * @author Stefan Oehme
 */
public interface CompositeBuildModelRepository extends ModelRepository {

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
