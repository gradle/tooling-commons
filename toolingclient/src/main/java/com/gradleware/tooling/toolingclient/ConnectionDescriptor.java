package com.gradleware.tooling.toolingclient;

import java.io.File;

public interface ConnectionDescriptor {

    /**
     * Specifies the working directory to use.
     *
     * @param projectDir the working directory
     * @return this
     */
    ConnectionDescriptor projectDir(File projectDir);

    /**
     * Specifies the user's Gradle home directory to use. Defaults to {@code ~/.gradle}.
     *
     * @param gradleUserHomeDir the user's Gradle home directory to use
     * @return this
     */
    ConnectionDescriptor gradleUserHomeDir(File gradleUserHomeDir);

    /**
     * Specifies the Gradle distribution to use. Defaults to a project-specific Gradle version.
     *
     * @param gradleDistribution the Gradle distribution to use
     * @return this
     */
    ConnectionDescriptor gradleDistribution(GradleDistribution gradleDistribution);
}
