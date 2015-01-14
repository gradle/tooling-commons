package com.gradleware.tooling.toolingmodel;

import com.gradleware.tooling.toolingmodel.generic.HierarchicalModel;

/**
 * Provides detailed information about the Gradle build.
 *
 * @see org.gradle.tooling.model.GradleProject
 */
public interface OmniGradleBuild {

    /**
     * Returns the root project of the build.
     *
     * @return the root project
     */
    OmniGradleProject getRootProject();

    /**
     * Returns the root project of the build.
     *
     * @return the root project
     */
    HierarchicalModel<GradleProjectFields> getRootProjectModel();

}
