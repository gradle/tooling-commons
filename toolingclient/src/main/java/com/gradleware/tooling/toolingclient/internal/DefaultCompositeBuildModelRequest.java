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

package com.gradleware.tooling.toolingclient.internal;

import org.gradle.tooling.connection.ModelResults;

import com.google.common.base.Preconditions;

import com.gradleware.tooling.toolingclient.LongRunningOperationPromise;

/**
 * Default implementation for {@link com.gradleware.tooling.toolingclient.CompositeBuildModelRequest}.
 *
 * @author Stefan Oehme
 * @param <T> the result type
 */
final class DefaultCompositeBuildModelRequest<T> extends BaseBuildRequest<ModelResults<T>, DefaultCompositeBuildModelRequest<T>> implements InspectableCompositeBuildModelRequest<T> {

    private final Class<T> modelType;

    DefaultCompositeBuildModelRequest(ExecutableToolingClient toolingClient, Class<T> modelType) {
        super(toolingClient);
        this.modelType = Preconditions.checkNotNull(modelType);
    }

    @Override
    public Class<T> getModelType() {
        return this.modelType;
    }

    @Override
    public ModelResults<T> executeAndWait() {
        return getToolingClient().executeAndWait(this);
    }

    @Override
    public LongRunningOperationPromise<ModelResults<T>> execute() {
        return getToolingClient().execute(this);
    }

    @Override
    DefaultCompositeBuildModelRequest<T> getThis() {
        return this;
    }

}
