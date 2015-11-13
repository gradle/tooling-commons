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

package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.gradleware.tooling.toolingclient.BuildActionRequest;
import com.gradleware.tooling.toolingclient.Consumer;
import com.gradleware.tooling.toolingclient.ModelRequest;
import com.gradleware.tooling.toolingclient.Request;
import com.gradleware.tooling.toolingclient.ToolingClient;
import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniBuildInvocationsContainer;
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure;
import com.gradleware.tooling.toolingmodel.buildaction.BuildActionFactory;
import com.gradleware.tooling.toolingmodel.buildaction.ModelForAllProjectsBuildAction;
import com.gradleware.tooling.toolingmodel.repository.BuildEnvironmentUpdateEvent;
import com.gradleware.tooling.toolingmodel.repository.BuildInvocationsUpdateEvent;
import com.gradleware.tooling.toolingmodel.repository.EclipseGradleBuildUpdateEvent;
import com.gradleware.tooling.toolingmodel.repository.Environment;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.GradleBuildStructureUpdateEvent;
import com.gradleware.tooling.toolingmodel.repository.GradleBuildUpdateEvent;
import com.gradleware.tooling.toolingmodel.repository.ModelRepository;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;
import org.gradle.tooling.BuildAction;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.gradle.BuildInvocations;
import org.gradle.tooling.model.gradle.GradleBuild;
import org.gradle.util.GradleVersion;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Repository for Gradle build models. Model updates are broadcast via Google Guava's {@link EventBus}.
 * <p/>
 * This repository is aware of the different constraints given by the Environment in which the repository is used and given by the target Gradle version.
 *
 * @author Etienne Studer
 */
public final class DefaultModelRepository implements ModelRepository {

    private final FixedRequestAttributes fixedRequestAttributes;
    private final ToolingClient toolingClient;
    private final EventBus eventBus;
    private final Cache<Class<?>, Object> cache;
    private final Environment environment;

    public DefaultModelRepository(FixedRequestAttributes fixedRequestAttributes, ToolingClient toolingClient, EventBus eventBus) {
        this(fixedRequestAttributes, toolingClient, eventBus, Environment.STANDALONE);
    }

