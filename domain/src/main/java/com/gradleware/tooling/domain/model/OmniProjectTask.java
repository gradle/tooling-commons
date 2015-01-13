package com.gradleware.tooling.domain.model;

/**
 * Represents a project task which is executable by Gradle.
 */
public interface OmniProjectTask {

    /**
     * Returns the name of this task. Note that the name is not a unique identifier for the project.
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
    String getPath();

    /**
     * Returns whether this task is public or not. Public tasks are those that have a non-null {@code group} property.     *
     *
     * @return {@code true} if this task is public, {@code false} otherwise
     */
    boolean isPublic();

}
