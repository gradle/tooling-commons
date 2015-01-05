package com.gradleware.tooling.toolingapi.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingapi.GradleDistribution;
import com.gradleware.tooling.toolingapi.Request;
import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressListener;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Internal base class for all tooling client request objects.
 */
abstract class BaseRequest<T, SELF extends BaseRequest<T, SELF>> implements Request<T>, InspectableRequest<T> {

    private final ExecutableToolingClient toolingClient;

    private File projectDir;
    private File gradleUserHomeDir;
    private GradleDistribution gradleDistribution;
    private boolean colorOutput;
    private OutputStream standardOutput;
    private OutputStream standardError;
    private InputStream standardInput;
    private File javaHomeDir;
    private ImmutableList<String> jvmArguments;
    private ImmutableList<String> arguments;
    private ImmutableList<ProgressListener> progressListeners;
    private CancellationToken cancellationToken;

    BaseRequest(ExecutableToolingClient toolingClient) {
        this.toolingClient = Preconditions.checkNotNull(toolingClient);
        this.gradleDistribution = GradleDistribution.fromBuild();
        this.jvmArguments = ImmutableList.of();
        this.arguments = ImmutableList.of();
        this.progressListeners = ImmutableList.of();
        this.cancellationToken = GradleConnector.newCancellationTokenSource().token();
    }

    ExecutableToolingClient getToolingClient() {
        return toolingClient;
    }

    @Override
    public SELF projectDir(File projectDir) {
        this.projectDir = projectDir;
        return getThis();
    }

    @Override
    public File getProjectDir() {
        return projectDir;
    }

    @Override
    public SELF gradleUserHomeDir(File gradleUserHomeDir) {
        this.gradleUserHomeDir = gradleUserHomeDir;
        return getThis();
    }

    @Override
    public File getGradleUserHomeDir() {
        return gradleUserHomeDir;
    }

    @Override
    public SELF gradleDistribution(GradleDistribution gradleDistribution) {
        this.gradleDistribution = Preconditions.checkNotNull(gradleDistribution);
        return getThis();
    }

    @Override
    public GradleDistribution getGradleDistribution() {
        return gradleDistribution;
    }

    @Override
    public SELF colorOutput(boolean colorOutput) {
        this.colorOutput = colorOutput;
        return getThis();
    }

    @Override
    public boolean isColorOutput() {
        return colorOutput;
    }

    @Override
    public SELF standardOutput(OutputStream outputStream) {
        this.standardOutput = outputStream;
        return getThis();
    }

    @Override
    public OutputStream getStandardOutput() {
        return standardOutput;
    }

    @Override
    public SELF standardError(OutputStream outputStream) {
        this.standardError = outputStream;
        return getThis();
    }

    @Override
    public OutputStream getStandardError() {
        return standardError;
    }

    @Override
    public SELF standardInput(InputStream inputStream) {
        this.standardInput = inputStream;
        return getThis();
    }

    @Override
    public InputStream getStandardInput() {
        return standardInput;
    }

    @Override
    public SELF javaHomeDir(File javaHomeDir) {
        this.javaHomeDir = javaHomeDir;
        return getThis();
    }

    @Override
    public File getJavaHomeDir() {
        return javaHomeDir;
    }

    @Override
    public SELF jvmArguments(String... jvmArguments) {
        this.jvmArguments = ImmutableList.copyOf(jvmArguments);
        return getThis();
    }

    @Override
    public String[] getJvmArguments() {
        return jvmArguments.toArray(new String[jvmArguments.size()]);
    }

    @Override
    public SELF arguments(String... arguments) {
        this.arguments = ImmutableList.copyOf(arguments);
        return getThis();
    }

    @Override
    public String[] getArguments() {
        return arguments.toArray(new String[arguments.size()]);
    }

    @Override
    public SELF progressListeners(ProgressListener... listeners) {
        this.progressListeners = ImmutableList.copyOf(listeners);
        return getThis();
    }

    @Override
    public ProgressListener[] getProgressListeners() {
        return progressListeners.toArray(new ProgressListener[progressListeners.size()]);
    }

    @Override
    public SELF cancellationToken(CancellationToken cancellationToken) {
        this.cancellationToken = Preconditions.checkNotNull(cancellationToken);
        return getThis();
    }

    @Override
    public CancellationToken getCancellationToken() {
        return cancellationToken;
    }

    <S, S_SELF extends BaseRequest<S, S_SELF>> S_SELF copy(BaseRequest<S, S_SELF> request) {
        return request.projectDir(getProjectDir()).
                gradleUserHomeDir(getGradleUserHomeDir()).
                gradleDistribution(getGradleDistribution()).
                colorOutput(isColorOutput()).
                standardOutput(getStandardOutput()).
                standardError(getStandardError()).
                standardInput(getStandardInput()).
                javaHomeDir(getJavaHomeDir()).
                jvmArguments(getJvmArguments()).
                arguments(getArguments()).
                progressListeners(getProgressListeners()).
                cancellationToken(getCancellationToken());
    }

    abstract SELF getThis();

}
