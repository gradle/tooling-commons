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

import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.composite.internal.DefaultCompositeBuildConnector;

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
 * CompositeBuildConnection connection = CompositeBuildConnector.newComposite()
 *     .addParticipant(new File("project-1"))
 *     .addParticipant(new File("project-2"))
 *     .connect();
 *
 * try {
 *     connection.getModels(EclipseProject.class);
 * } finally {
 *     connection.close();
 * }
 * </pre>
 * 
 * @author Benjamin Muschko
 */
public abstract class CompositeBuildConnector {

    /**
     * Creates a new composite build connector.
     *
     * @return the composite build connector
     */
    public static CompositeBuildConnector newComposite() {
        return new DefaultCompositeBuildConnector();
    }

    /**
     * Adds a participating Gradle project to the composite.
     * <p>
     * A composite needs to contain at least one participant before the method {@link #connect()} can be called.
     * <p>
     * If the provided project directory points to a sub-project in a multi-project build, then the root project
     * is determined and used automatically.
     *
     * @param rootProjectDirectory the root project directory of the build
     * @return The participating build
     */
    public abstract CompositeParticipant addParticipant(File rootProjectDirectory);

    /**
     * Creates the connection to all participating builds added to the composite. You should call
     * {@link org.gradle.tooling.composite.CompositeBuildConnection#close()} when you are finished with the connection.
     *
     * @throws GradleConnectionException on failure to establish a connection
     * @return The connection. Never return null.
     */
    public abstract CompositeBuildConnection connect() throws GradleConnectionException;
}