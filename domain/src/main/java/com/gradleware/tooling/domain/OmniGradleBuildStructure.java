package com.gradleware.tooling.domain;

import com.gradleware.tooling.domain.generic.HierarchicalModel;

/**
 * Provides information about the Gradle build structure.
 *
 * @see org.gradle.tooling.model.gradle.GradleBuild
 */
public interface OmniGradleBuildStructure {

    /**
     * Returns the root project of the build.
     *
     * @return the root project
     */
    OmniGradleProjectStructure getRootProject();

    /**
     * Returns the root project of the build.
     *
     * @return the root project
     */
    HierarchicalModel<BasicGradleProjectFields> getRootProjectModel();

}
