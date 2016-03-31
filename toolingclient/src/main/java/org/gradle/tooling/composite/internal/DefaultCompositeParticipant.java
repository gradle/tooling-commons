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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import org.gradle.tooling.composite.CompositeParticipant;
import org.gradle.tooling.composite.internal.dist.GradleDistribution;
import org.gradle.tooling.composite.internal.dist.InstalledGradleDistribution;
import org.gradle.tooling.composite.internal.dist.URILocatedGradleDistribution;
import org.gradle.tooling.composite.internal.dist.VersionBasedGradleDistribution;

import java.io.File;
import java.net.URI;
import java.util.List;

/**
 * The default implementation of a composite participant.
 *
 * @author Benjamin Muschko
 */
public class DefaultCompositeParticipant implements CompositeParticipant {
    private final File rootProjectDirectory;
    private GradleDistribution gradleDistribution;
    private File javaHome;
    private List<String> arguments;
    private List<String> jvmArguments;


    public DefaultCompositeParticipant(File rootProjectDirectory) {
        this.rootProjectDirectory = rootProjectDirectory;
    }

    @Override
    public File getRootProjectDirectory() {
        return this.rootProjectDirectory;
    }

    @Override
    public void useInstallation(File gradleHome) {
        this.gradleDistribution = new InstalledGradleDistribution(gradleHome);
    }

    @Override
    public void useGradleVersion(String gradleVersion) {
        this.gradleDistribution = new VersionBasedGradleDistribution(gradleVersion);
    }

    @Override
    public void useDistribution(URI location) {
        this.gradleDistribution = new URILocatedGradleDistribution(location);
    }

    GradleDistribution getDistribution() {
        return this.gradleDistribution;
    }

    @Override
    public void useJavaHome(File javaHome) {
        this.javaHome = javaHome;
    }

    public File getJavaHome() {
        return this.javaHome;
    }

    @Override
    public void useArguments(List<String> arguments) {
        this.arguments = ImmutableList.copyOf(arguments);
    }

    List<String> getArguments() {
        return this.arguments;
    }

    @Override
    public void useJvmArguments(List<String> jvmArguments) {
        this.jvmArguments = ImmutableList.copyOf(jvmArguments);
    }

    List<String> getJvmArguments() {
        return this.jvmArguments;
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
        return Objects.equal(this.rootProjectDirectory, that.rootProjectDirectory) &&
                Objects.equal(this.gradleDistribution, that.gradleDistribution) &&
                Objects.equal(this.javaHome, that.javaHome) &&
                Objects.equal(this.jvmArguments, that.jvmArguments) &&
                Objects.equal(this.arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.rootProjectDirectory,
                this.gradleDistribution,
                this.javaHome,
                this.jvmArguments,
                this.arguments);
    }

    @Override
    public String toString() {
        return String.format("root project dir: %s, Gradle distribution: %s", this.rootProjectDirectory, this.gradleDistribution);
    }

    void setGradleDistribution(GradleDistribution gradleDistribution) {
        this.gradleDistribution = gradleDistribution;
    }
}
