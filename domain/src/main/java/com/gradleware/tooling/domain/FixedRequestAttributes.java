package com.gradleware.tooling.domain;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingapi.GradleDistribution;
import com.gradleware.tooling.toolingapi.Request;

import java.io.File;
import java.util.List;

/**
 * Container to hold those attributes of a {@link com.gradleware.tooling.toolingapi.Request} that must not change between request invocations if the semantics of how the build is
 * executed must not changed.
 */
public final class FixedRequestAttributes {

    private final File projectDir;
    private final File gradleUserHomeDir;
    private final GradleDistribution gradleDistribution;
    private final File javaHome;
    private final ImmutableList<String> jvmArguments;
    private final ImmutableList<String> arguments;

    public FixedRequestAttributes(File projectDir, File gradleUserHomeDir, GradleDistribution gradleDistribution, File javaHome, List<String> jvmArguments, List<String> arguments) {
        this.projectDir = projectDir;
        this.gradleUserHomeDir = gradleUserHomeDir;
        this.gradleDistribution = gradleDistribution;
        this.javaHome = javaHome;
        this.jvmArguments = ImmutableList.copyOf(jvmArguments);
        this.arguments = ImmutableList.copyOf(arguments);
    }

    @SuppressWarnings("UnusedDeclaration")
    public File getProjectDir() {
        return projectDir;
    }

    @SuppressWarnings("UnusedDeclaration")
    public File getGradleUserHomeDir() {
        return gradleUserHomeDir;
    }

    @SuppressWarnings("UnusedDeclaration")
    public GradleDistribution getGradleDistribution() {
        return gradleDistribution;
    }

    @SuppressWarnings("UnusedDeclaration")
    public File getJavaHome() {
        return javaHome;
    }

    @SuppressWarnings("UnusedDeclaration")
    public ImmutableList<String> getJvmArguments() {
        return jvmArguments;
    }

    @SuppressWarnings("UnusedDeclaration")
    public ImmutableList<String> getArguments() {
        return arguments;
    }

    public void apply(Request<?> request) {
        request.projectDir(projectDir);
        request.gradleUserHomeDir(gradleUserHomeDir);
        request.gradleDistribution(gradleDistribution);
        request.javaHomeDir(javaHome);
        request.jvmArguments(jvmArguments.toArray(new String[jvmArguments.size()]));
        request.arguments(arguments.toArray(new String[arguments.size()]));
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
        return Objects.equal(projectDir, that.projectDir) &&
                Objects.equal(gradleUserHomeDir, that.gradleUserHomeDir) &&
                Objects.equal(gradleDistribution, that.gradleDistribution) &&
                Objects.equal(javaHome, that.javaHome) &&
                Objects.equal(jvmArguments, that.jvmArguments) &&
                Objects.equal(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
                projectDir,
                gradleUserHomeDir,
                gradleDistribution,
                javaHome,
                jvmArguments,
                arguments);
    }

}
