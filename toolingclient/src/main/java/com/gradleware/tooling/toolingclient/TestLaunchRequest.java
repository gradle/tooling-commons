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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.ProgressListener;

/**
 * A {@code TestLaunchRequest} allows you to configure and execute tests through Gradle. Instances of {@code TestLaunchRequest} are not thread-safe.
 * <p/>
 * You use a {@code TestLaunchRequest} as follows: <ul> <li>Create an instance of {@code TestLaunchRequest} by calling {@link ToolingClient#newTestLaunchRequest(TestConfig)}.</li>
 * <li>Configure the request as appropriate. <li>Call either {@link #executeAndWait()} or {@link #execute()} to execute the tests through Gradle. <li>Optionally, you can reuse the
 * request to execute the tests through Gradle multiple times. </ul>
 *
 * @author Donát Csikós
 */
public interface TestLaunchRequest extends BuildRequest<Void> {

    /**
     * {@inheritDoc}
     */
    @Override
    TestLaunchRequest projectDir(File projectDir);

    /**
     * {@inheritDoc}
     */
    @Override
    TestLaunchRequest gradleUserHomeDir(File gradleUserHomeDir);

    /**
     * {@inheritDoc}
     */
    @Override
    TestLaunchRequest gradleDistribution(GradleDistribution gradleDistribution);

    /**
     * {@inheritDoc}
     */
    @Override
    TestLaunchRequest colorOutput(boolean colorOutput);

    /**
     * {@inheritDoc}
     */
    @Override
    TestLaunchRequest standardOutput(OutputStream outputStream);

    /**
     * {@inheritDoc}
     */
    @Override
    TestLaunchRequest standardError(OutputStream outputStream);

    /**
     * {@inheritDoc}
     */
    @Override
    TestLaunchRequest standardInput(InputStream inputStream);

    /**
     * {@inheritDoc}
     */
    @Override
    TestLaunchRequest javaHomeDir(File javaHomeDir);

    /**
     * {@inheritDoc}
     */
    @Override
    TestLaunchRequest jvmArguments(String... jvmArguments);

    /**
     * {@inheritDoc}
     */
    @Override
    TestLaunchRequest arguments(String... arguments);

    /**
     * {@inheritDoc}
     */
    @Override
    TestLaunchRequest progressListeners(ProgressListener... listeners);

    /**
     * {@inheritDoc}
     */
    @Override
    TestLaunchRequest addProgressListeners(ProgressListener... listeners);

    /**
     * {@inheritDoc}
     */
    @Override
    TestLaunchRequest typedProgressListeners(org.gradle.tooling.events.ProgressListener... listeners);

    /**
     * {@inheritDoc}
     */
    @Override
    TestLaunchRequest addTypedProgressListeners(org.gradle.tooling.events.ProgressListener... listeners);

    /**
     * {@inheritDoc}
     */
    @Override
    TestLaunchRequest cancellationToken(CancellationToken cancellationToken);

    /**
     * Derive a new test launch request from this request and apply the given tests. This request and the
     * new request do not share any state except the cancellation token.
     *
     * @param tests the tests to execute
     * @return the new test launch request instance
     */
    TestLaunchRequest deriveForTests(TestConfig tests);

    /**
     * Executes the configured tests through Gradle synchronously. Calling this method will block until
     * the Gradle build has been executed or a failure has occurred.
     */
    @Override
    Void executeAndWait();

    /**
     * Executes the configured tests through Gradle asynchronously. Calling this method will return
     * immediately. The returned promise is used to configure the success and failure behavior.
     *
     * @return the promise of the void result of executing the build
     * @see org.gradle.tooling.TestLauncher#run(org.gradle.tooling.ResultHandler)
     */
    @Override
    LongRunningOperationPromise<Void> execute();

}
