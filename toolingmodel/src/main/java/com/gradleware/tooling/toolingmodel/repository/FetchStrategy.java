package com.gradleware.tooling.toolingmodel.repository;

/**
 * Enumerates different strategies of how to fetch a given value from a caching data provider.
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
