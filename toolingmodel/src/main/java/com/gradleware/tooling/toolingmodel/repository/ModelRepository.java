package com.gradleware.tooling.toolingmodel.repository;

import com.gradleware.tooling.toolingmodel.util.Pair;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.gradle.BuildInvocations;
import org.gradle.tooling.model.gradle.GradleBuild;

/**
 * Repository for Gradle build models. Listeners can be registered to get notified about model updates. It is left to the implementation through which channel the events are
 * broadcast.
 */
public interface ModelRepository {

    /**
     * Registers the given {@code listener} to receive model change events.
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
     * Fetches the {@link BuildEnvironment} synchronously and broadcasts it through a {@link BuildEnvironmentUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the build environment, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    BuildEnvironment fetchBuildEnvironmentAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

    /**
     * Fetches the {@link GradleBuild} synchronously and broadcasts it through a {@link GradleBuildUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the gradle build, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    GradleBuild fetchGradleBuildAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

    /**
     * Fetches the {@link EclipseProject} synchronously and broadcasts it through a {@link EclipseProjectUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the eclipse project, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    EclipseProject fetchEclipseProjectAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

    /**
     * Fetches the {@link GradleProject} synchronously and broadcasts it through a {@link GradleProjectUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the gradle project, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    GradleProject fetchGradleProjectAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

    /**
     * Fetches the {@link BuildInvocations} synchronously and broadcasts them through a {@link BuildInvocationsUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the build invocations, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    BuildInvocationsContainer fetchBuildInvocationsAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

    /**
     * Fetches the {@link GradleProject} and {@link BuildInvocations} synchronously and broadcasts them through a {@link GradleProjectUpdateEvent} and {@link
     * BuildInvocationsUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the gradle project and build invocations, pair values never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    Pair<GradleProject, BuildInvocationsContainer> fetchGradleProjectWithBuildInvocationsAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

}
