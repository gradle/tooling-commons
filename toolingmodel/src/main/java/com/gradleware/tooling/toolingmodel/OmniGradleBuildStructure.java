package com.gradleware.tooling.toolingmodel;

import com.gradleware.tooling.toolingmodel.generic.HierarchicalModel;

/**
 * Provides information about the Gradle build structure.
 *
 * @see org.gradle.tooling.model.gradle.GradleBuild
 */
public interface OmniGradleBuildStructure extends BuildScopedModel {

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
