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

package com.gradleware.tooling.toolingmodel.repository;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.gradle.api.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingclient.CompositeBuildRequest;
import com.gradleware.tooling.toolingclient.GradleBuildIdentifier;
import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingclient.SingleBuildRequest;
import com.gradleware.tooling.toolingutils.ImmutableCollection;

/**
 * Container to hold those attributes of a {@link com.gradleware.tooling.toolingclient.Request} that must not change between request invocations if the semantics of how the build
 * is executed must not changed.
 *
 * @author Etienne Studer
 */
public final class FixedRequestAttributes {

    private final File projectDir;
    private final File gradleUserHome;
    private final GradleDistribution gradleDistribution;
    private final File javaHome;
    private final ImmutableList<String> jvmArguments;
    private final ImmutableList<String> arguments;

    /**
     * All directories are canonicalized to match the behavior of Gradle.
     *
     * @param projectDir the project directory, must not be null
     * @param gradleUserHome the Gradle user home, can be null
     * @param gradleDistribution the Gradle distribution, must not be null
     * @param javaHome the Java installation, can be null
     * @param jvmArguments the JVM arguments, must not be null
     * @param arguments the build arguments, must not be null
     * @throws NullPointerException if any mandatory argument is null
     * @throws IllegalArgumentException if a directory cannot be canonicalized
     */
    public FixedRequestAttributes(File projectDir, File gradleUserHome, GradleDistribution gradleDistribution, File javaHome, List<String> jvmArguments, List<String> arguments) {
        this.projectDir = canonicalize(Preconditions.checkNotNull(projectDir));
        this.gradleUserHome = gradleUserHome == null ? null : canonicalize(gradleUserHome);
        this.gradleDistribution = Preconditions.checkNotNull(gradleDistribution);
        this.javaHome = javaHome == null ? null : canonicalize(javaHome);
        this.jvmArguments = ImmutableList.copyOf(jvmArguments);
        this.arguments = ImmutableList.copyOf(arguments);
    }

    private static File canonicalize(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public File getProjectDir() {
        return this.projectDir;
    }

    @Nullable
    @SuppressWarnings("UnusedDeclaration")
    public File getGradleUserHome() {
        return this.gradleUserHome;
    }

    @SuppressWarnings("UnusedDeclaration")
    public GradleDistribution getGradleDistribution() {
        return this.gradleDistribution;
    }

    @Nullable
    @SuppressWarnings("UnusedDeclaration")
    public File getJavaHome() {
        return this.javaHome;
    }

    @ImmutableCollection
    @SuppressWarnings("UnusedDeclaration")
    public List<String> getJvmArguments() {
        return this.jvmArguments;
    }

    @ImmutableCollection
    @SuppressWarnings("UnusedDeclaration")
    public List<String> getArguments() {
        return this.arguments;
    }

    public void apply(SingleBuildRequest<?> request) {
        request.projectDir(this.projectDir);
        request.gradleUserHomeDir(this.gradleUserHome);
        request.gradleDistribution(this.gradleDistribution);
        request.javaHomeDir(this.javaHome);
        request.jvmArguments(this.jvmArguments.toArray(new String[this.jvmArguments.size()]));
        request.arguments(this.arguments.toArray(new String[this.arguments.size()]));
    }

    public void apply(CompositeBuildRequest<?> request) {
        GradleBuildIdentifier participant = new GradleBuildIdentifier(this.projectDir, this.gradleDistribution);
        request.addParticipants(participant);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        FixedRequestAttributes that = (FixedRequestAttributes) other;
        return Objects.equal(this.projectDir, that.projectDir) &&
                Objects.equal(this.gradleUserHome, that.gradleUserHome) &&
                Objects.equal(this.gradleDistribution, that.gradleDistribution) &&
                Objects.equal(this.javaHome, that.javaHome) &&
                Objects.equal(this.jvmArguments, that.jvmArguments) &&
                Objects.equal(this.arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
                this.projectDir,
                this.gradleUserHome,
                this.gradleDistribution,
                this.javaHome,
                this.jvmArguments,
                this.arguments);
    }

}
