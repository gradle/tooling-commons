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

/**
 * Represents a project task which is executable by Gradle.
 */
public interface OmniProjectTask {

    /**
     * Returns the name of this task. Note that the name is not a unique identifier for the task.
     *
     * @return the name of this task
     */
    String getName();

    /**
     * Returns the description of this task, or {@code null} if it has no description.
     *
     * @return the description of this task, or {@code null} if it has no description
     */
    String getDescription();

    /**
     * Returns the path of this task. The path can be used as a unique identifier for the task within a given build.
     *
     * @return the path of this task
     */
    Path getPath();

    /**
     * Returns whether this task is public or not. Public tasks are those that have a non-empty {@code group} property.
     *
     * @return {@code true} if this task is public, {@code false} otherwise
     */
    boolean isPublic();

}
