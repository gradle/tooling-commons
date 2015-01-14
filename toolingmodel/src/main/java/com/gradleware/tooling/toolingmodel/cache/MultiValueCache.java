package com.gradleware.tooling.toolingmodel.cache;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Cached mapping from keys to values.
 *
 * @param <K> the type of the keys
 * @param <V> the type of the values
 */
public final class MultiValueCache<K, V> {

    private final SingleValueCacheMapper<K, V> singleValueCaches;

    public MultiValueCache() {
        this.singleValueCaches = new SingleValueCacheMapper<K, V>();
    }

    /**
     * Gets the value for the given key, if the value has been loaded into the cache.
     *
     * @param key the key by which to look up the value
     * @return the value if present, {@link Optional#absent()} otherwise
     */
    public Optional<V> getIfPresent(K key) {
        Preconditions.checkArgument(key != null, "Key must not be null");

        // first, check if a cache exists for the given key, this avoid creating an 'empty' cache just because someone asked for it
        Optional<SingleValueCache<V>> cache = this.singleValueCaches.getIfPresent(key);
        if (cache.isPresent()) {
            return cache.get().getIfPresent();
        } else {
            return Optional.absent();
        }
    }

    /**
     * Gets the value for the given key. If the value has already been loaded into the cache, the value is returned. If the value is currently being loaded by some other thread,
     * waits for the loading to complete and then returns the value. If the value has not been started to load yet, triggers the loading using the given value loader and then
     * returns the value.
     *
     * @param key the key by which to look up the value
     * @param valueLoader the value loader invoked if the value is not yet loaded
     * @return the value
     * @throws CacheException thrown if something goes wrong while loading the value
     */
    public V get(K key, Callable<? extends V> valueLoader) throws CacheException {
        Preconditions.checkArgument(key != null, "Key must not be null");
        Preconditions.checkArgument(valueLoader != null, "Value loader must not be null");

        // reuse the existing cache for the given key, otherwise create a new cache
        return this.singleValueCaches.get(key).get(valueLoader);
    }

    /**
     * Stores the given value under the given key.
     *
     * @param key the key under which to store the value
     * @param value the value to store
     * @throws IllegalArgumentException thrown if key or value is null
     */
    public void put(K key, V value) {
        Preconditions.checkArgument(key != null, "Key must not be null");
        Preconditions.checkArgument(value != null, "Value must not be null");

        // reuse the existing cache for the given key, otherwise create a new cache
        this.singleValueCaches.get(key).put(value);
    }

    /**
     * Invalidates the given key by removing its stored value from the cache.
     *
     * @param key the key to invalidate
     */
    public void invalidate(K key) {
        Preconditions.checkArgument(key != null, "Key must not be null");

        // first, remove the cache if it exists for the given key, this avoids keeping around an 'empty' cache
        Optional<SingleValueCache<V>> cache = this.singleValueCaches.remove(key);
        if (cache.isPresent()) {
            cache.get().invalidate();
        }
    }

    /**
     * Holds mappings from keys to values of type {@code SingleValueCache} where the value is created on first access by a given key.
     */
    private static final class SingleValueCacheMapper<K, V> {

        private final Map<K, SingleValueCache<V>> mapping;

        private SingleValueCacheMapper() {
            this.mapping = Maps.newHashMap();
        }

        /**
         * Get the cache iff there exists a mapping for the given key.
         *
         * @param key the cache key
         * @return the cache for the given key if the mapping already exists
         */
        private Optional<SingleValueCache<V>> getIfPresent(K key) {
            synchronized (this.mapping) {
                if (this.mapping.containsKey(key)) {
                    return Optional.of(this.mapping.get(key));
                } else {
                    return Optional.absent();
                }
            }
        }

        /**
         * Get the cache for the given key.
         *
         * @param key the cache key
         * @return the cache for the given key
         */
        private SingleValueCache<V> get(K key) {
            synchronized (this.mapping) {
                if (this.mapping.containsKey(key)) {
                    return this.mapping.get(key);
                } else {
                    SingleValueCache<V> value = new SingleValueCache<V>();
                    this.mapping.put(key, value);
                    return value;
                }
            }
        }

        /**
         * Remove the cache if there exists a mapping for the given key.
         *
         * @param key the key of the cache to remove
         * @return the removed cache for the given key if the mapping already exists
         */
        private Optional<SingleValueCache<V>> remove(K key) {
            synchronized (this.mapping) {
                if (this.mapping.containsKey(key)) {
                    return Optional.of(this.mapping.remove(key));
                } else {
                    return Optional.absent();
                }
            }
        }

    }

}
