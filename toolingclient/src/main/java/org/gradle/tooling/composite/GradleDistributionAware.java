/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.tooling.composite;

import java.io.File;
import java.net.URI;

/**
 * Indicates that a Gradle distribution can be used for executing the composite operation.
 * <p>
 * If no distribution is set explicitly, the distribution defined by the target Gradle build is used.
 *
 * @author Benjamin Muschko
 */
public interface GradleDistributionAware {

    /**
     * Configures the consumer to make the composite request using the installation of Gradle specified.
     * <p>
     * The given file must be a directory containing a valid Gradle installation.
     *
     * @param gradleHome a valid Gradle installation
     * @see #useGradleVersion(String)
     * @see #useDistribution(URI)
     */
    void useInstallation(File gradleHome);

    /**
     * Configures the consumer to execute the build with the version of Gradle specified.
     * <p>
     * Unless previously downloaded, this method will cause the Gradle runtime for the version specified
     * to be downloaded over the Internet from Gradle's distribution servers.
     * The download will be cached beneath the Gradle User Home directory.
     *
     * @param gradleVersion the version number (e.g. "2.9")
     * @see #useInstallation(File)
     * @see #useDistribution(URI)
     */
    void useGradleVersion(String gradleVersion);

    /**
     * Configures the consumer to execute the build using the distribution of Gradle specified.
     * <p>
     * The given URI must point to a valid Gradle distribution ZIP file.
     * <p>
     * Unless previously downloaded, this method will cause the Gradle runtime at the given URI to be downloaded.
     * The download will be cached beneath the Gradle User Home directory.
     *
     * @param location a URI pointing at a valid Gradle distribution zip file
     * @see #useGradleVersion(String)
     * @see #useInstallation(File)
     */
    void useDistribution(URI location);
}
