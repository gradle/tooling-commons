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

package org.gradle.tooling.composite.internal;

import org.gradle.tooling.composite.CompositeParticipant;
import org.gradle.tooling.composite.internal.dist.GradleDistribution;
import org.gradle.tooling.composite.internal.dist.InstalledGradleDistribution;
import org.gradle.tooling.composite.internal.dist.URILocatedGradleDistribution;
import org.gradle.tooling.composite.internal.dist.VersionBasedGradleDistribution;

import java.io.File;
import java.net.URI;

/**
 * The default implementation of a composite participant.
 * 
 * @author Benjamin Muschko
 */
public class DefaultCompositeParticipant implements CompositeParticipant {
    private final File rootProjectDirectory;
    private GradleDistribution gradleDistribution;

    public DefaultCompositeParticipant(File rootProjectDirectory) {
        this.rootProjectDirectory = rootProjectDirectory;
    }

    @Override
    public File getRootProjectDirectory() {
        return rootProjectDirectory;
    }

    @Override
    public void useInstallation(File gradleHome) {
        gradleDistribution = new InstalledGradleDistribution(gradleHome);
    }

    @Override
    public void useGradleVersion(String gradleVersion) {
        gradleDistribution = new VersionBasedGradleDistribution(gradleVersion);
    }

    @Override
    public void useDistribution(URI location) {
        gradleDistribution = new URILocatedGradleDistribution(location);
    }

    GradleDistribution getDistribution() {
        return gradleDistribution;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultCompositeParticipant that = (DefaultCompositeParticipant) o;

        if (rootProjectDirectory != null ? !rootProjectDirectory.equals(that.rootProjectDirectory) : that.rootProjectDirectory != null)
            return false;
        return gradleDistribution != null ? gradleDistribution.equals(that.gradleDistribution) : that.gradleDistribution == null;

    }

    @Override
    public int hashCode() {
        int result = rootProjectDirectory != null ? rootProjectDirectory.hashCode() : 0;
        result = 31 * result + (gradleDistribution != null ? gradleDistribution.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("root project dir: %s, Gradle distribution: %s", rootProjectDirectory, gradleDistribution);
    }

    void setGradleDistribution(GradleDistribution gradleDistribution) {
        this.gradleDistribution = gradleDistribution;
    }
}
