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

import org.gradle.internal.concurrent.DefaultExecutorFactory;
import org.gradle.internal.concurrent.ExecutorFactory;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.composite.internal.CompositeBuildConnectorFactory;
import org.gradle.tooling.internal.consumer.*;
import org.gradle.tooling.internal.consumer.loader.CachingToolingImplementationLoader;
import org.gradle.tooling.internal.consumer.loader.DefaultToolingImplementationLoader;
import org.gradle.tooling.internal.consumer.loader.SynchronizedToolingImplementationLoader;
import org.gradle.tooling.internal.consumer.loader.ToolingImplementationLoader;

import java.io.File;

/**
 * <p>A {@code CompositeBuildConnector} is the main entry point for create a composite build. You use this API as follows:</p>
 *
 * <ol>
 *     <li>Call {@link #newComposite()} to create a new connector instance.</li>
 *     <li>Add participating builds with the method {@link #addParticipant(File)}</li>
 *     <li>Call {@link #connect()} to create the connection to the composite.</li>
 *     <li>When finished with the connection, call {@link CompositeBuildConnection#close()} to clean up.</li>
 * </ol>
 *
 * Example:
 * <pre>
 * CompositeBuildConnection connection = null;
 *
 * try {
 *     connection = CompositeBuildConnector.newComposite()
 *         .addParticipant(new File("project-1"))
 *         .addParticipant(new File("project-2"))
 *         .connect();
 *     connection.getModels(EclipseProject.class);
 * } finally {
 *     if (connection != null) {
 *         connection.close();
 *     }
 * }
 * </pre>
 * 
 * @author Benjamin Muschko
 */
public abstract class CompositeBuildConnector {
    private static final ToolingImplementationLoader toolingImplementationLoader = new SynchronizedToolingImplementationLoader(new CachingToolingImplementationLoader(new DefaultToolingImplementationLoader()));
    private static final ExecutorFactory executorFactory =  new DefaultExecutorFactory();
    private static final LoggingProvider loggingProvider = new SynchronizedLogging();
    private static final ExecutorServiceFactory executorServiceFactory = new DefaultExecutorServiceFactory();
    private static final DistributionFactory distributionFactory = new DistributionFactory(executorServiceFactory);
    private static final DefaultConnectionParameters.Builder connectionParamsBuilder = DefaultConnectionParameters.builder();
    private static final CompositeBuildConnectorFactory gradleCompositeFactory = new CompositeBuildConnectorFactory(toolingImplementationLoader, executorFactory, loggingProvider);

    /**
     * Creates a new composite build connector.
     *
     * @return the composite build connector
     */
    public static CompositeBuildConnector newComposite() {
        ConnectionParameters parameters = connectionParamsBuilder.build();
        Distribution distribution = distributionFactory.getClasspathDistribution();
        return gradleCompositeFactory.create(distribution, parameters);
    }

    /**
     * Adds a participating Gradle project to the composite.
     * <p>
     * A composite needs to contain at least one participant before the method {@link #connect()} can be called.
     * <p>
     * The provided project directory should point to the root directory of a Gradle build. If the provided project
     * directory points to a sub-project within an hierarchical multi-project build, then the root project directory
     * is determined and the full project tree is traversed nonetheless.
     *
     * @param rootProjectDirectory the root project directory of the build
     * @return The participating build
     */
    public abstract CompositeParticipant addParticipant(File rootProjectDirectory);

    /**
     * Creates the connection to all participating builds added to the composite. You should call
     * {@link org.gradle.tooling.composite.CompositeBuildConnection#close()} when you are finished with the connection.
     *
     * @throws GradleConnectionException on failure to establish a connection to the provided participants
     * @throws IllegalStateException if no participating builds were provided by calling {@link #addParticipant(File)}
     * @return The connection. Never return null.
     */
    public abstract CompositeBuildConnection connect() throws GradleConnectionException;
}