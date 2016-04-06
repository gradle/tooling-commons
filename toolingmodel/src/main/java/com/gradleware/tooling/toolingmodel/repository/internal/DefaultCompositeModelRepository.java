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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gradle.tooling.model.BuildIdentifier;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.util.GradleVersion;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;

import com.gradleware.tooling.toolingclient.CompositeBuildModelRequest;
import com.gradleware.tooling.toolingclient.Consumer;
import com.gradleware.tooling.toolingclient.ToolingClient;
import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniEclipseWorkspace;
import com.gradleware.tooling.toolingmodel.repository.CompositeBuildModelRepository;
import com.gradleware.tooling.toolingmodel.repository.EclipseWorkspaceUpdateEvent;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

/**
 * Default implementation for {@link CompositeBuildModelRepository}.
 *
 * @author Stefan Oehme
 */
public class DefaultCompositeModelRepository extends BaseModelRepository implements CompositeBuildModelRepository {

    private final ImmutableSet<FixedRequestAttributes> requestAttributes;

    public DefaultCompositeModelRepository(Set<FixedRequestAttributes> requestAttributes, ToolingClient toolingClient, EventBus eventBus) {
        super(toolingClient, eventBus);
        Preconditions.checkArgument(requestAttributes.size() > 0, "Composite builds need at least one participant");
        this.requestAttributes = ImmutableSet.copyOf(requestAttributes);
    }

    @Override
    public OmniEclipseWorkspace fetchEclipseWorkspace(final TransientRequestAttributes transientAttributes, FetchStrategy fetchStrategy) {
        final OmniBuildEnvironments buildEnvironments = fetchBuildEnvironments(transientAttributes, fetchStrategy);
        CompositeBuildModelRequest<EclipseProject> modelRequest = createModelRequest(EclipseProject.class, this.requestAttributes, transientAttributes);
        Consumer<OmniEclipseWorkspace> successHandler = new Consumer<OmniEclipseWorkspace>() {

            @Override
            public void accept(OmniEclipseWorkspace result) {
                postEvent(new EclipseWorkspaceUpdateEvent(result));
            }
        };
        Converter<Set<EclipseProject>, OmniEclipseWorkspace> converter = new BaseConverter<Set<EclipseProject>, OmniEclipseWorkspace>() {

            @Override
            public OmniEclipseWorkspace apply(Set<EclipseProject> eclipseProjects) {
                List<OmniEclipseProject> omniEclipseProjects = FluentIterable.from(eclipseProjects).transform(new Function<EclipseProject, OmniEclipseProject>() {

                    @Override
                    public OmniEclipseProject apply(EclipseProject eclipseProject) {
                        boolean isPublicFixRequired = isPublicFixRequired(buildEnvironments, eclipseProject);
                        return DefaultOmniEclipseProject.from(eclipseProject, isPublicFixRequired);
                    }
                }).toList();
                return DefaultOmniEclipseWorkspace.from(omniEclipseProjects);
            }
        };
        return executeRequest(modelRequest, successHandler, fetchStrategy, OmniEclipseWorkspace.class, converter);
    }

    private OmniBuildEnvironments fetchBuildEnvironments(TransientRequestAttributes transientAttributes, FetchStrategy fetchStrategy) {
        CompositeBuildModelRequest<BuildEnvironment> modelRequest = createModelRequest(BuildEnvironment.class, this.requestAttributes, transientAttributes);
        Consumer<OmniBuildEnvironments> newCacheEntryHandler = new Consumer<OmniBuildEnvironments>() {

            @Override
            public void accept(OmniBuildEnvironments input) {
            }
        };
        Converter<Set<BuildEnvironment>, OmniBuildEnvironments> resultConverter = new BaseConverter<Set<BuildEnvironment>, DefaultCompositeModelRepository.OmniBuildEnvironments>() {

            @Override
            public OmniBuildEnvironments apply(Set<BuildEnvironment> input) {
                return OmniBuildEnvironments.from(input);
            }
        };
        return executeRequest(modelRequest, newCacheEntryHandler, fetchStrategy, OmniBuildEnvironments.class, resultConverter);
    }

    private <T> CompositeBuildModelRequest<T> createModelRequest(Class<T> model, Set<FixedRequestAttributes> fixedAttributes, TransientRequestAttributes transientAttributes) {
        CompositeBuildModelRequest<T> request = getToolingClient().newCompositeModelRequest(model);
        for (FixedRequestAttributes fixedRequestAttribute : fixedAttributes) {
            fixedRequestAttribute.apply(request);
        }
        transientAttributes.apply(request);
        return request;
    }

    boolean isPublicFixRequired(OmniBuildEnvironments buildEnvironments, EclipseProject eclipseProject) {
        OmniBuildEnvironment buildEnvironment = buildEnvironments.get(eclipseProject.getGradleProject().getProjectIdentifier().getBuildIdentifier());
        return targetGradleVersionIsBetween("2.1", "2.2.1", buildEnvironment);
    }

    private boolean targetGradleVersionIsBetween(String minVersion, String maxVersion, OmniBuildEnvironment buildEnvironment) {
        GradleVersion gradleVersion = GradleVersion.version(buildEnvironment.getGradle().getGradleVersion());
        return gradleVersion.getBaseVersion().compareTo(GradleVersion.version(minVersion)) >= 0 &&
                gradleVersion.getBaseVersion().compareTo(GradleVersion.version(maxVersion)) <= 0;
    }

    /**
     * A container for {@link OmniBuildEnvironment} models in a composite.
     * @author Stefan Oehme
     */
    private static class OmniBuildEnvironments {

        public static OmniBuildEnvironments from(Set<BuildEnvironment> environments) {
            Map<BuildIdentifier, OmniBuildEnvironment>environmentsByBuild = Maps.newHashMap();
            for (BuildEnvironment buildEnvironment : environments) {
                environmentsByBuild.put(buildEnvironment.getBuildIdentifier(), DefaultOmniBuildEnvironment.from(buildEnvironment));
            }
            return new OmniBuildEnvironments(environmentsByBuild);
        }

        private final Map<BuildIdentifier, OmniBuildEnvironment> environmentsByBuild;

        private OmniBuildEnvironments(Map<BuildIdentifier, OmniBuildEnvironment> environmentsByBuild) {
            this.environmentsByBuild = environmentsByBuild;
        }

        public OmniBuildEnvironment get(BuildIdentifier build) {
            return this.environmentsByBuild.get(build);
        }
    }

}
