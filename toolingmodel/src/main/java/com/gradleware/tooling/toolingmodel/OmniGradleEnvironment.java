package com.gradleware.tooling.toolingmodel;

import com.gradleware.tooling.toolingmodel.util.Maybe;

import java.io.File;

/**
 * Informs about the Gradle environment.
 */
public interface OmniGradleEnvironment {

    /**
     * Returns the Gradle user home.
     *
     * @return the Gradle user home location
     */
    Maybe<File> getGradleUserHome();

    /**
     * Returns the Gradle version.
     *
     * @return the Gradle version
     */
    String getGradleVersion();

}
