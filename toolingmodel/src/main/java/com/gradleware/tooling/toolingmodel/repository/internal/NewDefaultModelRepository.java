package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Converter;
import com.google.common.base.Preconditions;
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
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure;
import com.gradleware.tooling.toolingmodel.buildaction.BuildActionFactory;
import com.gradleware.tooling.toolingmodel.buildaction.ModelForAllProjectsBuildAction;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.BuildEnvironmentUpdateEvent;
import com.gradleware.tooling.toolingmodel.repository.EclipseGradleBuildUpdateEvent;
import com.gradleware.tooling.toolingmodel.repository.GradleBuildStructureUpdateEvent;
import com.gradleware.tooling.toolingmodel.repository.GradleBuildUpdateEvent;
import com.gradleware.tooling.toolingmodel.repository.NewModelRepository;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;
import com.gradleware.tooling.toolingmodel.util.BaseConverter;
import org.gradle.tooling.BuildAction;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.gradle.GradleBuild;
import org.gradle.util.GradleVersion;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Repository for Gradle build models. Model updates are broadcast via Google Guava's {@link EventBus}.
 *
 * This repository is aware of the different constraints given by the Environment in which the repository is used and given by the target Gradle version.
 */
public final class NewDefaultModelRepository implements NewModelRepository {

    private final FixedRequestAttributes fixedRequestAttributes;
    private final ToolingClient toolingClient;
    private final EventBus eventBus;
    private final Cache<Class<?>, Object> cache;

