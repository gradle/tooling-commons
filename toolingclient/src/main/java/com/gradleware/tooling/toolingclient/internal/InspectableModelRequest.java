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

import com.gradleware.tooling.toolingclient.ModelRequest;

/**
 * Internal interface that describes the configurable attributes of the model request.
 */
interface InspectableModelRequest<T> extends InspectableRequest<T>, ModelRequest<T> {

    /**
     * @return never null, DefaultToolingClient requires a model type to execute the request
     * @see DefaultToolingClient#mapToModelBuilder(InspectableModelRequest, org.gradle.tooling.ProjectConnection)
     */
    Class<T> getModelType();

    /**
     * @return never null, DefaultModelBuilder builder takes care of converting null and empty arrays to null
     * @see org.gradle.tooling.internal.consumer.DefaultModelBuilder#forTasks(String...)
     */
    String[] getTasks();

}
