package com.gradleware.tooling.toolingmodel;

import com.google.common.base.Optional;
import com.gradleware.tooling.utils.ImmutableCollection;
import org.gradle.api.specs.Spec;

import java.io.File;
import java.util.List;

/**
 * Provides detailed information about the Eclipse project and its hierarchy.
 */
public interface OmniEclipseProject extends HierarchicalModel<OmniEclipseProject> {

    /**
     * Returns the parent project of this project.
     *
     * @return the parent project, can be null
     */
    @Override
    OmniEclipseProject getParent();

    /**
     * Returns the immediate child projects of this project.
     *
     * @return the immediate child projects of this project
     */
    @Override
    @ImmutableCollection
    List<OmniEclipseProject> getChildren();

    /**
     * Returns this project and all the nested child projects in its hierarchy.
     *
     * @return this project and all the nested child projects in its hierarchy
     */
    @Override
    @ImmutableCollection
    List<OmniEclipseProject> getAll();

    /**
     * Returns all projects that match the given criteria.
     *
     * @param predicate the criteria to match
     * @return the matching projects
     */
    @Override
    @ImmutableCollection
    List<OmniEclipseProject> filter(Spec<? super OmniEclipseProject> predicate);

    /**
     * Returns the first project that matches the given criteria, if any.
     *
     * @param predicate the criteria to match
     * @return the matching project, if any
     */
    @Override
    Optional<OmniEclipseProject> tryFind(Spec<? super OmniEclipseProject> predicate);

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
    File getProjectDirectory();

    /**
     * Returns the project dependencies of this project.
     *
     * @return the project dependencies
     */
    @ImmutableCollection
    List<OmniEclipseProjectDependency> getProjectDependencies();

    /**
     * Returns the external dependencies of this project.
     *
     * @return the external dependencies
     */
    @ImmutableCollection
    List<OmniExternalDependency> getExternalDependencies();

    /**
     * Returns the linked resources of this project.
     *
     * @return the linked resources
     */
    @ImmutableCollection
    List<OmniEclipseLinkedResource> getLinkedResources();

    /**
     * Returns the source directories of this project.
     *
     * @return the source directories
     */
    @ImmutableCollection
    List<OmniEclipseSourceDirectory> getSourceDirectories();

}
