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

package com.gradleware.tooling.toolingmodel.repository.internal;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.UncheckedExecutionException;

import com.gradleware.tooling.toolingclient.Consumer;
import com.gradleware.tooling.toolingclient.Request;
import com.gradleware.tooling.toolingclient.ToolingClient;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.ObservableModelRepository;
import org.gradle.tooling.connection.ModelResults;

/**
 * Common base class for {@code ObservableModelRepository} implementations. Model updates are broadcast via Google Guava's {@link EventBus}.
 *
 * @author Etienne Studer
 */
public abstract class BaseModelRepository implements ObservableModelRepository {

    private final ToolingClient toolingClient;
    private final EventBus eventBus;
    private final Cache<Object, Object> cache;

    public BaseModelRepository(ToolingClient toolingClient, EventBus eventBus) {
        this.toolingClient = Preconditions.checkNotNull(toolingClient);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.cache = CacheBuilder.newBuilder().build();
    }

    /**
     * Registers all subscriber methods on {@code listener} to receive model change events.
     *
     * @param listener object whose subscriber methods should be registered
     * @see com.google.common.eventbus.EventBus#register(Object)
     */
    @Override
    public void register(Object listener) {
        Preconditions.checkNotNull(listener);
        this.eventBus.register(listener);
    }

    /**
     * Unregisters all subscriber methods on a registered {@code listener} from receiving model
     * change events.
     *
     * @param listener object whose subscriber methods should be unregistered
     * @see com.google.common.eventbus.EventBus#unregister(Object)
     */
    @Override
    public void unregister(Object listener) {
        Preconditions.checkNotNull(listener);
        this.eventBus.unregister(listener);
    }

    protected ToolingClient getToolingClient() {
        return this.toolingClient;
    }

    protected void postEvent(Object event) {
        this.eventBus.post(event);
    }

    protected <T, U> U executeRequest(final Request<T> request, final Consumer<U> newCacheEntryHandler, FetchStrategy fetchStrategy, Class<?> cacheKey,
            final Converter<T, U> resultConverter) {
        return executeRequest(new Supplier<T>() {

            @Override
            public T get() {
                // issue the request (synchronously)
                // it is assumed that the result returned by a model request or build action request
                // is never null
                return request.executeAndWait();
            }
        }, newCacheEntryHandler, fetchStrategy, cacheKey, resultConverter);
    }

    protected <T, U> ModelResults<U> executeRequest(Request<ModelResults<T>> request, FetchStrategy fetchStrategy, Class<?> cacheKey, Converter<T, U> resultConverter) {
        Consumer<ModelResults<U>> dontSendEvents = new Consumer<ModelResults<U>>() {

            @Override
            public void accept(ModelResults<U> input) {
            }
        };
        return executeRequest(request, dontSendEvents, fetchStrategy, cacheKey, new ModelResultsConverter<T, U>(resultConverter));
    }

    protected <T, U> U executeRequest(final Supplier<T> operation, final Consumer<U> newCacheEntryHandler, FetchStrategy fetchStrategy, Class<?> cacheKey,
            final Converter<T, U> resultConverter) {
        // if model is only accessed from the cache, we can return immediately
        if (FetchStrategy.FROM_CACHE_ONLY == fetchStrategy) {
            @SuppressWarnings("unchecked")
            U result = (U) this.cache.getIfPresent(cacheKey);
            return result;
        }

        // if model must be reloaded, we can invalidate the cache entry and then proceed as for
        // FetchStrategy.LOAD_IF_NOT_CACHED
        if (FetchStrategy.FORCE_RELOAD == fetchStrategy) {
            this.cache.invalidate(cacheKey);
        }

        // load the values from the cache iff not already cached
        final AtomicBoolean modelLoaded = new AtomicBoolean(false);
        U value = getFromCache(cacheKey, new Callable<U>() {

            @Override
            public U call() {
                U model = executeAndWait(operation, resultConverter);
                modelLoaded.set(true);
                return model;
            }
        });

        // if the model was not in the cache before, notify the callback about the new cache entry
        if (modelLoaded.get()) {
            newCacheEntryHandler.accept(value);
        }

        return value;
    }

    private <U> U getFromCache(Class<?> cacheKey, Callable<U> cacheValueLoader) {
        try {
            @SuppressWarnings("unchecked")
            U result = (U) this.cache.get(cacheKey, cacheValueLoader);
            return result;
        } catch (UncheckedExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private <T, U> U executeAndWait(Supplier<T> operation, Converter<T, U> resultConverter) {
        // invoke the operation and convert the result
        T result = operation.get();
        return resultConverter.apply(result);
    }

}
