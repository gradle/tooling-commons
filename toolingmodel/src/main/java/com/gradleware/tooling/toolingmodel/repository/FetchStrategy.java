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

package com.gradleware.tooling.toolingmodel.repository;

/**
 * Enumerates different strategies of how to fetch a given value from a caching data provider.
 *
 * @author Etienne Studer
 */
public enum FetchStrategy {

    /**
     * Looks up the requested value in the cache only. If the value is not present, the value is not loaded from the underlying system.
     */
    FROM_CACHE_ONLY,

    /**
     * Looks up the requested value in the cache and, iff the value is not present in the cache, loads the value from the underlying system.
     */
    LOAD_IF_NOT_CACHED,

    /**
     * Loads the value from the underlying system, regardless of whether the value is currently in the cache or not.
     */
    FORCE_RELOAD

}
