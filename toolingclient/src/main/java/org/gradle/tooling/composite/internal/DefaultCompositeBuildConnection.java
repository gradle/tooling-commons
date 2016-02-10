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

package org.gradle.tooling.composite.internal;

import com.google.common.base.Throwables;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.composite.CompositeBuildConnection;
import org.gradle.tooling.composite.ModelResult;
import org.gradle.tooling.internal.consumer.ConnectionParameters;
import org.gradle.tooling.internal.consumer.async.AsyncConsumerActionExecutor;
import org.gradle.tooling.model.eclipse.EclipseProject;

import java.util.Set;

/**
 * The default implementation of a composite build connection.
 *
 * @author Benjamin Muschko
 */
public class DefaultCompositeBuildConnection implements CompositeBuildConnection {
    private final AsyncConsumerActionExecutor connection;
    private final ConnectionParameters parameters;
    private final Set<ProjectConnection> participants;

    public DefaultCompositeBuildConnection(AsyncConsumerActionExecutor connection, ConnectionParameters parameters,
                                           Set<ProjectConnection> participants) {
        this.connection = connection;
        this.parameters = parameters;

        if (participants.isEmpty()) {
            throw new IllegalStateException("A composite build requires at least one participating project.");
        }

        this.participants = participants;
    }

    @Override
    public <T> Set<ModelResult<T>> getModels(Class<T> modelType) {
        return models(modelType).get();
    }

    @Override
    public <T> ModelBuilder<Set<ModelResult<T>>> models(Class<T> modelType) {
        if (!modelType.isInterface()) {
            throw new IllegalArgumentException(String.format("Cannot fetch a model of type '%s' as this type is not an interface.", modelType.getName()));
        }

        if (!modelType.equals(EclipseProject.class)) {
            throw new IllegalArgumentException(String.format("The only supported model for a Gradle composite is %s.class.", EclipseProject.class.getSimpleName()));
        }

        return new EclipseModelResultSetModelBuilder<T>(modelType, this.connection, this.parameters, this.participants);
    }

    @Override
    public void close() {
        Throwable failure = null;

        try {
            this.connection.stop();
        } catch (Throwable throwable) {
            if (failure == null) {
                failure = throwable;
            }
        }

        for (ProjectConnection projectConnection : this.participants) {
            try {
                projectConnection.close();
            } catch (Throwable throwable) {
                if (failure == null) {
                    failure = throwable;
                }
            }
        }
        if (failure != null) {
            Throwables.propagate(failure);
        }
    }
}