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

import com.gradleware.tooling.toolingclient.LongRunningOperationPromise;

/**
 * Internal interface that describes the request execution part of the tooling client.
 *
 * @author Etienne Studer
 */
interface ExecutableToolingClient {

    <T> T executeAndWait(InspectableModelRequest<T> modelRequest);

    <T> LongRunningOperationPromise<T> execute(InspectableModelRequest<T> modelRequest);

    <T> T executeAndWait(InspectableBuildActionRequest<T> buildActionRequest);

    <T> LongRunningOperationPromise<T> execute(InspectableBuildActionRequest<T> buildActionRequest);

    Void executeAndWait(InspectableBuildLaunchRequest buildLaunchRequest);

    LongRunningOperationPromise<Void> execute(InspectableBuildLaunchRequest buildLaunchRequest);

}
