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

package org.gradle.tooling.composite.internal.dist;

import java.io.File;

/**
 * Represents a directory containing a valid Gradle installation.
 * 
 * @author Benjamin Muschko
 */
public class InstalledGradleDistribution implements GradleDistribution {

    private final File gradleHome;

    public InstalledGradleDistribution(File gradleHome) {
        this.gradleHome = gradleHome;
    }

    public File getGradleHome() {
        return gradleHome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InstalledGradleDistribution that = (InstalledGradleDistribution) o;

        return gradleHome != null ? gradleHome.equals(that.gradleHome) : that.gradleHome == null;

    }

    @Override
    public int hashCode() {
        return gradleHome != null ? gradleHome.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("installed Gradle distribution: %s", gradleHome.getAbsolutePath());
    }
}
