package com.gradleware.tooling.toolingmodel;

import com.google.common.collect.ImmutableList;

/**
 * Provides information about the launchables (project tasks, task selectors) that can be used to initiate a Gradle build.
 *
 * @see org.gradle.tooling.model.gradle.BuildInvocations
 */
public interface OmniBuildInvocations {

    /**
     * Returns the tasks of this project.
     *
     * @return the tasks of this project
     */
    ImmutableList<OmniProjectTask> getProjectTasks();

    /**
     * Returns the task selectors of this project.
     *
     * @return the task selectors of this project
     */
    ImmutableList<OmniTaskSelector> getTaskSelectors();

}
