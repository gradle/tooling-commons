package com.gradleware.tooling.toolingmodel;

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

}
