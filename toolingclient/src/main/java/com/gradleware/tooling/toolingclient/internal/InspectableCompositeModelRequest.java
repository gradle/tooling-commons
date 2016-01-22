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

import java.util.Set;

import com.gradleware.tooling.toolingclient.CompositeModelRequest;

/**
 * Internal companion for {@link CompositeModelRequest} that allows reading the configuration
 * values.
 *
 * @author Stefan Oehme
 *
 * @param <T> the result type
 */
public interface InspectableCompositeModelRequest<T> extends CompositeModelRequest<T>, InspectableCompositeRequest<Set<T>> {

    /**
     * @return never null, DefaultToolingClient requires a model type to execute the request
     */
    Class<T> getModelType();

}
