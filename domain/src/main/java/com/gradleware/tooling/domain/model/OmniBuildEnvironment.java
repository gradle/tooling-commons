package com.gradleware.tooling.domain.model;

import com.gradleware.tooling.domain.model.generic.DomainObject;

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
     * @since 1.0-milestone-8
     */
    OmniGradleEnvironment getGradle();

    /**
     * Returns information about the Gradle environment.
     *
     * @return the Gradle environment
     */
    DomainObject<GradleEnvironmentFields> getGradleModel();

    /**
     * Returns information about the Java environment.
     *
     * @return the Java environment
     * @since 1.0-milestone-8
     */
    OmniJavaEnvironment getJava();

    /**
     * Returns information about the Java environment.
     *
     * @return the Java environment
     */
    DomainObject<JavaEnvironmentFields> getJavaModel();

}
