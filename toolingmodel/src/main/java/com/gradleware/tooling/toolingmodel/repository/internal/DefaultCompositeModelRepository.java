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

import java.util.HashMap;
import java.util.Set;

import org.gradle.tooling.connection.ModelResult;
import org.gradle.tooling.connection.ModelResults;
import org.gradle.tooling.model.BuildIdentifier;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.util.GradleVersion;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;

import com.gradleware.tooling.toolingclient.CompositeBuildModelRequest;
import com.gradleware.tooling.toolingclient.Consumer;
import com.gradleware.tooling.toolingclient.Request;
import com.gradleware.tooling.toolingclient.ToolingClient;
import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.repository.CompositeBuildModelRepository;
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
        this.requestAttributes = ImmutableSet.copyOf(requestAttributes);
    }

    @Override
    public ModelResults<OmniEclipseProject> fetchEclipseProjects(final TransientRequestAttributes transientAttributes, FetchStrategy fetchStrategy) {
        final HashMap<BuildIdentifier, OmniBuildEnvironment> buildEnvironmentsById = getBuildEnvironmentsById(transientAttributes, FetchStrategy.FORCE_RELOAD);
        CompositeBuildModelRequest<EclipseProject> modelRequest = createModelRequest(EclipseProject.class, this.requestAttributes, transientAttributes);
        Converter<EclipseProject, OmniEclipseProject> converter = new BaseConverter<EclipseProject, OmniEclipseProject>() {

            @Override
            public OmniEclipseProject apply(EclipseProject eclipseProject) {
                OmniBuildEnvironment buildEnvironment = buildEnvironmentsById.get(eclipseProject.getGradleProject().getProjectIdentifier().getBuildIdentifier());
                return DefaultOmniEclipseProject.from(eclipseProject, isPublicFixRequired(buildEnvironment));
            }
        };
        return executeRequest(modelRequest, fetchStrategy, OmniEclipseProject.class, converter);
    }

    @Override
    public ModelResults<OmniBuildEnvironment> fetchBuildEnvironments(TransientRequestAttributes transientAttributes, FetchStrategy fetchStrategy) {
        CompositeBuildModelRequest<BuildEnvironment> modelRequest = createModelRequest(BuildEnvironment.class, this.requestAttributes, transientAttributes);
        Converter<BuildEnvironment, OmniBuildEnvironment> converter = new BaseConverter<BuildEnvironment, OmniBuildEnvironment>() {

            @Override
            public OmniBuildEnvironment apply(BuildEnvironment buildEnvironment) {
                return DefaultOmniBuildEnvironment.from(buildEnvironment);
            }
        };
        return executeRequest(modelRequest, fetchStrategy, OmniBuildEnvironment.class, converter);
    }

    private <T> CompositeBuildModelRequest<T> createModelRequest(Class<T> model, Set<FixedRequestAttributes> fixedAttributes, TransientRequestAttributes transientAttributes) {
        CompositeBuildModelRequest<T> request = getToolingClient().newCompositeModelRequest(model);
        for (FixedRequestAttributes fixedRequestAttribute : fixedAttributes) {
            fixedRequestAttribute.apply(request);
        }
        transientAttributes.apply(request);
        return request;
    }

    protected <T, U> ModelResults<U> executeRequest(Request<ModelResults<T>> request, FetchStrategy fetchStrategy, Class<?> cacheKey, Converter<T, U> resultConverter) {
        // TODO push this special handling down to DefaultToolingClient
        if (this.requestAttributes.isEmpty()) {
            return new EmptyModelResults<U>();
        }
        Consumer<ModelResults<U>> dontSendEvents = new Consumer<ModelResults<U>>() {

            @Override
            public void accept(ModelResults<U> input) {
            }
        };
        return super.executeRequest(request, dontSendEvents, fetchStrategy, cacheKey, new ModelResultsConverter<T, U>(resultConverter));
    }

    private HashMap<BuildIdentifier, OmniBuildEnvironment> getBuildEnvironmentsById(final TransientRequestAttributes transientAttributes, FetchStrategy fetchStrategy) {
        ModelResults<OmniBuildEnvironment> buildEnvironments = fetchBuildEnvironments(transientAttributes, fetchStrategy);
        final HashMap<BuildIdentifier, OmniBuildEnvironment> buildEnvironmentsById = Maps.newHashMap();
        for (ModelResult<OmniBuildEnvironment> modelResult : buildEnvironments) {
            if (modelResult.getFailure() == null) {
                buildEnvironmentsById.put(modelResult.getModel().getBuildIdentifier(), modelResult.getModel());
            }
        }
        return buildEnvironmentsById;
    }

    private boolean isPublicFixRequired(OmniBuildEnvironment buildEnvironment) {
        if (buildEnvironment == null) {
            return false;
        } else {
            return targetGradleVersionIsBetween("2.1", "2.2.1", buildEnvironment);
        }
    }

    private boolean targetGradleVersionIsBetween(String minVersion, String maxVersion, OmniBuildEnvironment buildEnvironment) {
        GradleVersion gradleVersion = GradleVersion.version(buildEnvironment.getGradle().getGradleVersion());
        return gradleVersion.getBaseVersion().compareTo(GradleVersion.version(minVersion)) >= 0 && gradleVersion.getBaseVersion().compareTo(GradleVersion.version(maxVersion)) <= 0;
    }
}
