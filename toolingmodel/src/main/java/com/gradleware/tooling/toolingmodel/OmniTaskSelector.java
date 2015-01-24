package com.gradleware.tooling.toolingmodel;

import com.gradleware.tooling.utils.ImmutableCollection;

import java.util.SortedSet;

/**
 * Represents a task selector which is executable by Gradle. A task selector requests to execute all project tasks with a given name in the context of some project and all its
 * subprojects.
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
    String getProjectPath();

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
    SortedSet<String> getSelectedTaskPaths();

}
