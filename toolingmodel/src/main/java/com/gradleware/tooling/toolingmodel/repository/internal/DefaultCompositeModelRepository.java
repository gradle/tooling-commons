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

import java.util.List;

import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingclient.ModelRequest;
import com.gradleware.tooling.toolingclient.ToolingClient;
import com.gradleware.tooling.toolingmodel.OmniEclipseWorkspace;
import com.gradleware.tooling.toolingmodel.repository.CompositeModelRepository;
import com.gradleware.tooling.toolingmodel.repository.Environment;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

public class DefaultCompositeModelRepository implements CompositeModelRepository {

    private ImmutableList<FixedRequestAttributes> requestAttributes;
    private ToolingClient toolingClient;
    private Environment environment;

    public DefaultCompositeModelRepository(List<FixedRequestAttributes> requestAttributes, ToolingClient toolingClient, Environment environment) {
        if (requestAttributes.size() != 1) {
            throw new IllegalArgumentException("Composite builds can currently contain exactly one project");
        }
        this.requestAttributes = ImmutableList.copyOf(requestAttributes);
        this.toolingClient = Preconditions.checkNotNull(toolingClient);
        this.environment = Preconditions.checkNotNull(environment);
    }

    @Override
    public OmniEclipseWorkspace fetchEclipseWorkspace(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
        ModelRequest<EclipseProject> modelRequest = this.toolingClient.newModelRequest(EclipseProject.class);
        this.requestAttributes.get(0).apply(modelRequest);
        transientRequestAttributes.apply(modelRequest);
        EclipseProject eclipseProject = modelRequest.executeAndWait();
        return DefaultOmniEclipseWorkspace.from(DefaultOmniEclipseProject.from(eclipseProject, false));
    }

}
