package com.gradleware.tooling.toolingmodel;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import java.io.File;

/**
 * Provides detailed information about the Gradle project and its hierarchy, suited for consumption in Eclipse.
 */
public interface OmniEclipseGradleProject extends HierarchicalModel<OmniEclipseGradleProject> {

    /**
     * Returns the parent project of this project.
     *
     * @return the parent project, can be null
     */
    @Override
    OmniEclipseGradleProject getParent();

    /**
     * Returns the immediate child projects of this project.
     *
     * @return the immediate child projects of this project
     */
    ImmutableList<OmniEclipseGradleProject> getChildren();

    /**
     * Returns this project and all the nested child projects in its hierarchy.
     *
     * @return this project and all the nested child projects in its hierarchy
     */
    @Override
    ImmutableList<OmniEclipseGradleProject> getAll();

    /**
     * Returns all projects that match the given criteria.
     *
     * @param predicate the criteria to match
     * @return the matching projects
     */
    @Override
    ImmutableList<OmniEclipseGradleProject> filter(Predicate<? super OmniEclipseGradleProject> predicate);

    /**
     * Returns the first project that matches the given criteria, if any.
     *
     * @param predicate the criteria to match
     * @return the matching project, if any
     */
    @Override
    Optional<OmniEclipseGradleProject> tryFind(Predicate<? super OmniEclipseGradleProject> predicate);

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
