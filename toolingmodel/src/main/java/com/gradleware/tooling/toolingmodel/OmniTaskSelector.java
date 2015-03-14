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

import com.gradleware.tooling.toolingutils.ImmutableCollection;

import java.util.SortedSet;

/**
 * Represents a task selector which is executable by Gradle. A task selector requests to execute all project tasks with a given name in the context of some project and all its
 * subprojects.
 *
 * @author Etienne Studer
 */
public interface OmniTaskSelector {

    /**
     * Returns the name of this task selector. All project tasks selected by this task selector have the same name as this task selector.
     *
     * @return the name of this task selector
     */
    String getName();

    /**
     * Returns the description of this task selector, or {@code null} if it has no description.
     *
     * @return the description of this task selector, or {@code null} if it has no description
     */
    String getDescription();

    /**
     * Returns the path of the project and all its subprojects in which the task selector operates.
     *
     * @return the path of the project in which this task selector operates
     */
    Path getProjectPath();

    /**
     * Returns whether this task selector is public or not. Public task selectors are those that select at least one public project task.
     *
     * @return {@code true} if this task selector is public, {@code false} otherwise
     */
    boolean isPublic();

    /**
     * Returns the tasks selected by this task selector, identified by their unique path.
     *
     * @return the selected tasks, identified by their unique path
     */
    @ImmutableCollection
    SortedSet<Path> getSelectedTaskPaths();

}
