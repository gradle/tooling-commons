/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gradleware.tooling.toolingclient.internal.deduplication;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ResultHandler;
import org.gradle.tooling.connection.GradleConnection;
import org.gradle.tooling.connection.ModelResult;
import org.gradle.tooling.connection.ModelResults;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * De-duplicates {@link EclipseProject} names.
 *
 * @author Stefan Oehme
 */
public class DeduplicatingGradleConnection implements GradleConnection {

    private final GradleConnection delegate;

    public DeduplicatingGradleConnection(GradleConnection delegate) {
        this.delegate = delegate;
    }

    @Override
    public <T> ModelResults<T> getModels(Class<T> modelType) throws GradleConnectionException, IllegalStateException, IllegalArgumentException {
        return models(modelType).get();
    }

    @Override
    public <T> void getModels(Class<T> modelType, ResultHandler<? super ModelResults<T>> handler) throws IllegalStateException {
        models(modelType).get(handler);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <T> ModelBuilder<ModelResults<T>> models(Class<T> modelType) throws GradleConnectionException, IllegalStateException, IllegalArgumentException {
        ModelBuilder<ModelResults<T>> models = this.delegate.models(modelType);
        if (modelType == EclipseProject.class) {
            DeduplicatingEclipseModelBuilder builder = new DeduplicatingEclipseModelBuilder((ModelBuilder) models);
            return (ModelBuilder) builder;
        } else {
            return models;
        }
    }

    @Override
    public BuildLauncher newBuild() {
        return this.delegate.newBuild();
    }


    @Override
    public void close() {
        this.delegate.close();
    }

    /**
     * Deduplicates {@link EclipseProject}s returned from the delegate.
     */
    private static class DeduplicatingEclipseModelBuilder extends DelegatingModelBuilder<ModelResults<EclipseProject>> {

        public DeduplicatingEclipseModelBuilder(ModelBuilder<ModelResults<EclipseProject>> delegate) {
            super(delegate);
        }

        @Override
        public ModelResults<EclipseProject> get() throws GradleConnectionException, IllegalStateException {
            ModelResults<EclipseProject> result = super.get();
            return deduplicate(result);
        }

        @Override
        public void get(final ResultHandler<? super ModelResults<EclipseProject>> handler) throws IllegalStateException {
            this.delegate.get(new DeduplicatingResultHandler(handler));
        }

        private ModelResults<EclipseProject> deduplicate(ModelResults<EclipseProject> results) {
            return new DeduplicatedModelResults(results);
        }

        /**
         * Deduplicates the {@link EclipseProject}s contained in the delegate.
         * @author Stefan Oehme
         */
        private static final class DeduplicatedModelResults implements ModelResults<EclipseProject> {

            private final ModelResults<EclipseProject> delegate;

            public DeduplicatedModelResults(ModelResults<EclipseProject> delegate) {
                this.delegate = delegate;
            }

            @Override
            public Iterator<ModelResult<EclipseProject>> iterator() {
                Set<EclipseProject> projects = Sets.newHashSet();
                for (ModelResult<EclipseProject> result : this.delegate) {
                    if (result.getFailure() == null) {
                        projects.add(result.getModel());
                    }
                }
                Set<EclipseProject> deduplicatedProjects = new EclipseProjectDeduplicator().deduplicate(projects);
                Map<File, EclipseProject> deduplicatedProjectsByProjectDir = Maps.newHashMap();
                for (EclipseProject deduplicatedProject : deduplicatedProjects) {
                    deduplicatedProjectsByProjectDir.put(deduplicatedProject.getProjectDirectory(), deduplicatedProject);
                }

                Set<ModelResult<EclipseProject>> deduplicatedResults = Sets.newHashSet();
                for (ModelResult<EclipseProject> result : this.delegate) {
                    if (result.getFailure() == null) {
                        EclipseProject deduplicatedProject = deduplicatedProjectsByProjectDir.get(result.getModel().getProjectDirectory());
                        deduplicatedResults.add(new DeduplicatedModelResult(result, deduplicatedProject));
                    } else {
                        deduplicatedResults.add(result);
                    }
                }
                return deduplicatedResults.iterator();
            }

        }

        /**
         * Returns the deduplicated {@link EclipseProject} instead of the original model.
         * @author Stefan Oehme
         */
        private static final class DeduplicatedModelResult implements ModelResult<EclipseProject> {

            private final ModelResult<EclipseProject> result;
            private final EclipseProject deduplicatedProject;

            DeduplicatedModelResult(ModelResult<EclipseProject> result, EclipseProject deduplicatedProject) {
                this.result = result;
                this.deduplicatedProject = deduplicatedProject;
            }

            @Override
            public EclipseProject getModel() throws GradleConnectionException {
                return this.deduplicatedProject;
            }

            @Override
            public GradleConnectionException getFailure() {
                return this.result.getFailure();
            }
        }

        /**
         * Deduplicates the {@link EclipseProject}s before passing them on to the actual handler.
         */
        private final class DeduplicatingResultHandler implements ResultHandler<ModelResults<EclipseProject>> {

            private final ResultHandler<? super ModelResults<EclipseProject>> delegate;

            private DeduplicatingResultHandler(ResultHandler<? super ModelResults<EclipseProject>> handler) {
                this.delegate = handler;
            }

            @Override
            public void onComplete(ModelResults<EclipseProject> result) {
                this.delegate.onComplete(deduplicate(result));
            }

            @Override
            public void onFailure(GradleConnectionException failure) {
                this.delegate.onFailure(failure);
            }

        }
    }

}
