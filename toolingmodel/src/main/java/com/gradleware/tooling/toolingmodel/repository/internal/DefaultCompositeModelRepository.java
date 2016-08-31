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

import java.util.Map;

import org.gradle.tooling.connection.ModelResults;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;

import com.gradleware.tooling.toolingclient.CompositeBuildModelRequest;
import com.gradleware.tooling.toolingclient.Consumer;
import com.gradleware.tooling.toolingclient.Request;
import com.gradleware.tooling.toolingclient.ToolingClient;
import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.Path;
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

    private final FixedRequestAttributes requestAttribute;

    public DefaultCompositeModelRepository(FixedRequestAttributes requestAttribute, ToolingClient toolingClient, EventBus eventBus) {
        super(toolingClient, eventBus);
        this.requestAttribute = Preconditions.checkNotNull(requestAttribute);
    }

    @Override
    public ModelResults<OmniEclipseProject> fetchEclipseProjects(final TransientRequestAttributes transientAttributes, FetchStrategy fetchStrategy) {
        CompositeBuildModelRequest<EclipseProject> modelRequest = createModelRequest(EclipseProject.class, this.requestAttribute, transientAttributes);
        final Map<Path, DefaultOmniEclipseProject> knownEclipseProjects = Maps.newHashMap();
        final Map<Path, DefaultOmniGradleProject> knownGradleProjects = Maps.newHashMap();
        Converter<EclipseProject, OmniEclipseProject> converter = new BaseConverter<EclipseProject, OmniEclipseProject>() {

            @Override
            public OmniEclipseProject apply(EclipseProject eclipseProject) {
                return DefaultOmniEclipseProject.from(eclipseProject, knownEclipseProjects, knownGradleProjects);
            }
        };
        return executeRequest(modelRequest, fetchStrategy, OmniEclipseProject.class, converter);
    }

    @Override
    public ModelResults<OmniBuildEnvironment> fetchBuildEnvironments(TransientRequestAttributes transientAttributes, FetchStrategy fetchStrategy) {
        CompositeBuildModelRequest<BuildEnvironment> modelRequest = createModelRequest(BuildEnvironment.class, this.requestAttribute, transientAttributes);
        Converter<BuildEnvironment, OmniBuildEnvironment> converter = new BaseConverter<BuildEnvironment, OmniBuildEnvironment>() {

            @Override
            public OmniBuildEnvironment apply(BuildEnvironment buildEnvironment) {
                return DefaultOmniBuildEnvironment.from(buildEnvironment);
            }
        };
        return executeRequest(modelRequest, fetchStrategy, OmniBuildEnvironment.class, converter);
    }

    private <T> CompositeBuildModelRequest<T> createModelRequest(Class<T> model, FixedRequestAttributes fixedAttribute, TransientRequestAttributes transientAttributes) {
        CompositeBuildModelRequest<T> request = getToolingClient().newCompositeModelRequest(model);
        fixedAttribute.apply(request);
        transientAttributes.apply(request);
        return request;
    }

    protected <T, U> ModelResults<U> executeRequest(Request<ModelResults<T>> request, FetchStrategy fetchStrategy, Class<?> cacheKey, Converter<T, U> resultConverter) {
        Consumer<ModelResults<U>> dontSendEvents = new Consumer<ModelResults<U>>() {

            @Override
            public void accept(ModelResults<U> input) {
            }
        };
        return super.executeRequest(request, dontSendEvents, fetchStrategy, cacheKey, new ModelResultsConverter<T, U>(resultConverter));
    }
}
