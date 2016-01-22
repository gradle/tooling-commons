/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gradleware.tooling.toolingclient;

import java.io.File;

/**
 * Encapsulates the information required to connect to a Gradle project.
 *
 * @author Stefan Oehme
 */
public final class GradleBuildIdentifier {

    public static GradleBuildIdentifier withProjectDir(File projectDir) {
        return new GradleBuildIdentifier(projectDir);
    }

    private final File projectDir;
    private File gradleUserHomeDir;
    private GradleDistribution gradleDistribution;

    private GradleBuildIdentifier(File projectDir) {
        this.projectDir = projectDir;
    }

    public File getProjectDir() {
        return this.projectDir;
    }

    /**
     * Specifies the user's Gradle home directory to use. Defaults to {@code ~/.gradle}.
     *
     * @param gradleUserHomeDir the user's Gradle home directory to use
     * @return this
     */
    public GradleBuildIdentifier gradleUserHomeDir(File gradleUserHomeDir) {
        this.gradleUserHomeDir = gradleUserHomeDir;
        return this;
    }

    public File getGradleUserHomeDir() {
        return this.gradleUserHomeDir;
    }

    /**
     * Specifies the Gradle distribution to use. Defaults to a project-specific Gradle version.
     *
     * @param gradleDistribution the Gradle distribution to use
     * @return this
     */
    public GradleBuildIdentifier gradleDistribution(GradleDistribution gradleDistribution) {
        this.gradleDistribution = gradleDistribution;
        return this;
    }

    public GradleDistribution getGradleDistribution() {
        return this.gradleDistribution;
    }
}
