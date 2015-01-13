package com.gradleware.tooling.domain.model;

import com.google.common.collect.ImmutableList;

import java.io.File;

/**
 * Provides basic information about the Gradle project and its hierarchy.
 */
public interface OmniGradleProjectStructure extends HierarchicalModel<OmniGradleProjectStructure> {

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
    ImmutableList<OmniGradleProjectStructure> getChildren();

    /**
     * Returns this project and all the nested child projects in its hierarchy.
     *
     * @return this project and all the nested child projects in its hierarchy
     */
    @Override
    ImmutableList<OmniGradleProjectStructure> getAll();

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
    String getPath();

    /**
     * Returns the project directory of this project.
     *
     * @return the project directory
     */
    File getProjectDirectory();

}
