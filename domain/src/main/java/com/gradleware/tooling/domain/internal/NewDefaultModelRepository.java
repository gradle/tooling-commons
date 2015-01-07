package com.gradleware.tooling.domain.internal;

import com.google.common.base.Converter;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.gradleware.tooling.domain.BuildEnvironmentUpdateEvent;
import com.gradleware.tooling.domain.FetchStrategy;
import com.gradleware.tooling.domain.FixedRequestAttributes;
import com.gradleware.tooling.domain.GradleBuildUpdateEvent;
import com.gradleware.tooling.domain.NewModelRepository;
import com.gradleware.tooling.domain.TransientRequestAttributes;
import com.gradleware.tooling.domain.buildaction.BuildActionFactory;
import com.gradleware.tooling.domain.buildaction.ModelForAllProjectsBuildAction;
import com.gradleware.tooling.toolingapi.BuildActionRequest;
import com.gradleware.tooling.toolingapi.Consumer;
import com.gradleware.tooling.toolingapi.ModelRequest;
import com.gradleware.tooling.toolingapi.Request;
import com.gradleware.tooling.toolingapi.ToolingClient;
import org.gradle.tooling.BuildAction;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.gradle.GradleBuild;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Repository for Gradle build models. Model updates are broadcast via Google Guava's {@link com.google.common.eventbus.EventBus}.
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

        eventBus.register(listener);
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

        eventBus.unregister(listener);
    }

    @Override
    public BuildEnvironment fetchBuildEnvironmentAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
        Preconditions.checkNotNull(transientRequestAttributes);
        Preconditions.checkNotNull(fetchStrategy);

        ModelRequest<BuildEnvironment> request = createModelRequestForBuildModel(BuildEnvironment.class, transientRequestAttributes);
        Consumer<BuildEnvironment> successHandler = new Consumer<BuildEnvironment>() {
            @Override
            public void accept(BuildEnvironment result) {
                eventBus.post(new BuildEnvironmentUpdateEvent(result));
            }
        };

        return executeRequest(request, successHandler, fetchStrategy, BuildEnvironment.class);
    }

    @Override
    public GradleBuild fetchGradleBuildAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
        Preconditions.checkNotNull(transientRequestAttributes);
        Preconditions.checkNotNull(fetchStrategy);

        ModelRequest<GradleBuild> request = createModelRequestForBuildModel(GradleBuild.class, transientRequestAttributes);
        Consumer<GradleBuild> successHandler = new Consumer<GradleBuild>() {
            @Override
            public void accept(GradleBuild result) {
                eventBus.post(new GradleBuildUpdateEvent(result));
            }
        };

        return executeRequest(request, successHandler, fetchStrategy, GradleBuild.class);
    }

//    @Override
//    public EclipseProject fetchEclipseProjectAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
//        Preconditions.checkNotNull(transientRequestAttributes);
//        Preconditions.checkNotNull(fetchStrategy);
//
//        ModelRequest<EclipseProject> request = createModelRequestForBuildModel(EclipseProject.class, transientRequestAttributes);
//        Consumer<EclipseProject> successHandler = new Consumer<EclipseProject>() {
//            @Override
//            public void accept(EclipseProject result) {
//                eventBus.post(new EclipseProjectUpdateEvent(result));
//            }
//        };
//
//        return executeRequest(request, successHandler, fetchStrategy, EclipseProject.class);
//    }
//
//    @Override
//    public GradleProject fetchGradleProjectAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
//        Preconditions.checkNotNull(transientRequestAttributes);
//        Preconditions.checkNotNull(fetchStrategy);
//
//        ModelRequest<GradleProject> request = createModelRequestForBuildModel(GradleProject.class, transientRequestAttributes);
//        Consumer<GradleProject> successHandler = new Consumer<GradleProject>() {
//            @Override
//            public void accept(GradleProject result) {
//                eventBus.post(new GradleProjectUpdateEvent(result));
//            }
//        };
//
//        return executeRequest(request, successHandler, fetchStrategy, GradleProject.class);
//    }
//
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

    private <T> T executeRequest(final Request<T> request, final Consumer<T> successHandler, FetchStrategy fetchStrategy, Class<T> cacheKey) {
        return executeRequest(request, successHandler, fetchStrategy, cacheKey, Converter.<T>identity());
    }

    private <T, U> U executeRequest(final Request<T> request, final Consumer<U> newCacheEntryHandler, FetchStrategy fetchStrategy, Class<U> cacheKey, final Converter<T, U> resultConverter) {
        // if model is only accessed from the cache, we can return immediately
        if (FetchStrategy.FROM_CACHE_ONLY == fetchStrategy) {
            Object result = cache.getIfPresent(cacheKey);
            return cacheKey.cast(result);
        }

        // if model must be reloaded, we can invalidate the cache entry and then proceed as for FetchStrategy.LOAD_IF_NOT_CACHED
        if (FetchStrategy.FORCE_RELOAD == fetchStrategy) {
            cache.invalidate(cacheKey);
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
            Object result = cache.get(cacheKey, cacheValueLoader);
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
        ModelRequest<T> request = toolingClient.newModelRequest(model);
        fixedRequestAttributes.apply(request);
        transientRequestAttributes.apply(request);
        return request;
    }

    private <T> BuildActionRequest<Map<String, T>> createBuildActionRequestForProjectModel(Class<T> model, TransientRequestAttributes transientRequestAttributes) {
        // build the request
        ModelForAllProjectsBuildAction<T> buildAction = BuildActionFactory.getModelForAllProjects(model);
        BuildActionRequest<Map<String, T>> request = toolingClient.newBuildActionRequest(buildAction);
        fixedRequestAttributes.apply(request);
        transientRequestAttributes.apply(request);
        return request;
    }

    private <T> BuildActionRequest<T> createBuildActionRequestForBuildAction(BuildAction<T> buildAction, TransientRequestAttributes transientRequestAttributes) {
        // build the request
        BuildActionRequest<T> request = toolingClient.newBuildActionRequest(buildAction);
        fixedRequestAttributes.apply(request);
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
