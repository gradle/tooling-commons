package com.gradleware.tooling.domain;

import com.gradleware.tooling.domain.model.OmniBuildEnvironment;
import com.gradleware.tooling.domain.model.OmniGradleBuildStructure;
import org.gradle.tooling.model.GradleProject;

/**
 * Repository for Gradle build models. Listeners can be registered to get notified about model updates. It is left to the implementation through which channel the events are
 * broadcast.
 */
public interface NewModelRepository {

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
     * Fetches the {@link OmniBuildEnvironment} synchronously and broadcasts it through a {@link NewBuildEnvironmentUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the build environment, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    OmniBuildEnvironment fetchBuildEnvironmentAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

    /**
     * Fetches the {@link OmniGradleBuildStructure} synchronously and broadcasts it through a {@link NewGradleBuildStructureUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the gradle build, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    OmniGradleBuildStructure fetchGradleBuildAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

//    /**
//     * Fetches the {@link org.gradle.tooling.model.eclipse.EclipseProject} synchronously and broadcasts it through a {@link com.gradleware.tooling.domain.EclipseProjectUpdateEvent}.
//     *
//     * @param transientRequestAttributes the transient request attributes
//     * @param fetchStrategy the fetch strategy
//     * @return the eclipse project, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
//     */
//    EclipseProject fetchEclipseProjectAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);
//
    /**
     * Fetches the {@link org.gradle.tooling.model.GradleProject} synchronously and broadcasts it through a {@link com.gradleware.tooling.domain.GradleProjectUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the gradle project, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    GradleProject fetchGradleProjectAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

//    /**
//     * Fetches the {@link org.gradle.tooling.model.gradle.BuildInvocations} synchronously and broadcasts them through a {@link com.gradleware.tooling.domain.BuildInvocationsUpdateEvent}.
//     *
//     * @param transientRequestAttributes the transient request attributes
//     * @param fetchStrategy the fetch strategy
//     * @return the build invocations, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
//     */
//    BuildInvocationsContainer fetchBuildInvocationsAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);
//
//    /**
//     * Fetches the {@link org.gradle.tooling.model.GradleProject} and {@link org.gradle.tooling.model.gradle.BuildInvocations} synchronously and broadcasts them through a {@link com.gradleware.tooling.domain.GradleProjectUpdateEvent} and {@link
//     * com.gradleware.tooling.domain.BuildInvocationsUpdateEvent}.
//     *
//     * @param transientRequestAttributes the transient request attributes
//     * @param fetchStrategy the fetch strategy
//     * @return the gradle project and build invocations, pair values never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
//     */
//    Pair<GradleProject, BuildInvocationsContainer> fetchGradleProjectWithBuildInvocationsAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

}
