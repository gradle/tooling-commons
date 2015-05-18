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

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.ProgressListener;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;

import org.gradle.tooling.events.ProgressEventType;

/**
 * A {@code BuildActionRequest} allows to execute logic in the build process. Instances of {@code BuildActionRequest} are not thread-safe. <p> You use a {@code BuildActionRequest}
 * as follows: <ul> <li>Create an instance of {@code BuildActionRequest} by calling {@link ToolingClient#newBuildActionRequest(org.gradle.tooling.BuildAction)}. <li>Configure the
 * request as appropriate. <li>Call either {@link #executeAndWait()} or {@link #execute()} to execute the build action. <li>Optionally, you can reuse the request to execute the
 * build action multiple times. </ul>
 *
 * @param <T> the type of result produced by the build action
 * @author Etienne Studer
 */
public interface BuildActionRequest<T> extends Request<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    BuildActionRequest<T> projectDir(File projectDir);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildActionRequest<T> gradleUserHomeDir(File gradleUserHomeDir);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildActionRequest<T> gradleDistribution(GradleDistribution gradleDistribution);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildActionRequest<T> colorOutput(boolean colorOutput);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildActionRequest<T> standardOutput(OutputStream outputStream);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildActionRequest<T> standardError(OutputStream outputStream);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildActionRequest<T> standardInput(InputStream inputStream);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildActionRequest<T> javaHomeDir(File javaHomeDir);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildActionRequest<T> jvmArguments(String... jvmArguments);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildActionRequest<T> arguments(String... arguments);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildActionRequest<T> progressListeners(ProgressListener... listeners);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildActionRequest<T> addProgressListeners(ProgressListener... listeners);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildActionRequest<T> typedProgressListeners(org.gradle.tooling.events.ProgressListener... listeners);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildActionRequest<T> typedProgressListeners(EnumSet<ProgressEventType> progressEventTypes, org.gradle.tooling.events.ProgressListener... listeners);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildActionRequest<T> addTypedProgressListeners(org.gradle.tooling.events.ProgressListener... listeners);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildActionRequest<T> addTypedProgressListeners(EnumSet<ProgressEventType> progressEventTypes, org.gradle.tooling.events.ProgressListener... listeners);

    /**
     * {@inheritDoc}
     */
    @Override
    BuildActionRequest<T> cancellationToken(CancellationToken cancellationToken);

    /**
     * Derive a new build action request from this request and apply the given build action. This request and the new request do not share any state except the cancellation token.
     *
     * @param buildAction the build action to run
     * @param <S> the result type of running the build action
     * @return the new build action request instance
     */
    <S> BuildActionRequest<S> deriveForBuildAction(BuildAction<S> buildAction);

    /**
     * Executes the configured build action synchronously. Calling this method will block until the build action has been executed or a failure has occurred.
     *
     * @return the result of running the build action
     * @see org.gradle.tooling.BuildActionExecuter#run()
     */
    @Override
    T executeAndWait();

    /**
     * Executes the configured build action asynchronously. Calling this method will return immediately. The returned promise is used to configure the success and failure
     * behavior.
     *
     * @return the promise of the result of running the build action
     * @see org.gradle.tooling.BuildActionExecuter#run(org.gradle.tooling.ResultHandler)
     */
    @Override
    LongRunningOperationPromise<T> execute();

}
