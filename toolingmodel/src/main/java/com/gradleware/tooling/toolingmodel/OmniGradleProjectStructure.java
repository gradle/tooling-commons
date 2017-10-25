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
import com.gradleware.tooling.toolingmodel.util.Maybe;
import com.gradleware.tooling.toolingutils.ImmutableCollection;
import org.gradle.api.specs.Spec;

import java.io.File;
import java.util.List;

/**
 * Provides basic information about the Gradle project and its hierarchy.
 *
 * @author Etienne Studer
 */
public interface OmniGradleProjectStructure extends HierarchicalModel<OmniGradleProjectStructure> {

    /**
     * Returns the root project of this project.
     *
     * @return the root project, never null
     */
    @Override
    OmniGradleProjectStructure getRoot();

    /**
     * Returns the parent project of this project.
     *
     * @return the parent project, can be null
     */
    @Override
    OmniGradleProjectStructure getParent();

    /**
     * Returns the immediate child projects of this project.
     *
     * @return the immediate child projects of this project
     */
    @Override
    @ImmutableCollection
    List<OmniGradleProjectStructure> getChildren();

    /**
     * Returns this project and all the nested child projects in its hierarchy.
     *
     * @return this project and all the nested child projects in its hierarchy
     */
    @Override
    @ImmutableCollection
    List<OmniGradleProjectStructure> getAll();

    /**
     * Returns all projects that match the given criteria.
     *
     * @param predicate the criteria to match
     * @return the matching projects
     */
    @Override
    @ImmutableCollection
    List<OmniGradleProjectStructure> filter(Spec<? super OmniGradleProjectStructure> predicate);

    /**
     * Returns the first project that matches the given criteria, if any.
     *
     * @param predicate the criteria to match
     * @return the matching project, if any
     */
    @Override
    Optional<OmniGradleProjectStructure> tryFind(Spec<? super OmniGradleProjectStructure> predicate);

    /**
     * Returns the name of this project. Note that the name is not a unique identifier for the project.
     *
     * @return the name of this project
     */
    String getName();

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
    Maybe<File> getProjectDirectory();

}
