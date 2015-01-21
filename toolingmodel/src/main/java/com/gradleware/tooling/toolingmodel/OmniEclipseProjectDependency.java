package com.gradleware.tooling.toolingmodel;

/**
 * Describes a dependency on another Eclipse project.
 */
public interface OmniEclipseProjectDependency {

    /**
     * Returns the unique path of the target project of this dependency.
     *
     * @return the unique path of the target project of this dependency, never null
     */
    String getTargetProjectPath();

    /**
     * Returns the path to use for this project dependency.
     *
     * @return the path to use for this project dependency
     */
    String getPath();

}
