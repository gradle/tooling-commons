package com.gradleware.tooling.toolingmodel;

import com.google.common.base.Optional;
import com.gradleware.tooling.toolingmodel.util.Maybe;
import com.gradleware.tooling.utils.ImmutableCollection;
import org.gradle.api.specs.Spec;

import java.io.File;
import java.util.List;

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
    @Override
    @ImmutableCollection
    List<OmniGradleProject> getChildren();

    /**
     * Returns this project and all the nested child projects in its hierarchy.
     *
     * @return this project and all the nested child projects in its hierarchy
     */
    @Override
    @ImmutableCollection
    List<OmniGradleProject> getAll();

    /**
     * Returns all projects that match the given criteria.
     *
     * @param predicate the criteria to match
     * @return the matching projects
     */
    @Override
    @ImmutableCollection
    List<OmniGradleProject> filter(Spec<? super OmniGradleProject> predicate);

    /**
     * Returns the first project that matches the given criteria, if any.
     *
     * @param predicate the criteria to match
     * @return the matching project, if any
     */
    @Override
    Optional<OmniGradleProject> tryFind(Spec<? super OmniGradleProject> predicate);

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
    Path getPath();

    /**
     * Returns the project directory of this project.
     *
     * @return the project directory
     */
    Maybe<File> getProjectDirectory();

    /**
     * Returns the build directory of this project.
     *
     * @return the build directory
     */
    Maybe<File> getBuildDirectory();

    /**
     * Returns the build script of this project.
     *
     * @return the build script
     */
    Maybe<OmniGradleScript> getBuildScript();

    /**
     * Returns the tasks of this project.
     *
     * @return the tasks of this project
     */
    @ImmutableCollection
    List<OmniProjectTask> getProjectTasks();

    /**
     * Returns the task selectors of this project.
     *
     * @return the task selectors of this project
     */
    @ImmutableCollection
    List<OmniTaskSelector> getTaskSelectors();

}
