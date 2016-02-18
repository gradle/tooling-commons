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

import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.ProgressListener;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A {@code BuildLaunchRequest} allows you to configure and execute a Gradle build. Instances of {@code BuildLaunchRequest} are not thread-safe. <p> You use a {@code
 * BuildLaunchRequest} as follows: <ul> <li>Create an instance of {@code BuildLaunchRequest} by calling {@link ToolingClient#newBuildLaunchRequest(LaunchableConfig)}. <li>Configure
 * the request as appropriate. <li>Call either {@link #executeAndWait()} or {@link #execute()} to execute the Gradle build. <li>Optionally, you can reuse the request to execute the
 * Gradle build multiple times. </ul>
 *
 * @author Etienne Studer
 */
public interface BuildLaunchRequest extends SimpleRequest<Void> {

    /**
     * {@inheritDoc}
     */
    @Override
    BuildLaunchRequest projectDir(File projectDir);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildLaunchRequest gradleUserHomeDir(File gradleUserHomeDir);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildLaunchRequest gradleDistribution(GradleDistribution gradleDistribution);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildLaunchRequest colorOutput(boolean colorOutput);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildLaunchRequest standardOutput(OutputStream outputStream);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildLaunchRequest standardError(OutputStream outputStream);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildLaunchRequest standardInput(InputStream inputStream);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildLaunchRequest javaHomeDir(File javaHomeDir);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildLaunchRequest jvmArguments(String... jvmArguments);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildLaunchRequest arguments(String... arguments);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildLaunchRequest progressListeners(ProgressListener... listeners);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildLaunchRequest addProgressListeners(ProgressListener... listeners);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildLaunchRequest typedProgressListeners(org.gradle.tooling.events.ProgressListener... listeners);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildLaunchRequest addTypedProgressListeners(org.gradle.tooling.events.ProgressListener... listeners);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildLaunchRequest cancellationToken(CancellationToken cancellationToken);

    /**
     * Derive a new build launch request from this request and apply the given launchables. This request and the new request do not share any state except the cancellation token.
     *
     * @param launchables the launchables to execute
     * @return the new build launch request instance
     */
    BuildLaunchRequest deriveForLaunchables(LaunchableConfig launchables);

    /**
     * Executes the configured Gradle build synchronously. Calling this method will block until the Gradle build has been executed or a failure has occurred.
     *
     * @return the void result of executing the build
     * @see org.gradle.tooling.BuildLauncher#run()
     */
    @Override
    Void executeAndWait();

    /**
     * Executes the configured Gradle build asynchronously. Calling this method will return immediately. The returned promise is used to configure the success and failure
     * behavior.
     *
     * @return the promise of the void result of executing the build
     * @see org.gradle.tooling.BuildLauncher#run(org.gradle.tooling.ResultHandler)
     */
    @Override
    LongRunningOperationPromise<Void> execute();

}
