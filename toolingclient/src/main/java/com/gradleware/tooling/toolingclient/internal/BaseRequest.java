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

package com.gradleware.tooling.toolingclient.internal;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.events.test.TestProgressListener;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingclient.GradleDistribution;

/**
 * Internal base class for all tooling client request objects.
 *
 * @param <T> the result type
 * @param <SELF> self reference
 * @author Etienne Studer
 */
abstract class BaseRequest<T, SELF extends BaseRequest<T, SELF>> implements InspectableRequest<T> {

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
    private ImmutableList<TestProgressListener> testProgressListeners;
    private CancellationToken cancellationToken;

    BaseRequest(ExecutableToolingClient toolingClient) {
        this.toolingClient = Preconditions.checkNotNull(toolingClient);
        this.gradleDistribution = GradleDistribution.fromBuild();
        this.jvmArguments = ImmutableList.of();
        this.arguments = ImmutableList.of();
        this.progressListeners = ImmutableList.of();
        this.testProgressListeners = ImmutableList.of();
        this.cancellationToken = GradleConnector.newCancellationTokenSource().token();
    }

    ExecutableToolingClient getToolingClient() {
        return this.toolingClient;
    }

    @Override
    public SELF projectDir(File projectDir) {
        this.projectDir = projectDir;
        return getThis();
    }

    @Override
    public File getProjectDir() {
        return this.projectDir;
    }

    @Override
    public SELF gradleUserHomeDir(File gradleUserHomeDir) {
        this.gradleUserHomeDir = gradleUserHomeDir;
        return getThis();
    }

    @Override
    public File getGradleUserHomeDir() {
        return this.gradleUserHomeDir;
    }

    @Override
    public SELF gradleDistribution(GradleDistribution gradleDistribution) {
        this.gradleDistribution = Preconditions.checkNotNull(gradleDistribution);
        return getThis();
    }

    @Override
    public GradleDistribution getGradleDistribution() {
        return this.gradleDistribution;
    }

    @Override
    public SELF colorOutput(boolean colorOutput) {
        this.colorOutput = colorOutput;
        return getThis();
    }

    @Override
    public boolean isColorOutput() {
        return this.colorOutput;
    }

    @Override
    public SELF standardOutput(OutputStream outputStream) {
        this.standardOutput = outputStream;
        return getThis();
    }

    @Override
    public OutputStream getStandardOutput() {
        return this.standardOutput;
    }

    @Override
    public SELF standardError(OutputStream outputStream) {
        this.standardError = outputStream;
        return getThis();
    }

    @Override
    public OutputStream getStandardError() {
        return this.standardError;
    }

    @Override
    public SELF standardInput(InputStream inputStream) {
        this.standardInput = inputStream;
        return getThis();
    }

    @Override
    public InputStream getStandardInput() {
        return this.standardInput;
    }

    @Override
    public SELF javaHomeDir(File javaHomeDir) {
        this.javaHomeDir = javaHomeDir;
        return getThis();
    }

    @Override
    public File getJavaHomeDir() {
        return this.javaHomeDir;
    }

    @Override
    public SELF jvmArguments(String... jvmArguments) {
        this.jvmArguments = ImmutableList.copyOf(jvmArguments);
        return getThis();
    }

    @Override
    public String[] getJvmArguments() {
        return this.jvmArguments.toArray(new String[this.jvmArguments.size()]);
    }

    @Override
    public SELF arguments(String... arguments) {
        this.arguments = ImmutableList.copyOf(arguments);
        return getThis();
    }

    @Override
    public String[] getArguments() {
        return this.arguments.toArray(new String[this.arguments.size()]);
    }

    @Override
    public SELF progressListeners(ProgressListener... listeners) {
        this.progressListeners = ImmutableList.copyOf(listeners);
        return getThis();
    }

    @Override
    public ProgressListener[] getProgressListeners() {
        return this.progressListeners.toArray(new ProgressListener[this.progressListeners.size()]);
    }

    @Override
    public SELF testProgressListeners(TestProgressListener... listeners) {
        this.testProgressListeners = ImmutableList.copyOf(listeners);
        return getThis();
    }

    public TestProgressListener[] getTestProgressListeners() {
        return this.testProgressListeners.toArray(new TestProgressListener[this.testProgressListeners.size()]);
    }

    @Override
    public SELF cancellationToken(CancellationToken cancellationToken) {
        this.cancellationToken = Preconditions.checkNotNull(cancellationToken);
        return getThis();
    }

    @Override
    public CancellationToken getCancellationToken() {
        return this.cancellationToken;
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
                testProgressListeners(getTestProgressListeners()).
                cancellationToken(getCancellationToken());
    }

    abstract SELF getThis();

}
