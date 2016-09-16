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

import com.gradleware.tooling.toolingclient.TestLaunchRequest;
import com.gradleware.tooling.toolingclient.TestConfig;

/**
 * Internal interface that describes the configurable attributes of the test launch request.
 *
 * @author Donát Csikós
 */
interface InspectableTestLaunchRequest extends InspectableBuildRequest<Void>, TestLaunchRequest {

    /**
     * @return never null, DefaultToolingClient requires a test configuration to execute the request
     * @see DefaultToolingClient#mapToTestLauncher(InspectableTestLaunchRequest, org.gradle.tooling.ProjectConnection)
     */
    TestConfig getTests();

}
