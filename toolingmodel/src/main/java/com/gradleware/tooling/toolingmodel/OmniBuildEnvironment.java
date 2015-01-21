package com.gradleware.tooling.toolingmodel;

/**
 * Provides information about the build environment.
 *
 * @see org.gradle.tooling.model.build.BuildEnvironment
 */
public interface OmniBuildEnvironment extends BuildScopedModel {

    /**
     * Returns information about the Gradle environment.
     *
     * @return the Gradle environment
     */
    OmniGradleEnvironment getGradle();

    /**
     * Returns information about the Java environment.
     *
     * @return the Java environment
     */
    OmniJavaEnvironment getJava();

}
