/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.tooling.composite;

import org.gradle.tooling.*;

import java.util.Set;

/**
 * Represents a long-lived connection to a composite of Gradle project(s). You obtain an instance of a
 * {@code CompositeBuildConnection} by using {@link org.gradle.tooling.composite.CompositeBuildConnector#connect()}.
 *
 * @author Benjamin Muschko
 */
public interface CompositeBuildConnection {

    /**
     * Fetches a snapshot of the models of the given type for this composite. This method blocks until the model is available.
     * <p>
     * This method is simply a convenience for calling {@code models(modelType).get()}
     *
     * @param modelType the model type
     * @param <T> the model type
     * @return the models
     * @throws BuildException On some failure executing the Gradle build, in order to build the model
     * @throws GradleConnectionException On some other failure using the connection
     * @throws IllegalStateException When this connection has been closed or is closing
     * @throws IllegalArgumentException if the provided model type is not supported
     */
    <T> Set<ModelResult<T>> getModels(Class<T> modelType) throws GradleConnectionException, IllegalStateException, IllegalArgumentException;

    /**
     * Creates a builder which can be used to query the model of the given type.
     * <p>
     * Any of following models types are available:
     *
     * <ul>
     *     <li>{@link org.gradle.tooling.model.eclipse.EclipseProject}</li>
     * </ul>
     *
     * @param modelType the model type
     * @param <T> the model type
     * @return the builder
     * @throws BuildException On some failure executing the Gradle build, in order to build the model
     * @throws GradleConnectionException On some other failure using the connection
     * @throws IllegalStateException When this connection has been closed or is closing
     * @throws IllegalArgumentException if the provided model type is not supported
     */
    <T> ModelBuilder<Set<ModelResult<T>>> models(Class<T> modelType) throws GradleConnectionException, IllegalStateException, IllegalArgumentException;

    /**
     * Closes this connection. Blocks until any pending operations are complete.
     */
    void close();
}