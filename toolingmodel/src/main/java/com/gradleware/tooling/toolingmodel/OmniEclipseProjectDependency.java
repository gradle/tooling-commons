package com.gradleware.tooling.toolingmodel;

/**
 * Describes a dependency on another Eclipse project.
 */
public interface OmniEclipseProjectDependency {

    /**
     * Returns the path of the target project of this dependency.
     *
     * @return the path of the target project of this dependency, never null
     */
    Path getTargetProjectPath();

    /**
     * Returns the path to use for this project dependency.
     *
     * @return the path to use for this project dependency
     */
    String getPath();

}
