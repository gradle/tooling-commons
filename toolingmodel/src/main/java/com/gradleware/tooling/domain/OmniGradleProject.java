package com.gradleware.tooling.domain;

import com.google.common.collect.ImmutableList;

import java.io.File;

/**
 * Provides detailed information about the Gradle project and its hierarchy.
 */
public interface OmniGradleProject extends HierarchicalModel<OmniGradleProject> {

    /**
     * Returns the parent project of this project.
     *
     * @return the parent project, can be null
     */
    @Override
    OmniGradleProject getParent();

    /**
     * Returns the immediate child projects of this project.
     *
     * @return the immediate child projects of this project
     */
    ImmutableList<OmniGradleProject> getChildren();

    /**
     * Returns this project and all the nested child projects in its hierarchy.
     *
     * @return this project and all the nested child projects in its hierarchy
     */
    @Override
    ImmutableList<OmniGradleProject> getAll();

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
    String getDescription();

    /**
     * Returns the path of this project. The path can be used as a unique identifier for the project within a given build.
     *
     * @return the path of this project
     */
    String getPath();

    /**
     * Returns the project directory of this project.
     *
     * @return the project directory
     */
    File getProjectDirectory();

    /**
     * Returns the build directory of this project.
     *
     * @return the build directory
     */
    File getBuildDirectory();

    /**
     * Returns the build script of this project.
     *
     * @return the build script
     */
    OmniGradleScript getBuildScript();

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
