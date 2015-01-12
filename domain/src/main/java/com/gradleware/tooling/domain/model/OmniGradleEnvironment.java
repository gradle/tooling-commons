package com.gradleware.tooling.domain.model;

/**
 * Informs about the Gradle environment.
 */
public interface OmniGradleEnvironment {

    /**
     * Returns the Gradle version.
     *
     * @since 1.0-milestone-8
     */
    String getGradleVersion();

}
