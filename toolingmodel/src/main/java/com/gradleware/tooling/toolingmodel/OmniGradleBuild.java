package com.gradleware.tooling.toolingmodel;

/**
 * Provides detailed information about the Gradle build.
 *
 * @see org.gradle.tooling.model.GradleProject
 */
public interface OmniGradleBuild extends BuildScopedModel {

    /**
     * Returns the root project of the build.
     *
     * @return the root project
     */
    OmniGradleProject getRootProject();

}
