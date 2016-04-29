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

package com.gradleware.tooling.toolingmodel;

import com.google.common.base.Optional;
import com.gradleware.tooling.toolingutils.ImmutableCollection;
import org.gradle.api.Nullable;
import org.gradle.api.specs.Spec;
import org.gradle.tooling.model.eclipse.EclipseProjectIdentifier;

import java.io.File;
import java.util.List;

/**
 * Provides detailed information about the Eclipse project and its hierarchy.
 *
 * @author Etienne Studer
 */
public interface OmniEclipseProject extends HierarchicalModel<OmniEclipseProject> {

    /**
     * Returns the root project of this project.
     *
     * @return the root project, never null
     */
    @Override
    OmniEclipseProject getRoot();

    /**
     * Returns the parent project of this project.
     *
     * @return the parent project, can be null
     */
    @Nullable
    @Override
    OmniEclipseProject getParent();

    /**
     * Returns the immediate child projects of this project.
     *
     * @return the immediate child projects of this project
     */
    @Override
    @ImmutableCollection
    List<OmniEclipseProject> getChildren();

    /**
     * Returns this project and all the nested child projects in its hierarchy.
     *
     * @return this project and all the nested child projects in its hierarchy
     */
    @Override
    @ImmutableCollection
    List<OmniEclipseProject> getAll();

    /**
     * Returns all projects that match the given criteria.
     *
     * @param predicate the criteria to match
     * @return the matching projects
     */
    @Override
    @ImmutableCollection
    List<OmniEclipseProject> filter(Spec<? super OmniEclipseProject> predicate);

    /**
     * Returns the first project that matches the given criteria, if any.
     *
     * @param predicate the criteria to match
     * @return the matching project, if any
     */
    @Override
    Optional<OmniEclipseProject> tryFind(Spec<? super OmniEclipseProject> predicate);

    /**
     * Returns the identifier of this project, which is used to correlate it with other models.
     * @return the identifier, never null
     */
    EclipseProjectIdentifier getIdentifier();

    /**
     * Returns the name of this project. Note that the name is not a unique identifier for the project.
     *
     * @return the name of this project
     */
    String getName();

    /**
     * Returns the description of this project, or {@code null} if it has no description.
     *
     * @return the description of this project
     */
    @Nullable
    String getDescription();

    /**
     * Returns the path of this project. The path can be used as a unique identifier for the project within a given build.
     *
     * @return the path of this project
     */
    Path getPath();

    /**
     * Returns the project directory of this project.
     *
     * @return the project directory
     */
    File getProjectDirectory();

    /**
     * Returns the project dependencies of this project.
     *
     * @return the project dependencies
     */
    @ImmutableCollection
    List<OmniEclipseProjectDependency> getProjectDependencies();

    /**
     * Returns the external dependencies of this project.
     *
     * @return the external dependencies
     */
    @ImmutableCollection
    List<OmniExternalDependency> getExternalDependencies();

    /**
     * Returns the linked resources of this project.
     *
     * @return the linked resources
     */
    @ImmutableCollection
    List<OmniEclipseLinkedResource> getLinkedResources();

    /**
     * Returns the source directories of this project.
     *
     * @return the source directories
     */
    @ImmutableCollection
    List<OmniEclipseSourceDirectory> getSourceDirectories();

    /**
     * Returns the Eclipse natures of this project.
     * <p>
     * If the target Gradle version doesn't support retrieving the natures then the method returns {@code Optional#absent()}
     *
     * @return the project natures
     */
    Optional<List<OmniEclipseProjectNature>> getProjectNatures();

    /**
     * Returns the build commands of this project.
     *
     * <p>
     * If the target Gradle version doesn't support retrieving the build commands then the method returns {@code Optional#absent()}
     * @return the build commands
     */
    Optional<List<OmniEclipseBuildCommand>> getBuildCommands();

    /**
     * Returns the Java source settings of this project.
     * <p>
     * If an older Gradle version is used then the result is calculated as follows:
     * <ul>
     * <li>the result is non-absent if there is at least one source folder defined on the project</li>
     * <li>the source language level and the Java Runtime is calculated from the JVM being used</li>
     * </ul>
     *
     * @return the Java source settings, or {@link Optional#absent()} if not a Java project
     */
    Optional<OmniJavaSourceSettings> getJavaSourceSettings();

    /**
     * Returns the underlying Gradle project.
     *
     * @return the underlying {@link OmniGradleProject}
     */
    OmniGradleProject getGradleProject();

}
