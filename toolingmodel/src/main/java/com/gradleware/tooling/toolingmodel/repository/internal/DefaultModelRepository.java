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

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.gradleware.tooling.toolingclient.*;
import com.gradleware.tooling.toolingmodel.*;
import com.gradleware.tooling.toolingmodel.buildaction.BuildActionFactory;
import com.gradleware.tooling.toolingmodel.buildaction.RootModelsForCompositeProjectBuildAction;
import com.gradleware.tooling.toolingmodel.repository.*;
import org.gradle.tooling.BuildAction;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.gradle.GradleBuild;
import org.gradle.util.GradleVersion;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Repository for Gradle build models.
 *
 * @author Etienne Studer
 */
public final class DefaultModelRepository implements ModelRepository {

    private final ToolingClient toolingClient;
    private final EventBus eventBus;
    private final Cache<Object, Object> cache;
    private final FixedRequestAttributes fixedRequestAttributes;

    public DefaultModelRepository(FixedRequestAttributes fixedRequestAttributes, ToolingClient toolingClient, EventBus eventBus) {
        this.toolingClient = Preconditions.checkNotNull(toolingClient);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.cache = CacheBuilder.newBuilder().build();
        this.fixedRequestAttributes = Preconditions.checkNotNull(fixedRequestAttributes);
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

    /*
     * natively supported by all Gradle versions >= 1.0
     */
    @Override
    public OmniBuildEnvironment fetchBuildEnvironment(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
        Preconditions.checkNotNull(transientRequestAttributes);
        Preconditions.checkNotNull(fetchStrategy);

        ModelRequest<BuildEnvironment> request = createModelRequestForBuildModel(BuildEnvironment.class, transientRequestAttributes);
        Consumer<OmniBuildEnvironment> successHandler = new Consumer<OmniBuildEnvironment>() {

            @Override
            public void accept(OmniBuildEnvironment result) {
                DefaultModelRepository.this.eventBus.post(new BuildEnvironmentUpdateEvent(result));
            }
        };
        Converter<BuildEnvironment, OmniBuildEnvironment> converter = new BaseConverter<BuildEnvironment, OmniBuildEnvironment>() {

            @Override
            public OmniBuildEnvironment apply(BuildEnvironment buildEnvironment) {
                return DefaultOmniBuildEnvironment.from(buildEnvironment);
            }

        };

        return executeRequest(request, successHandler, fetchStrategy, OmniBuildEnvironment.class, converter);
    }

    /*
     * supported by all Gradle versions either natively (>=1.8) or through conversion from a GradleProject (<1.8)
     * note that for versions <1.8, the conversion from GradleProject to GradleBuild happens outside of our control and
     * thus that GradleProject instance cannot be cached, this means that if the GradleProject model is later asked for
     * from this model repository, it needs to be fetched again (and then gets cached)
     */
    @Override
    public OmniGradleBuild fetchGradleBuild(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
        Preconditions.checkNotNull(transientRequestAttributes);
        Preconditions.checkNotNull(fetchStrategy);

        ModelRequest<GradleBuild> request = createModelRequestForBuildModel(GradleBuild.class, transientRequestAttributes);
        Consumer<OmniGradleBuild> successHandler = new Consumer<OmniGradleBuild>() {
            @Override
            public void accept(OmniGradleBuild result) {
                DefaultModelRepository.this.eventBus.post(new GradleBuildUpdateEvent(result));
            }
        };
        Converter<GradleBuild, OmniGradleBuild> converter = new BaseConverter<GradleBuild, OmniGradleBuild>() {

            @Override
            public OmniGradleBuild apply(GradleBuild gradleBuild) {
                return DefaultOmniGradleBuild.from(gradleBuild);
            }

        };

        return executeRequest(request, successHandler, fetchStrategy, GradleBuild.class, converter);
    }

    /*
     * natively supported by all Gradle versions >= 1.0
     */
    @Override
    public Set<OmniGradleProject> fetchGradleProjects(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
        Preconditions.checkNotNull(transientRequestAttributes);
        Preconditions.checkNotNull(fetchStrategy);
        if (!supportsCompositeBuilds(transientRequestAttributes)) {
            ModelRequest<GradleProject> request = createModelRequestForBuildModel(GradleProject.class, transientRequestAttributes);
            Consumer<Set<OmniGradleProject>> successHandler = new Consumer<Set<OmniGradleProject>>() {
                @Override
                public void accept(Set<OmniGradleProject> result) {
                    DefaultModelRepository.this.eventBus.post(new GradleProjectUpdateEvent(result));
                }
            };
            Converter<GradleProject, Set<OmniGradleProject>> converter = new BaseConverter<GradleProject, Set<OmniGradleProject>>() {

                @Override
                public Set<OmniGradleProject> apply(GradleProject gradleProject) {
                    return ImmutableSet.copyOf(DefaultOmniGradleProject.from(gradleProject).getAll());
                }

            };
            return executeRequest(request, successHandler, fetchStrategy, OmniGradleProject.class, converter);
        } else {
            BuildActionRequest<Collection<GradleProject>> request = createBuildActionRequestForCompositeModel(GradleProject.class, transientRequestAttributes);
            Consumer<Set<OmniGradleProject>> successHandler = new Consumer<Set<OmniGradleProject>>() {
                @Override
                public void accept(Set<OmniGradleProject> result) {
                    DefaultModelRepository.this.eventBus.post(new GradleProjectUpdateEvent(result));
                }
            };
            Converter<Collection<GradleProject>, Set<OmniGradleProject>> converter = new BaseConverter<Collection<GradleProject>, Set<OmniGradleProject>>() {

                @Override
                public Set<OmniGradleProject> apply(Collection<GradleProject> gradleProjects) {
                    ImmutableSet.Builder<OmniGradleProject> projects = ImmutableSet.builder();
                    for (GradleProject gradleProject : gradleProjects) {
                        projects.addAll(DefaultOmniGradleProject.from(gradleProject).getAll());
                    }
                    return projects.build();
                }

            };
            return executeRequest(request, successHandler, fetchStrategy, OmniGradleProject.class, converter);
        }
    }

    /*
     * natively supported by all Gradle versions >= 1.0
     */
    @Override
    public Set<OmniEclipseProject> fetchEclipseGradleProjects(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
        Preconditions.checkNotNull(transientRequestAttributes);
        Preconditions.checkNotNull(fetchStrategy);

        if (!supportsCompositeBuilds(transientRequestAttributes)) {
            ModelRequest<EclipseProject> request = createModelRequestForBuildModel(EclipseProject.class, transientRequestAttributes);
            Consumer<Set<OmniEclipseProject>> successHandler = new Consumer<Set<OmniEclipseProject>>() {
                @Override
                public void accept(Set<OmniEclipseProject> result) {
                    DefaultModelRepository.this.eventBus.post(new EclipseProjectUpdateEvent(result));
                }
            };
            Converter<EclipseProject, Set<OmniEclipseProject>> converter = new BaseConverter<EclipseProject, Set<OmniEclipseProject>>() {

                @Override
                public Set<OmniEclipseProject> apply(EclipseProject eclipseProject) {
                    return ImmutableSet.copyOf(DefaultOmniEclipseProject.from(eclipseProject).getAll());
                }
            };
        return executeRequest(request, successHandler, fetchStrategy, OmniEclipseProject.class, converter);
        } else {
            BuildActionRequest<Collection<EclipseProject>> request = createBuildActionRequestForCompositeModel(EclipseProject.class, transientRequestAttributes);
            Consumer<Set<OmniEclipseProject>> successHandler = new Consumer<Set<OmniEclipseProject>>() {
                @Override
                public void accept(Set<OmniEclipseProject> result) {
                    DefaultModelRepository.this.eventBus.post(new EclipseProjectUpdateEvent(result));
                }
            };
            Converter<Collection<EclipseProject>, Set<OmniEclipseProject>> converter = new BaseConverter<Collection<EclipseProject>, Set<OmniEclipseProject>>() {

                @Override
                public Set<OmniEclipseProject> apply(Collection<EclipseProject> eclipseProjects) {
                    ImmutableSet.Builder<OmniEclipseProject> projects = ImmutableSet.builder();
                    for (EclipseProject eclipseProject : eclipseProjects) {
                        projects.addAll(DefaultOmniEclipseProject.from(eclipseProject).getAll());
                    }
                    return projects.build();
                }

            };
            return executeRequest(request, successHandler, fetchStrategy, OmniEclipseProject.class, converter);
        }
    }

    private boolean supportsCompositeBuilds(TransientRequestAttributes transientRequestAttributes) {
        return targetGradleVersionIsEqualOrHigherThan("3.3", transientRequestAttributes);
    }

    private boolean targetGradleVersionIsEqualOrHigherThan(String refVersion, TransientRequestAttributes transientRequestAttributes) {
        OmniBuildEnvironment buildEnvironment = fetchBuildEnvironment(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED);
        GradleVersion gradleVersion = GradleVersion.version(buildEnvironment.getGradle().getGradleVersion());
        return gradleVersion.getBaseVersion().compareTo(GradleVersion.version(refVersion)) >= 0;
    }

    private <T> ModelRequest<T> createModelRequestForBuildModel(Class<T> model, TransientRequestAttributes transientRequestAttributes) {
        // build the request
        ModelRequest<T> request = this.toolingClient.newModelRequest(model);
        this.fixedRequestAttributes.apply(request);
        transientRequestAttributes.apply(request);
        return request;
    }

    private <T> BuildActionRequest<Collection<T>> createBuildActionRequestForCompositeModel(Class<T> model, TransientRequestAttributes transientRequestAttributes) {
        // build the request
        RootModelsForCompositeProjectBuildAction<T> buildAction = BuildActionFactory.getModelForCompositeProjects(model);
        BuildActionRequest<Collection<T>> request = this.toolingClient.newBuildActionRequest(buildAction);
        this.fixedRequestAttributes.apply(request);
        transientRequestAttributes.apply(request);
        return request;
    }

    @SuppressWarnings({ "UnusedDeclaration", "unused" })
    private <T> BuildActionRequest<T> createBuildActionRequestForBuildAction(BuildAction<T> buildAction, TransientRequestAttributes transientRequestAttributes) {
        // build the request
        BuildActionRequest<T> request = this.toolingClient.newBuildActionRequest(buildAction);
        this.fixedRequestAttributes.apply(request);
        transientRequestAttributes.apply(request);
        return request;
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
