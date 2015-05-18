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
 * Describes the state and actions common to all requests issued through the tooling client.
 *
 * @param <T> the result type
 * @author Etienne Studer
 */
public interface Request<T> {

    /**
     * Specifies the working directory to use.
     *
     * @param projectDir the working directory
     * @return this
     */
    Request<T> projectDir(File projectDir);

    /**
     * Specifies the user's Gradle home directory to use. Defaults to {@code ~/.gradle}.
     *
     * @param gradleUserHomeDir the user's Gradle home directory to use
     * @return this
     */
    Request<T> gradleUserHomeDir(File gradleUserHomeDir);

    /**
     * Specifies the Gradle distribution to use. Defaults to a project-specific Gradle version.
     *
     * @param gradleDistribution the Gradle distribution to use
     * @return this
     */
    Request<T> gradleDistribution(GradleDistribution gradleDistribution);

    /**
     * Specifies whether to generate colored (ANSI encoded) output for logging. The default is to not generate color output.
     *
     * @param colorOutput {@code true} to request color output (using ANSI encoding)
     * @return this
     */
    Request<T> colorOutput(boolean colorOutput);

    /**
     * Specifies the {@link java.io.OutputStream} which should receive standard output logging generated while running the operation. The default is to discard the output.
     *
     * @param outputStream the output stream, the system default character encoding will be used to encode characters written to this stream
     * @return this
     */
    Request<T> standardOutput(OutputStream outputStream);

    /**
     * Specifies the {@link java.io.OutputStream} which should receive standard error logging generated while running the operation. The default is to discard the output.
     *
     * @param outputStream the output stream, the system default character encoding will be used to encode characters written to this stream
     * @return this
     */
    Request<T> standardError(OutputStream outputStream);

    /**
     * Specifies the {@link java.io.InputStream} that will be used as standard input for this operation. Defaults to an empty input stream.
     *
     * @param inputStream the input stream
     * @return this
     */
    Request<T> standardInput(InputStream inputStream);

    /**
     * Specifies the Java home directory to use for this operation.
     *
     * @param javaHomeDir the home directory of the java installation to use for the Gradle process
     * @return this
     */
    Request<T> javaHomeDir(File javaHomeDir);

    /**
     * Specifies the Java VM arguments to use for this operation.
     *
     * @param jvmArguments the jvm arguments to use for the Gradle process
     * @return this
     */
    Request<T> jvmArguments(String... jvmArguments);

    /**
     * Specifies the command line build arguments.
     *
     * @param arguments the Gradle command line arguments
     * @return this
     */
    Request<T> arguments(String... arguments);

    /**
     * Specifies the progress listeners which will receive progress events as the request is executed.
     *
     * @param listeners the progress listeners to register
     * @return this
     */
    Request<T> progressListeners(ProgressListener... listeners);

    /**
     * Specifies additional progress listeners that will receive progress events as the request is executed.
     *
     * @param listeners the progress listeners to register in addition to the already registered progress listeners
     * @return this
     */
    Request<T> addProgressListeners(ProgressListener... listeners);

    /**
     * Specifies the progress listeners which will receive progress events as the request is executed.
     *
     * @param listeners the progress listeners to register
     * @return this
     */
    Request<T> typedProgressListeners(org.gradle.tooling.events.ProgressListener... listeners);

    /**
     * Specifies additional progress listeners that will receive progress events as the request is executed.
     *
     * @param listeners the progress listeners to register in addition to the already registered progress listeners
     * @return this
     */
    Request<T> addTypedProgressListeners(org.gradle.tooling.events.ProgressListener... listeners);

    /**
     * Specifies the cancellation token to use to cancel the request if required.
     *
     * @param cancellationToken the cancellation token
     * @return this
     */
    Request<T> cancellationToken(CancellationToken cancellationToken);

    /**
     * Executes this request synchronously. Calling this method will block until the request has completed or a failure has occurred.
     *
     * @return the request result
     */
    T executeAndWait();

    /**
     * Executes this request asynchronously. Calling this method will return immediately. The returned promise is used to configure the success and failure behavior.
     *
     * @return the promise of the request result
     */
    LongRunningOperationPromise<T> execute();

}