    public DefaultModelRepository(FixedRequestAttributes fixedRequestAttributes, ToolingClient toolingClient, EventBus eventBus, Environment environment) {
        this.fixedRequestAttributes = Preconditions.checkNotNull(fixedRequestAttributes);
        this.toolingClient = Preconditions.checkNotNull(toolingClient);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.cache = CacheBuilder.newBuilder().build();
        this.environment = environment;
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
     * Unregisters all subscriber methods on a registered {@code listener} from receiving model change events.
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
    public OmniGradleBuildStructure fetchGradleBuildStructure(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
        Preconditions.checkNotNull(transientRequestAttributes);
        Preconditions.checkNotNull(fetchStrategy);

        ModelRequest<GradleBuild> request = createModelRequestForBuildModel(GradleBuild.class, transientRequestAttributes);
        Consumer<OmniGradleBuildStructure> successHandler = new Consumer<OmniGradleBuildStructure>() {
            @Override
            public void accept(OmniGradleBuildStructure result) {
                DefaultModelRepository.this.eventBus.post(new GradleBuildStructureUpdateEvent(result));
            }
        };
        Converter<GradleBuild, OmniGradleBuildStructure> converter = new BaseConverter<GradleBuild, OmniGradleBuildStructure>() {

            @Override
            public OmniGradleBuildStructure apply(GradleBuild gradleBuild) {
                return DefaultOmniGradleBuildStructure.from(gradleBuild);
            }

        };

        return executeRequest(request, successHandler, fetchStrategy, OmniGradleBuildStructure.class, converter);
    }

    /*
     * natively supported by all Gradle versions >= 1.0
     */
    @Override
    public OmniGradleBuild fetchGradleBuild(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
        Preconditions.checkNotNull(transientRequestAttributes);
        Preconditions.checkNotNull(fetchStrategy);

        // in versions 2.1 and 2.2.1, all projects tasks are falsely set to public = false in the Tooling API
        final boolean requiresIsPublicFix = targetGradleVersionIsBetween("2.1", "2.2.1", transientRequestAttributes);

        ModelRequest<GradleProject> request = createModelRequestForBuildModel(GradleProject.class, transientRequestAttributes);
        Consumer<OmniGradleBuild> successHandler = new Consumer<OmniGradleBuild>() {
            @Override
            public void accept(OmniGradleBuild result) {
                DefaultModelRepository.this.eventBus.post(new GradleBuildUpdateEvent(result));
            }
        };
        Converter<GradleProject, OmniGradleBuild> converter = new BaseConverter<GradleProject, OmniGradleBuild>() {

            @Override
            public OmniGradleBuild apply(GradleProject gradleProject) {
                return DefaultOmniGradleBuild.from(gradleProject, requiresIsPublicFix);
            }

        };

        return executeRequest(request, successHandler, fetchStrategy, OmniGradleBuild.class, converter);
    }

    /*
     * natures and build commands supported by Gradle versions >= 2.9
     * source language level supported by Gradle version >= 2.10
     */
    @Override
    public OmniEclipseGradleBuild fetchEclipseGradleBuild(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
        Preconditions.checkNotNull(transientRequestAttributes);
        Preconditions.checkNotNull(fetchStrategy);

        final boolean requiresIsPublicFix = targetGradleVersionIsBetween("2.1", "2.2.1", transientRequestAttributes);
        final boolean buildCommandsAndNaturesAvailable = targetGradleVersionIsEqualOrHigherThan("2.9", transientRequestAttributes);
        final boolean sourceSettingsAvailable = targetGradleVersionIsEqualOrHigherThan("2.10", transientRequestAttributes);

        ModelRequest<EclipseProject> request = createModelRequestForBuildModel(EclipseProject.class, transientRequestAttributes);
        Consumer<OmniEclipseGradleBuild> successHandler = new Consumer<OmniEclipseGradleBuild>() {
            @Override
            public void accept(OmniEclipseGradleBuild result) {
                DefaultModelRepository.this.eventBus.post(new EclipseGradleBuildUpdateEvent(result));
            }
        };
        Converter<EclipseProject, OmniEclipseGradleBuild> converter = new BaseConverter<EclipseProject, OmniEclipseGradleBuild>() {

            @Override
            public OmniEclipseGradleBuild apply(EclipseProject eclipseProject) {
                return DefaultOmniEclipseGradleBuild.from(eclipseProject, requiresIsPublicFix, buildCommandsAndNaturesAvailable, sourceSettingsAvailable);
            }

        };
        return executeRequest(request, successHandler, fetchStrategy, OmniEclipseGradleBuild.class, converter);
    }

    /*
     * natively supported by all Gradle versions >= 1.12, artificially constructable from a GradleProject model for all Gradle versions >= 1.0
     * native support requires BuildActions which are available in standalone environments for Gradle versions >= 1.8 and in Eclipse environments for Gradle versions >= 2.3
     */
    @Override
    public OmniBuildInvocationsContainer fetchBuildInvocations(final TransientRequestAttributes transientRequestAttributes, final FetchStrategy fetchStrategy) {
        Preconditions.checkNotNull(transientRequestAttributes);
        Preconditions.checkNotNull(fetchStrategy);

        // natively supported by all Gradle versions >= 1.12, if BuildActions supported in the running environment
        if (!supportsBuildInvocations(transientRequestAttributes) || !supportsBuildActions(transientRequestAttributes)) {
            Supplier<OmniBuildInvocationsContainer> operation = new Supplier<OmniBuildInvocationsContainer>() {
                @Override
                public OmniBuildInvocationsContainer get() {
                    return deriveBuildInvocationsFromOtherModel(transientRequestAttributes, fetchStrategy);
                }
            };
            Consumer<OmniBuildInvocationsContainer> successHandler = new Consumer<OmniBuildInvocationsContainer>() {
                @Override
                public void accept(OmniBuildInvocationsContainer result) {
                    DefaultModelRepository.this.eventBus.post(new BuildInvocationsUpdateEvent(result));
                }
            };
            Converter<OmniBuildInvocationsContainer, OmniBuildInvocationsContainer> converter = Converter.identity();
            return executeRequest(operation, successHandler, fetchStrategy, OmniBuildInvocationsContainer.class, converter);
        }

        BuildActionRequest<Map<String, BuildInvocations>> request = createBuildActionRequestForProjectModel(BuildInvocations.class, transientRequestAttributes);
        Consumer<OmniBuildInvocationsContainer> successHandler = new Consumer<OmniBuildInvocationsContainer>() {
            @Override
            public void accept(OmniBuildInvocationsContainer result) {
                DefaultModelRepository.this.eventBus.post(new BuildInvocationsUpdateEvent(result));
            }
        };
        Converter<Map<String, BuildInvocations>, OmniBuildInvocationsContainer> converter = new BaseConverter<Map<String, BuildInvocations>, OmniBuildInvocationsContainer>() {

            @Override
            public OmniBuildInvocationsContainer apply(Map<String, BuildInvocations> buildInvocations) {
                return DefaultOmniBuildInvocationsContainer.from(buildInvocations);
            }

        };
        return executeRequest(request, successHandler, fetchStrategy, OmniBuildInvocationsContainer.class, converter);
    }

    private OmniBuildInvocationsContainer deriveBuildInvocationsFromOtherModel(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
        // for fetch strategy FORCE_RELOAD, we re-fetch the GradleBuild model and derive the build invocations from it
        if (fetchStrategy == FetchStrategy.FORCE_RELOAD) {
            OmniGradleBuild gradleBuild = fetchGradleBuild(transientRequestAttributes, FetchStrategy.FORCE_RELOAD);
            return DefaultOmniBuildInvocationsContainer.from(gradleBuild.getRootProject());
        }

        // for the fetch strategies other than FORCE_RELOAD, we first check if there is a model already available from which we can derive the build invocations
        OmniGradleBuild gradleBuild = fetchGradleBuild(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY);
        if (gradleBuild != null) {
            return DefaultOmniBuildInvocationsContainer.from(gradleBuild.getRootProject());
        } else {
            OmniEclipseGradleBuild eclipseGradleBuild = fetchEclipseGradleBuild(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY);
            if (eclipseGradleBuild != null) {
                return DefaultOmniBuildInvocationsContainer.from(eclipseGradleBuild.getRootProject());
            }
        }

        // for fetch strategy LOAD_IF_NOT_CACHED, fetch the GradleBuild model and derive the build invocations from it
        gradleBuild = fetchGradleBuild(transientRequestAttributes, fetchStrategy);
        return DefaultOmniBuildInvocationsContainer.from(gradleBuild.getRootProject());
    }

    private boolean supportsBuildInvocations(TransientRequestAttributes transientRequestAttributes) {
        return targetGradleVersionIsEqualOrHigherThan("1.12", transientRequestAttributes);
    }

    private boolean supportsBuildActions(TransientRequestAttributes transientRequestAttributes) {
        if (this.environment == Environment.ECLIPSE) {
            // in an Eclipse/OSGi environment, the Tooling API supports BuildActions only in Gradle versions >= 2.3
            return targetGradleVersionIsEqualOrHigherThan("2.3", transientRequestAttributes);

        } else {
            // in all other environments, BuildActions are supported as of Gradle version >= 1.8
            return targetGradleVersionIsEqualOrHigherThan("1.8", transientRequestAttributes);
        }
    }

    private boolean targetGradleVersionIsBetween(String minVersion, String maxVersion, TransientRequestAttributes transientRequestAttributes) {
        OmniBuildEnvironment buildEnvironment = fetchBuildEnvironment(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED);
        GradleVersion gradleVersion = GradleVersion.version(buildEnvironment.getGradle().getGradleVersion());
        return gradleVersion.getBaseVersion().compareTo(GradleVersion.version(minVersion)) >= 0 &&
                gradleVersion.getBaseVersion().compareTo(GradleVersion.version(maxVersion)) <= 0;
    }

    private boolean targetGradleVersionIsEqualOrHigherThan(String refVersion, TransientRequestAttributes transientRequestAttributes) {
        OmniBuildEnvironment buildEnvironment = fetchBuildEnvironment(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED);
        GradleVersion gradleVersion = GradleVersion.version(buildEnvironment.getGradle().getGradleVersion());
        return gradleVersion.getBaseVersion().compareTo(GradleVersion.version(refVersion)) >= 0;
    }

    private <T, U> U executeRequest(final Request<T> request, final Consumer<U> newCacheEntryHandler, FetchStrategy fetchStrategy, Class<U> cacheKey, final Converter<T, U> resultConverter) {
        return executeRequest(new Supplier<T>() {
            @Override
            public T get() {
                // issue the request (synchronously)
                // it is assumed that the result returned by a model request or build action request is never null
                return request.executeAndWait();
            }
        }, newCacheEntryHandler, fetchStrategy, cacheKey, resultConverter);
    }

    private <T, U> U executeRequest(final Supplier<T> operation, final Consumer<U> newCacheEntryHandler, FetchStrategy fetchStrategy, Class<U> cacheKey, final Converter<T, U> resultConverter) {
        // if model is only accessed from the cache, we can return immediately
        if (FetchStrategy.FROM_CACHE_ONLY == fetchStrategy) {
            Object result = this.cache.getIfPresent(cacheKey);
            return cacheKey.cast(result);
        }

        // if model must be reloaded, we can invalidate the cache entry and then proceed as for FetchStrategy.LOAD_IF_NOT_CACHED
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

    private <U> U getFromCache(Class<U> cacheKey, Callable<U> cacheValueLoader) {
        try {
            Object result = this.cache.get(cacheKey, cacheValueLoader);
            return cacheKey.cast(result);
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

    private <T> ModelRequest<T> createModelRequestForBuildModel(Class<T> model, TransientRequestAttributes transientRequestAttributes) {
        // build the request
        ModelRequest<T> request = this.toolingClient.newModelRequest(model);
        this.fixedRequestAttributes.apply(request);
        transientRequestAttributes.apply(request);
        return request;
    }

    private <T> BuildActionRequest<Map<String, T>> createBuildActionRequestForProjectModel(Class<T> model, TransientRequestAttributes transientRequestAttributes) {
        // build the request
        ModelForAllProjectsBuildAction<T> buildAction = BuildActionFactory.getModelForAllProjects(model);
        BuildActionRequest<Map<String, T>> request = this.toolingClient.newBuildActionRequest(buildAction);
        this.fixedRequestAttributes.apply(request);
        transientRequestAttributes.apply(request);
        return request;
    }

    @SuppressWarnings("UnusedDeclaration")
    private <T> BuildActionRequest<T> createBuildActionRequestForBuildAction(BuildAction<T> buildAction, TransientRequestAttributes transientRequestAttributes) {
        // build the request
        BuildActionRequest<T> request = this.toolingClient.newBuildActionRequest(buildAction);
        this.fixedRequestAttributes.apply(request);
        transientRequestAttributes.apply(request);
        return request;
    }

    private <T, U> U executeAndWait(Supplier<T> operation, Converter<T, U> resultConverter) {
        // invoke the operation and convert the result
        T result = operation.get();
        return resultConverter.apply(result);
    }

}