    public NewDefaultModelRepository(FixedRequestAttributes fixedRequestAttributes, ToolingClient toolingClient, EventBus eventBus) {
        this.fixedRequestAttributes = Preconditions.checkNotNull(fixedRequestAttributes);
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
    public OmniBuildEnvironment fetchBuildEnvironmentAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
        Preconditions.checkNotNull(transientRequestAttributes);
        Preconditions.checkNotNull(fetchStrategy);

        ModelRequest<BuildEnvironment> request = createModelRequestForBuildModel(BuildEnvironment.class, transientRequestAttributes);
        Consumer<OmniBuildEnvironment> successHandler = new Consumer<OmniBuildEnvironment>() {

            @Override
            public void accept(OmniBuildEnvironment result) {
                NewDefaultModelRepository.this.eventBus.post(new BuildEnvironmentUpdateEvent(result));
            }
        };
        Converter<BuildEnvironment, OmniBuildEnvironment> converter = new BaseConverter<BuildEnvironment, OmniBuildEnvironment>() {

            @Override
            protected OmniBuildEnvironment doForward(BuildEnvironment buildEnvironment) {
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
    public OmniGradleBuildStructure fetchGradleBuildStructureAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
        Preconditions.checkNotNull(transientRequestAttributes);
        Preconditions.checkNotNull(fetchStrategy);

        ModelRequest<GradleBuild> request = createModelRequestForBuildModel(GradleBuild.class, transientRequestAttributes);
        Consumer<OmniGradleBuildStructure> successHandler = new Consumer<OmniGradleBuildStructure>() {
            @Override
            public void accept(OmniGradleBuildStructure result) {
                NewDefaultModelRepository.this.eventBus.post(new GradleBuildStructureUpdateEvent(result));
            }
        };
        Converter<GradleBuild, OmniGradleBuildStructure> converter = new BaseConverter<GradleBuild, OmniGradleBuildStructure>() {

            @Override
            protected OmniGradleBuildStructure doForward(GradleBuild gradleBuild) {
                return DefaultOmniGradleBuildStructure.from(gradleBuild);
            }

        };

        return executeRequest(request, successHandler, fetchStrategy, OmniGradleBuildStructure.class, converter);
    }

    /*
     * natively supported by all Gradle versions >= 1.0
     */
    @Override
    public OmniGradleBuild fetchGradleBuildAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
        Preconditions.checkNotNull(transientRequestAttributes);
        Preconditions.checkNotNull(fetchStrategy);

        // in versions 2.1 and 2.2.1, all projects tasks are falsely set to public = false in the Tooling API
        final boolean requiresIsPublicFix = targetGradleVersionIsBetween("2.1", "2.2.1", transientRequestAttributes);

        ModelRequest<GradleProject> request = createModelRequestForBuildModel(GradleProject.class, transientRequestAttributes);
        Consumer<OmniGradleBuild> successHandler = new Consumer<OmniGradleBuild>() {
            @Override
            public void accept(OmniGradleBuild result) {
                NewDefaultModelRepository.this.eventBus.post(new GradleBuildUpdateEvent(result));
            }
        };
        Converter<GradleProject, OmniGradleBuild> converter = new BaseConverter<GradleProject, OmniGradleBuild>() {

            @Override
            protected OmniGradleBuild doForward(GradleProject gradleProject) {
                return DefaultOmniGradleBuild.from(gradleProject, requiresIsPublicFix);
            }

        };

        return executeRequest(request, successHandler, fetchStrategy, OmniGradleBuild.class, converter);
    }

    /*
     * natively supported by all Gradle versions >= 1.0
     */
    @Override
    public OmniEclipseGradleBuild fetchEclipseGradleBuildAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
        Preconditions.checkNotNull(transientRequestAttributes);
        Preconditions.checkNotNull(fetchStrategy);

        final boolean requiresIsPublicFix = targetGradleVersionIsBetween("2.1", "2.2.1", transientRequestAttributes);

        ModelRequest<EclipseProject> request = createModelRequestForBuildModel(EclipseProject.class, transientRequestAttributes);
        Consumer<OmniEclipseGradleBuild> successHandler = new Consumer<OmniEclipseGradleBuild>() {
            @Override
            public void accept(OmniEclipseGradleBuild result) {
                NewDefaultModelRepository.this.eventBus.post(new EclipseGradleBuildUpdateEvent(result));
            }
        };
        Converter<EclipseProject, OmniEclipseGradleBuild> converter = new BaseConverter<EclipseProject, OmniEclipseGradleBuild>() {

            @Override
            protected OmniEclipseGradleBuild doForward(EclipseProject eclipseProject) {
                return DefaultOmniEclipseGradleBuild.from(eclipseProject, requiresIsPublicFix);
            }

        };
        return executeRequest(request, successHandler, fetchStrategy, OmniEclipseGradleBuild.class, converter);
    }

//    @Override
//    public BuildInvocationsContainer fetchBuildInvocationsAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
//        Preconditions.checkNotNull(transientRequestAttributes);
//        Preconditions.checkNotNull(fetchStrategy);
//
//        BuildActionRequest<Map<String, BuildInvocations>> request = createBuildActionRequestForProjectModel(BuildInvocations.class, transientRequestAttributes);
//        Consumer<BuildInvocationsContainer> successHandler = new Consumer<BuildInvocationsContainer>() {
//            @Override
//            public void accept(BuildInvocationsContainer result) {
//                eventBus.post(new BuildInvocationsUpdateEvent(result));
//            }
//        };
//
//        return executeRequest(request, successHandler, fetchStrategy, BuildInvocationsContainer.class, BuildInvocationsConverter.INSTANCE);
//    }
//
//    @SuppressWarnings("ConstantConditions")
//    @Override
//    public Pair<GradleProject, BuildInvocationsContainer> fetchGradleProjectWithBuildInvocationsAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
//        Preconditions.checkNotNull(transientRequestAttributes);
//        Preconditions.checkNotNull(fetchStrategy);
//
//        BuildAction<GradleProject> gradleProjectAction = BuildActionFactory.getBuildModel(GradleProject.class);
//        BuildAction<Map<String, BuildInvocations>> buildInvocationsAction = BuildActionFactory.getModelForAllProjects(BuildInvocations.class);
//        DoubleBuildAction<GradleProject, Map<String, BuildInvocations>> doubleAction = BuildActionFactory.getPairResult(gradleProjectAction, buildInvocationsAction);
//
//        final BuildActionRequest<Pair<GradleProject, Map<String, BuildInvocations>>> request = createBuildActionRequestForBuildAction(doubleAction, transientRequestAttributes);
//
//        // if values are only accessed from the cache, we can return immediately
//        if (FetchStrategy.FROM_CACHE_ONLY == fetchStrategy) {
//            @SuppressWarnings("unchecked") GradleProject gradleProject = (GradleProject) cache.getIfPresent(GradleProject.class);
//            @SuppressWarnings("unchecked") BuildInvocationsContainer buildInvocations = (BuildInvocationsContainer) cache.getIfPresent(BuildInvocationsContainer.class);
//            return new Pair<GradleProject, BuildInvocationsContainer>(gradleProject, buildInvocations);
//        }
//
//        // if values must be reloaded, we can invalidate the cache entry and then proceed as for FetchStrategy.LOAD_IF_NOT_CACHED
//        if (FetchStrategy.FORCE_RELOAD == fetchStrategy) {
//            cache.invalidate(GradleProject.class);
//            cache.invalidate(BuildInvocationsContainer.class);
//        }
//
//        // handle the case where we only load the values if not already cached
//        GradleProject gradleProject = (GradleProject) cache.getIfPresent(GradleProject.class);
//        @SuppressWarnings("unchecked") BuildInvocationsContainer buildInvocations = (BuildInvocationsContainer) cache.getIfPresent(BuildInvocationsContainer.class);
//
//        // case 1: all values cached
//        if (gradleProject != null && buildInvocations != null) {
//            return new Pair<GradleProject, BuildInvocationsContainer>(gradleProject, buildInvocations);
//        }
//        // case 2: at least one of the required values not cached
//        else {
//            Pair<GradleProject, Map<String, BuildInvocations>> result = request.executeAndWait();
//            Pair<GradleProject, BuildInvocationsContainer> convertedResult = new Pair<GradleProject, BuildInvocationsContainer>(result.getFirst(), BuildInvocationsConverter.INSTANCE.convert(result.getSecond()));
//            cache.put(GradleProject.class, convertedResult.getFirst());
//            cache.put(BuildInvocationsContainer.class, convertedResult.getSecond());
//            eventBus.post(new GradleProjectUpdateEvent(result.getFirst()));
//            eventBus.post(new BuildInvocationsUpdateEvent(convertedResult.getSecond()));
//            return convertedResult;
//        }
//
////        final AtomicReference<Pair<GradleProject, Map<String, BuildInvocations>>> result = new AtomicReference<Pair<GradleProject, Map<String, BuildInvocations>>>();
////        final CountDownLatch latch = new CountDownLatch(1);
////
////        getFromCache(GradleProject.class, new Callable<GradleProject>() {
////            @Override
////            public GradleProject call() throws InterruptedException {
////                latch.await();
////                return result.get().getFirst();
////            }
////        });
////
////        getFromCache(BuildInvocationsContainer.class, new Callable<Map<String, BuildInvocations>>() {
////            @Override
////            public Map<String, BuildInvocations> call() throws InterruptedException {
////                latch.await();
////                return result.get().getSecond();
////            }
////        });
////
////        Pair<GradleProject, Map<String, BuildInvocations>> result = executeAndWait(request, successHandler);
////        latch.countDown();
//
//    }

