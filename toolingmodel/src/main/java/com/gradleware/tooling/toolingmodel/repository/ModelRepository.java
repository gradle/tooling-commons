package com.gradleware.tooling.toolingmodel.repository;

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniBuildInvocationsContainer;
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure;

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
     * Fetches the {@link OmniBuildEnvironment} synchronously and broadcasts it through a {@link BuildEnvironmentUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the build environment, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    OmniBuildEnvironment fetchBuildEnvironmentAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

    /**
     * Fetches the {@link OmniGradleBuildStructure} synchronously and broadcasts it through a {@link GradleBuildStructureUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the gradle build, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    OmniGradleBuildStructure fetchGradleBuildStructureAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

    /**
     * Fetches the {@link OmniGradleBuild} synchronously and broadcasts it through a {@link GradleBuildUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the gradle project, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    OmniGradleBuild fetchGradleBuildAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

    /**
     * Fetches the {@link OmniEclipseGradleBuild} synchronously and broadcasts it through a {@link EclipseGradleBuildUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the eclipse project, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    OmniEclipseGradleBuild fetchEclipseGradleBuildAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

    /**
     * Fetches the {@link OmniBuildInvocationsContainer} synchronously and broadcasts it through a {@link BuildInvocationsUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the build invocations container, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    OmniBuildInvocationsContainer fetchBuildInvocationsAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);
//
//    /**
//     * Fetches the {@link org.gradle.tooling.model.GradleProject} and {@link org.gradle.tooling.model.gradle.BuildInvocations} synchronously and broadcasts them through a {@link com.gradleware.tooling.toolingmodel.repository.GradleProjectUpdateEvent} and {@link
//     * com.gradleware.tooling.toolingmodel.repository.BuildInvocationsUpdateEvent}.
//     *
//     * @param transientRequestAttributes the transient request attributes
//     * @param fetchStrategy the fetch strategy
//     * @return the gradle project and build invocations, pair values never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
//     */
//    Pair<GradleProject, BuildInvocationsContainer> fetchGradleProjectWithBuildInvocationsAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

}
