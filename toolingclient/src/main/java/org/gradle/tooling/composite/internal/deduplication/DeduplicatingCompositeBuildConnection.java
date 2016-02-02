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

package org.gradle.tooling.composite.internal.deduplication;

import java.util.Set;

import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ResultHandler;
import org.gradle.tooling.composite.CompositeBuildConnection;
import org.gradle.tooling.composite.ModelResult;
import org.gradle.tooling.composite.internal.DefaultModelResult;
import org.gradle.tooling.composite.internal.DelegatingModelBuilder;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.collect.Sets;

/**
 * De-duplicates {@link EclipseProject} names.
 *
 * @author Stefan Oehme
 */
public class DeduplicatingCompositeBuildConnection implements CompositeBuildConnection {

    private CompositeBuildConnection delegate;

    public DeduplicatingCompositeBuildConnection(CompositeBuildConnection delegate) {
        this.delegate = delegate;
    }

    @Override
    public <T> Set<ModelResult<T>> getModels(Class<T> modelType) throws GradleConnectionException, IllegalStateException, IllegalArgumentException {
        return models(modelType).get();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <T> ModelBuilder<Set<ModelResult<T>>> models(Class<T> modelType) throws GradleConnectionException, IllegalStateException, IllegalArgumentException {
        ModelBuilder<Set<ModelResult<T>>> models = this.delegate.models(modelType);
        if (modelType == EclipseProject.class) {
            DeduplicatingModelBuilder builder = new DeduplicatingModelBuilder((ModelBuilder) models);
            return (ModelBuilder) builder;
        } else {
            return models;
        }
    }

    @Override
    public void close() {
        this.delegate.close();
    }

    /**
     * Deduplicates {@link EclipseProject}s returned from the delegate.
     */
    private static class DeduplicatingModelBuilder extends DelegatingModelBuilder<Set<ModelResult<EclipseProject>>> {

        public DeduplicatingModelBuilder(ModelBuilder<Set<ModelResult<EclipseProject>>> delegate) {
            super(delegate);
        }

        @Override
        public Set<ModelResult<EclipseProject>> get() throws GradleConnectionException, IllegalStateException {
            Set<ModelResult<EclipseProject>> result = super.get();
            return deduplicate(result);
        }

        @Override
        public void get(final ResultHandler<? super Set<ModelResult<EclipseProject>>> handler) throws IllegalStateException {
            this.delegate.get(new DeduplicatingResultHanlder(handler));
        }

        private Set<ModelResult<EclipseProject>> deduplicate(Set<ModelResult<EclipseProject>> eclipseProjects) {
            Set<EclipseProject> unwrappedProjects = Sets.newHashSet();
            for (ModelResult<EclipseProject> modelResult : eclipseProjects) {
                unwrappedProjects.add(modelResult.getModel());
            }
            Set<EclipseProject> deduplicatedProjects = new EclipseProjectDeduplicator().deduplicate(unwrappedProjects);
            Set<ModelResult<EclipseProject>> deduplicatedModelResults = Sets.newHashSet();
            for (EclipseProject deduplicatedProject : deduplicatedProjects) {
                deduplicatedModelResults.add(new DefaultModelResult<EclipseProject>(deduplicatedProject));
            }
            return deduplicatedModelResults;
        }

        /**
         * Deduplicates the {@link EclipseProject}s before passing them on to the actual handler.
         */
        private final class DeduplicatingResultHanlder implements ResultHandler<Set<ModelResult<EclipseProject>>> {

            private final ResultHandler<? super Set<ModelResult<EclipseProject>>> delegate;

            private DeduplicatingResultHanlder(ResultHandler<? super Set<ModelResult<EclipseProject>>> handler) {
                this.delegate = handler;
            }

            @Override
            public void onComplete(Set<ModelResult<EclipseProject>> result) {
                this.delegate.onComplete(deduplicate(result));
            }

            @Override
            public void onFailure(GradleConnectionException failure) {
                this.delegate.onFailure(failure);
            }

        }
    }

}