    private boolean targetGradleVersionIsBetween(String minVersion, String maxVersion, TransientRequestAttributes transientRequestAttributes) {
        OmniBuildEnvironment buildEnvironment = fetchBuildEnvironmentAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED);
        GradleVersion gradleVersion = GradleVersion.version(buildEnvironment.getGradle().getGradleVersion());
        return gradleVersion.getBaseVersion().compareTo(GradleVersion.version(minVersion)) >= 0 &&
                gradleVersion.getBaseVersion().compareTo(GradleVersion.version(maxVersion)) <= 0;
    }

    private <T, U> U executeRequest(final Request<T> request, final Consumer<U> newCacheEntryHandler, FetchStrategy fetchStrategy, Class<U> cacheKey, final Converter<T, U> resultConverter) {
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
                U model = executeAndWait(request, resultConverter);
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

    private <T> BuildActionRequest<T> createBuildActionRequestForBuildAction(BuildAction<T> buildAction, TransientRequestAttributes transientRequestAttributes) {
        // build the request
        BuildActionRequest<T> request = this.toolingClient.newBuildActionRequest(buildAction);
        this.fixedRequestAttributes.apply(request);
        transientRequestAttributes.apply(request);
        return request;
    }

    private <T, U> U executeAndWait(Request<T> request, Converter<T, U> resultConverter) {
        // issue the request (synchronously)
        // it is assumed that the result returned by a model request or build action request is never null
        T result = request.executeAndWait();
        return resultConverter.convert(result);
    }

}
