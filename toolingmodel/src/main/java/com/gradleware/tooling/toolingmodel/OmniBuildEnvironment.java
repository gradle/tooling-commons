package com.gradleware.tooling.toolingmodel;

import com.gradleware.tooling.toolingmodel.generic.Model;

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
    Model<JavaEnvironmentFields> getJavaModel();

}
