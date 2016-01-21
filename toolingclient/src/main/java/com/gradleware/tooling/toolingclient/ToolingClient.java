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

package com.gradleware.tooling.toolingclient;

import com.gradleware.tooling.toolingclient.internal.DefaultToolingClient;
import org.gradle.internal.Factory;
import org.gradle.tooling.BuildAction;
import org.gradle.tooling.GradleConnector;

/**
 * Entry class to interact with the Tooling API. All interactions happen by creating and invoking requests of type {@link ModelRequest}. The tooling client takes care of the
 * house-keeping of all issued requests and the long-living resources potentially associated with these requests. Once the interactions with Gradle are over, the tooling client
 * must be stopped to clean up all remaining resources. <p> A tooling client instance is thread-safe. Typically, a single tooling client instance is used for the entire life-time
 * of the consumer interacting with the tooling client.
 *
 * @author Etienne Studer
 */
public abstract class ToolingClient {

    /**
     * Creates a new instance. Typically, a single tooling client instance is used for the entire life-time of the consumer interacting with the tooling client.
     *
     * @return a new instance
     */
    public static ToolingClient newClient() {
        return new DefaultToolingClient();
    }

    /**
     * Creates a new instance and uses the given factory whenever a new connector is required by the tooling client. Typically, a single tooling client instance is used for the
     * entire life-time of the consumer interacting with the tooling client.
     *
     * @param connectorFactory the connector factory
     * @return a new instance
     */
    public static ToolingClient newClient(Factory<GradleConnector> connectorFactory) {
        return new DefaultToolingClient(connectorFactory);
    }

    /**
     * Creates a new model request. A model request is used to fetch a given model that is available through the Tooling API.
     *
     * @param modelType the type of the model to fetch through the Tooling API
     * @param <T> the type of the model to fetch
     * @return a new instance
     */
    public abstract <T> ModelRequest<T> newModelRequest(Class<T> modelType);

    /**
     * Creates a new build action request. A build action request is used to run a given action in the build process. The build action and its result are serialized through the
     * Tooling API.
     *
     * @param buildAction the build action to run
     * @param <T> the result type of running the build action
     * @return a new instance
     */
    public abstract <T> BuildActionRequest<T> newBuildActionRequest(BuildAction<T> buildAction);

    /**
     * Creates a new build launch request. A build launch request is used to execute a Gradle build. If an empty set of launchables is specified, the project's default tasks are
     * executed. The build is executed through the Tooling API.
     *
     * @param launchables the launchables to execute
     * @return a new instance
     */
    public abstract BuildLaunchRequest newBuildLaunchRequest(LaunchableConfig launchables);

    /**
     * Creates a new test launch request. A test launch request is used to execute tests through a Gradle build.
     *
     * @param tests the tests to execute
     * @return a new instance
     */
    public abstract TestLaunchRequest newTestLaunchRequest(TestConfig tests);

    /**
     * Creates a new composite model request. A composite model request is used to fetch a given composite model that is available through the Tooling API.
     */
    public abstract <T> CompositeModelRequest<T> newCompositeModelRequest(Class<T> modelType);

    /**
     * Stops the tooling client and applies the specified clean-up strategy to any associated resources and processes. May block or may not block, depending on the specified
     * cleanup strategy.
     *
     * @param strategy the clean-up strategy to apply
     */
    public abstract void stop(CleanUpStrategy strategy);


    /**
     * Enumerates the different clean-up strategies.
     */
    public enum CleanUpStrategy {

        /**
         * Clean up all resources and forcefully shut down any associated running processes.
         */
        FORCEFULLY,

        /**
         * Clean up all resources and gracefully shut down any associated running processes.
         */
        GRACEFULLY

    }

}
