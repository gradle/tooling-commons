package com.gradleware.tooling.toolingmodel;

/**
 * Provides detailed information about the Gradle build, suited for consumption in Eclipse.
 *
 * @see org.gradle.tooling.model.eclipse.EclipseProject
 */
public interface OmniEclipseGradleBuild extends BuildScopedModel {

    /**
     * Returns the root Eclipse project of the build.
     *
     * @return the root Eclipse project
     */
    OmniEclipseProject getRootEclipseProject();

    /**
     * Returns the root Gradle project of the build.
     *
     * @return the root Gradle project
     */
    OmniGradleProject getRootProject();

}
