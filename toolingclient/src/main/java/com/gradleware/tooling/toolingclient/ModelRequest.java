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
import java.util.EnumSet;

import org.gradle.tooling.events.ProgressEventType;

/**
 * A {@code ModelRequest} allows you to fetch a snapshot of some model for a project or a build. Instances of {@code ModelRequest} are not thread-safe. <p> You use a {@code
 * ModelRequest} as follows: <ul> <li>Create an instance of {@code ModelRequest} by calling {@link ToolingClient#newModelRequest(Class)}. <li>Configure the request as appropriate.
 * <li>Call either {@link #executeAndWait()} or {@link #execute()} to fetch the model. <li>Optionally, you can reuse the request to fetch the model multiple times. </ul>
 *
 * @param <T> the type of model to fetch
 * @author Etienne Studer
 */
public interface ModelRequest<T> extends Request<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    ModelRequest<T> projectDir(File projectDir);

    /**
     * {@inheritDoc}
     */
    @Override
    ModelRequest<T> gradleUserHomeDir(File gradleUserHomeDir);

    /**
     * {@inheritDoc}
     */
    @Override
    ModelRequest<T> gradleDistribution(GradleDistribution gradleDistribution);

    /**
     * {@inheritDoc}
     */
    @Override
    ModelRequest<T> colorOutput(boolean colorOutput);

    /**
     * {@inheritDoc}
     */
    @Override
    ModelRequest<T> standardOutput(OutputStream outputStream);

    /**
     * {@inheritDoc}
     */
    @Override
    ModelRequest<T> standardError(OutputStream outputStream);

    /**
     * {@inheritDoc}
     */
    @Override
    ModelRequest<T> standardInput(InputStream inputStream);

    /**
     * {@inheritDoc}
     */
    @Override
    ModelRequest<T> javaHomeDir(File javaHomeDir);

    /**
     * {@inheritDoc}
     */
    @Override
    ModelRequest<T> jvmArguments(String... jvmArguments);

    /**
     * {@inheritDoc}
     */
    @Override
    ModelRequest<T> arguments(String... arguments);

    /**
     * {@inheritDoc}
     */
    @Override
    ModelRequest<T> progressListeners(ProgressListener... listeners);

    /**
     * {@inheritDoc}
     */
    @Override
    ModelRequest<T> addProgressListeners(ProgressListener... listeners);

    /**
     * {@inheritDoc}
     */
    @Override
    ModelRequest<T> typedProgressListeners(org.gradle.tooling.events.ProgressListener... listeners);

    /**
     * {@inheritDoc}
     */
    @Override
    ModelRequest<T> typedProgressListeners(EnumSet<ProgressEventType> progressEventTypes, org.gradle.tooling.events.ProgressListener... listeners);

    /**
     * {@inheritDoc}
     */
    @Override
    ModelRequest<T> addTypedProgressListeners(org.gradle.tooling.events.ProgressListener... listeners);

    /**
     * {@inheritDoc}
     */
    @Override
    ModelRequest<T> addTypedProgressListeners(EnumSet<ProgressEventType> progressEventTypes, org.gradle.tooling.events.ProgressListener... listeners);

    /**
     * {@inheritDoc}
     */
    @Override
    ModelRequest<T> cancellationToken(CancellationToken cancellationToken);

    /**
     * Specifies the tasks to execute before building the model. By default, no tasks are executed.
     *
     * @param tasks the paths of the tasks to be executed, relative paths are evaluated relative to the project for which this request was created
     * @return this
     */
    ModelRequest<T> tasks(String... tasks);

    /**
     * Derive a new model request from this request and apply the given model. This request and the new request do not share any state except the cancellation token.
     *
     * @param modelType the type of the model of the new request
     * @param <S> the type of the model to fetch
     * @return the new model request instance
     */
    <S> ModelRequest<S> deriveForModel(Class<S> modelType);

    /**
     * Fetches the requested model synchronously. Calling this method will block until the model has been fetched or a failure has occurred.
     *
     * @return the requested model
     * @see org.gradle.tooling.ModelBuilder#get()
     */
    @Override
    T executeAndWait();

    /**
     * Fetches the requested model asynchronously. Calling this method will return immediately. The returned promise is used to configure the success and failure behavior.
     *
     * @return the promise of the requested model
     * @see org.gradle.tooling.ModelBuilder#get(org.gradle.tooling.ResultHandler)
     */
    @Override
    LongRunningOperationPromise<T> execute();

}
