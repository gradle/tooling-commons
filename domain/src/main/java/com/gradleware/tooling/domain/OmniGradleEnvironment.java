package com.gradleware.tooling.domain;

/**
 * Informs about the Gradle environment.
 */
public interface OmniGradleEnvironment {

    /**
     * Returns the Gradle version.
     *
     * @return the Gradle version
     */
    String getGradleVersion();

}
