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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingclient.Request;
import com.gradleware.tooling.toolingutils.ImmutableCollection;

import java.io.File;
import java.util.List;

/**
 * Container to hold those attributes of a {@link com.gradleware.tooling.toolingclient.Request} that must not change between request invocations if the semantics of how the build is
 * executed must not changed.
 */
public final class FixedRequestAttributes {

    private final File projectDir;
    private final File gradleUserHome;
    private final GradleDistribution gradleDistribution;
    private final File javaHome;
    private final ImmutableList<String> jvmArguments;
    private final ImmutableList<String> arguments;

    public FixedRequestAttributes(File projectDir, File gradleUserHome, GradleDistribution gradleDistribution, File javaHome, List<String> jvmArguments, List<String> arguments) {
        this.projectDir = projectDir;
        this.gradleUserHome = gradleUserHome;
        this.gradleDistribution = gradleDistribution;
        this.javaHome = javaHome;
        this.jvmArguments = ImmutableList.copyOf(jvmArguments);
        this.arguments = ImmutableList.copyOf(arguments);
    }

    @SuppressWarnings("UnusedDeclaration")
    public File getProjectDir() {
        return this.projectDir;
    }

    @SuppressWarnings("UnusedDeclaration")
    public File getGradleUserHome() {
        return this.gradleUserHome;
    }

    @SuppressWarnings("UnusedDeclaration")
    public GradleDistribution getGradleDistribution() {
        return this.gradleDistribution;
    }

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

    public void apply(Request<?> request) {
        request.projectDir(this.projectDir);
        request.gradleUserHomeDir(this.gradleUserHome);
        request.gradleDistribution(this.gradleDistribution);
        request.javaHomeDir(this.javaHome);
        request.jvmArguments(this.jvmArguments.toArray(new String[this.jvmArguments.size()]));
        request.arguments(this.arguments.toArray(new String[this.arguments.size()]));
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
