package com.gradleware.tooling.toolingmodel;

import java.util.SortedSet;

/**
 * Represents a project task which is executable by Gradle.
 */
public interface OmniTaskSelector {

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
     * Returns whether this task is public or not. Public tasks are those that have a non-null {@code group} property.
     *
     * @return {@code true} if this task is public, {@code false} otherwise
     */
    boolean isPublic();

    /**
     * Returns the tasks selected by this task selector, identified by their unique path.
     *
     * @return the selected tasks
     */
    SortedSet<String> getSelectedTaskPaths();

}
