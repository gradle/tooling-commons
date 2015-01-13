package com.gradleware.tooling.domain.model;

import com.gradleware.tooling.domain.generic.Model;

/**
 * Provides information about the build environment.
 *
 * @see org.gradle.tooling.model.build.BuildEnvironment
 */
public interface OmniBuildEnvironment {

    /**
     * Returns information about the Gradle environment.
     *
     * @return the Gradle environment
     */
    OmniGradleEnvironment getGradle();

    /**
     * Returns information about the Gradle environment.
     *
     * @return the Gradle environment
     */
    Model<GradleEnvironmentFields> getGradleModel();

    /**
     * Returns information about the Java environment.
     *
     * @return the Java environment
     */
    OmniJavaEnvironment getJava();

    /**
     * Returns information about the Java environment.
     *
     * @return the Java environment
     */
    Model<JavaEnvironmentFields> getJavaModel();

}
