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

import com.gradleware.tooling.toolingclient.CompositeModelRequest;
import com.gradleware.tooling.toolingclient.LongRunningOperationPromise;

/**
 * Internal implementation of {@link CompositeModelRequest} API.
 *
 * @author Stefan Oehme
 * @param <T> the result type
 */
public class DefaultCompositeModelRequest<T> extends BaseCompositeRequest<T, DefaultCompositeModelRequest<T>>implements InspectableCompositeModelRequest<T> {

    private Class<T> modelType;

    DefaultCompositeModelRequest(ExecutableToolingClient toolingClient, Class<T> modelType) {
        super(toolingClient);
        this.modelType = modelType;
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
    DefaultCompositeModelRequest<T> getThis() {
        return this;
    }

}
