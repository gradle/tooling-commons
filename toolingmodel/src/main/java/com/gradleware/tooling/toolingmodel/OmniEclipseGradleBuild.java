package com.gradleware.tooling.toolingmodel;

import com.gradleware.tooling.toolingmodel.generic.HierarchicalModel;

/**
 * Provides detailed information about the Gradle build, suited for consumption in Eclipse.
 *
 * @see org.gradle.tooling.model.eclipse.EclipseProject
 */
public interface OmniEclipseGradleBuild extends BuildScopedModel {

    /**
     * Returns the root project of the build.
     *
     * @return the root project
     */
    OmniEclipseGradleProject getRootProject();

    /**
     * Returns the root project of the build.
     *
     * @return the root project
     */
    HierarchicalModel<EclipseGradleProjectFields> getRootProjectModel();

}
