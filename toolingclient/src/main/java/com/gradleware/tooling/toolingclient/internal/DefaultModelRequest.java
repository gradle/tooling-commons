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

package com.gradleware.tooling.toolingclient.internal;

import org.gradle.tooling.model.gradle.BuildInvocations;
import org.gradle.tooling.model.gradle.ProjectPublications;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingclient.LongRunningOperationPromise;
import com.gradleware.tooling.toolingclient.ModelRequest;
/**
 * Internal implementation of the {@link ModelRequest} API.
 *
 * @param <T> the result type
 * @author Etienne Studer
 */
final class DefaultModelRequest<T> extends BaseSimpleRequest<T, DefaultModelRequest<T>> implements InspectableModelRequest<T> {

    // all models provided by an instance of ProjectSensitiveToolingModelBuilder are listed here
    private static final Class<?>[] UNSUPPORTED_MODEL_TYPES = new Class<?>[]{BuildInvocations.class, ProjectPublications.class};

    private final Class<T> modelType;
    private ImmutableList<String> tasks;

    DefaultModelRequest(ExecutableToolingClient toolingClient, Class<T> modelType) {
        super(toolingClient);
        this.modelType = Preconditions.checkNotNull(modelType);
        this.tasks = ImmutableList.of();

        ensureModelIsBuildScoped(modelType);
    }

    private void ensureModelIsBuildScoped(Class<T> modelType) {
        for (Class<?> unsupportedModelType : UNSUPPORTED_MODEL_TYPES) {
            if (unsupportedModelType == modelType) {
                throw new IllegalArgumentException(String.format("%s model does not support the Build scope", modelType.getSimpleName()));
            }
        }
    }

    @Override
    public Class<T> getModelType() {
        return this.modelType;
    }

    @Override
    public DefaultModelRequest<T> tasks(String... tasks) {
        this.tasks = ImmutableList.copyOf(tasks);
        return getThis();
    }

    @Override
    public String[] getTasks() {
        return this.tasks.toArray(new String[this.tasks.size()]);
    }

    @Override
    public <S> ModelRequest<S> deriveForModel(Class<S> modelType) {
        return copy(new DefaultModelRequest<S>(getToolingClient(), modelType)).tasks(getTasks());
    }

    @Override
    public T executeAndWait() {
        return getToolingClient().executeAndWait(this);
    }

    @Override
    public LongRunningOperationPromise<T> execute() {
        return getToolingClient().execute(this);
    }

    @Override
    DefaultModelRequest<T> getThis() {
        return this;
    }

}
