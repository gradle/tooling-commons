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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.util.GradleVersion;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;

import com.gradleware.tooling.toolingclient.CompositeModelRequest;
import com.gradleware.tooling.toolingclient.Consumer;
import com.gradleware.tooling.toolingclient.ToolingClient;
import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniEclipseWorkspace;
import com.gradleware.tooling.toolingmodel.repository.CompositeModelRepository;
import com.gradleware.tooling.toolingmodel.repository.EclipseWorkspaceUpdateEvent;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.ModelRepositoryProvider;
import com.gradleware.tooling.toolingmodel.repository.SimpleModelRepository;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

/**
 * Default implementation for {@link CompositeModelRepository}.
 *
 * @author Stefan Oehme
 */
public class DefaultCompositeModelRepository extends BaseModelRepository implements CompositeModelRepository {

    private final ModelRepositoryProvider modelRepositoryProvider;
    private final ImmutableList<FixedRequestAttributes> requestAttributes;

    public DefaultCompositeModelRepository(ModelRepositoryProvider modelRepositoryProvider, List<FixedRequestAttributes> requestAttributes, ToolingClient toolingClient, EventBus eventBus) {
        super(toolingClient, eventBus);
        this.modelRepositoryProvider = Preconditions.checkNotNull(modelRepositoryProvider);
        Preconditions.checkArgument(requestAttributes.size() > 0, "Composite builds need at least one participant");
        this.requestAttributes = ImmutableList.copyOf(requestAttributes);
    }

    @Override
    public OmniEclipseWorkspace fetchEclipseWorkspace(final TransientRequestAttributes transientAttributes, FetchStrategy fetchStrategy) {
        CompositeModelRequest<EclipseProject> modelRequest = createModelRequest(EclipseProject.class, this.requestAttributes, transientAttributes);
        Consumer<OmniEclipseWorkspace> successHandler = new Consumer<OmniEclipseWorkspace>() {

            @Override
            public void accept(OmniEclipseWorkspace result) {
                postEvent(new EclipseWorkspaceUpdateEvent(result));
            }
        };
        Converter<Set<EclipseProject>, OmniEclipseWorkspace> converter = new BaseConverter<Set<EclipseProject>, OmniEclipseWorkspace>() {

            @Override
            public OmniEclipseWorkspace apply(Set<EclipseProject> eclipseProjects) {
                // for each project investigate if the isPublic fix is required
                Map<EclipseProject, Boolean> isPublicFixRequiredForProjects = Maps.newHashMap();
                for (EclipseProject project : eclipseProjects) {
                    for (FixedRequestAttributes attribute : DefaultCompositeModelRepository.this.requestAttributes) {
                        if (eclipseProjectIsSubProjectOf(attribute.getProjectDir(), project)) {
                            Boolean isPublicFixRequired = targetGradleVersionIsBetween("2.1", "2.2.1", attribute, transientAttributes);
                            isPublicFixRequiredForProjects.put(project, isPublicFixRequired);
                        }
                    }
                }
                return DefaultOmniEclipseWorkspace.from(eclipseProjects, isPublicFixRequiredForProjects);
            }
        };
        return executeRequest(modelRequest, successHandler, fetchStrategy, OmniEclipseWorkspace.class, converter);
    }

    private <T> CompositeModelRequest<T> createModelRequest(Class<T> model, List<FixedRequestAttributes> fixedAttributes, TransientRequestAttributes transientAttributes) {
        CompositeModelRequest<T> request = getToolingClient().newCompositeModelRequest(model);
        for (FixedRequestAttributes fixedRequestAttribute : fixedAttributes) {
            fixedRequestAttribute.apply(request);
        }
        transientAttributes.apply(request);
        return request;
    }

    private boolean targetGradleVersionIsBetween(String minVersion, String maxVersion, FixedRequestAttributes fixedAttributes, TransientRequestAttributes transientRequestAttributes) {
        SimpleModelRepository simpleRepo = modelRepositoryProvider.getModelRepository(fixedAttributes);
        OmniBuildEnvironment buildEnvironment = simpleRepo.fetchBuildEnvironment(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED);
        GradleVersion gradleVersion = GradleVersion.version(buildEnvironment.getGradle().getGradleVersion());
        return gradleVersion.getBaseVersion().compareTo(GradleVersion.version(minVersion)) >= 0 &&
                gradleVersion.getBaseVersion().compareTo(GradleVersion.version(maxVersion)) <= 0;
    }

    public static boolean eclipseProjectIsSubProjectOf(final File rootProjectDir, EclipseProject candidate) {
        try {
            File canonicalRootProjectDir = rootProjectDir.getCanonicalFile();
            return rootEclipseProject(candidate).getProjectDirectory().getCanonicalFile().equals(canonicalRootProjectDir);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static EclipseProject rootEclipseProject(EclipseProject project) {
        EclipseProject parent = project.getParent();
        return parent == null ? project : rootEclipseProject(parent);
    }

}
