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
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.gradle.tooling.connection.FailedModelResult;
import org.gradle.tooling.connection.ModelResult;
import org.gradle.tooling.connection.ModelResults;
import org.gradle.tooling.internal.connection.DefaultBuildIdentifier;
import org.gradle.tooling.model.BuildIdentifier;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.util.GradleVersion;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;

import com.gradleware.tooling.toolingclient.CompositeBuildModelRequest;
import com.gradleware.tooling.toolingclient.Consumer;
import com.gradleware.tooling.toolingclient.GradleBuildIdentifier;
import com.gradleware.tooling.toolingclient.GradleDistribution;
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
        this.requestAttributes = ImmutableSet.copyOf(requestAttributes);
    }

    @Override
    public OmniEclipseWorkspace fetchEclipseWorkspace(final TransientRequestAttributes transientAttributes, FetchStrategy fetchStrategy) {
        //TODO push this special handling down to DefaultToolingClient
        if (this.requestAttributes.isEmpty()) {
            return DefaultOmniEclipseWorkspace.from(Collections.<GradleBuildIdentifier, OmniEclipseProject>emptyMap(), Collections.<GradleBuildIdentifier, Exception>emptyMap());
        }
        final OmniBuildEnvironments buildEnvironments = fetchBuildEnvironments(transientAttributes, fetchStrategy);
        CompositeBuildModelRequest<EclipseProject> modelRequest = createModelRequest(EclipseProject.class, this.requestAttributes, transientAttributes);
        Consumer<OmniEclipseWorkspace> successHandler = new Consumer<OmniEclipseWorkspace>() {

            @Override
            public void accept(OmniEclipseWorkspace result) {
                postEvent(new EclipseWorkspaceUpdateEvent(result));
            }
        };
        Converter<ModelResults<EclipseProject>, OmniEclipseWorkspace> converter = new BaseConverter<ModelResults<EclipseProject>, OmniEclipseWorkspace>() {

            @Override
            public OmniEclipseWorkspace apply(ModelResults<EclipseProject> results) {
                Map<GradleBuildIdentifier, OmniEclipseProject> models = Maps.newHashMap();
                Map<GradleBuildIdentifier, Exception> failures = Maps.newHashMap();
                  for (ModelResult<EclipseProject> result : results) {
                      if (result.getFailure() == null) {
                          boolean isPublicFixRequired = isPublicFixRequired(buildEnvironments, result.getModel());
                          GradleBuildIdentifier identifier = createBuildIdentifier(result.getModel().getGradleProject().getProjectIdentifier().getBuildIdentifier());
                          DefaultOmniEclipseProject model = DefaultOmniEclipseProject.from(result.getModel(), isPublicFixRequired);
                          models.put(identifier, model);
                      } else {
                          @SuppressWarnings("unchecked")
                          FailedModelResult<EclipseProject> failureResult = (FailedModelResult<EclipseProject>) result.getFailure();
                          GradleBuildIdentifier identifier = createBuildIdentifier(failureResult.getBuildIdentifier());
                          Exception exception = failureResult.getFailure();
                          failures.put(identifier, exception);
                      }
                  }
                return DefaultOmniEclipseWorkspace.from(models, failures);
            }
        };
        return executeRequest(modelRequest, successHandler, fetchStrategy, OmniEclipseWorkspace.class, converter);
    }

    private static GradleBuildIdentifier createBuildIdentifier(BuildIdentifier buildIdentifier) {
        try {
            Field field = DefaultBuildIdentifier.class.getDeclaredField("rootDir");
            field.setAccessible(true);
            File rootDir = (File) field.get(buildIdentifier);
            return new GradleBuildIdentifier(rootDir, GradleDistribution.fromBuild());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private OmniBuildEnvironments fetchBuildEnvironments(TransientRequestAttributes transientAttributes, FetchStrategy fetchStrategy) {
        CompositeBuildModelRequest<BuildEnvironment> modelRequest = createModelRequest(BuildEnvironment.class, this.requestAttributes, transientAttributes);
        Consumer<OmniBuildEnvironments> newCacheEntryHandler = new Consumer<OmniBuildEnvironments>() {

            @Override
            public void accept(OmniBuildEnvironments input) {
            }
        };
        Converter<ModelResults<BuildEnvironment>, OmniBuildEnvironments> resultConverter = new BaseConverter<ModelResults<BuildEnvironment>, DefaultCompositeModelRepository.OmniBuildEnvironments>() {

            @Override
            public OmniBuildEnvironments apply(ModelResults<BuildEnvironment> input) {
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
        Optional<OmniBuildEnvironment> buildEnvironment = buildEnvironments.get(eclipseProject.getGradleProject().getProjectIdentifier().getBuildIdentifier());
        return buildEnvironment.isPresent() ? targetGradleVersionIsBetween("2.1", "2.2.1", buildEnvironment.get()) : false;
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

        private Map<BuildIdentifier, OmniBuildEnvironment> models;

        public OmniBuildEnvironments(Map<BuildIdentifier, OmniBuildEnvironment> models) {
                this.models = models;
        }

        public Optional<OmniBuildEnvironment> get(BuildIdentifier identifier) {
            return Optional.fromNullable(this.models.get(identifier));
        }

        public static OmniBuildEnvironments from(ModelResults<BuildEnvironment> environments) {
            Map<BuildIdentifier, OmniBuildEnvironment> models = Maps.newHashMap();
            for (ModelResult<BuildEnvironment> buildEnvironment : environments) {
                if (buildEnvironment.getFailure() == null) {
                    BuildIdentifier identifier = buildEnvironment.getModel().getBuildIdentifier();
                    models.put(identifier, DefaultOmniBuildEnvironment.from(buildEnvironment.getModel()));
                }
            }
            return new OmniBuildEnvironments(models);
        }

    }

}
