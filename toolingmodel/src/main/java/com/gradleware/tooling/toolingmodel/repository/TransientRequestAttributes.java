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

import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingclient.Request;
import com.gradleware.tooling.toolingutils.ImmutableCollection;
import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.TestProgressListener;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Container to hold those attributes of a {@link Request} that do change between request invocations.
 */
public final class TransientRequestAttributes {

    private final boolean colorOutput;
    private final OutputStream standardOutput;
    private final OutputStream standardError;
    private final InputStream standardInput;
    private final ImmutableList<ProgressListener> progressListeners;
    private final ImmutableList<TestProgressListener> testProgressListeners;
    private final CancellationToken cancellationToken;

    public TransientRequestAttributes(boolean colorOutput, OutputStream standardOutput, OutputStream standardError, InputStream standardInput,
                                      List<ProgressListener> progressListeners, List<TestProgressListener> testProgressListeners, CancellationToken cancellationToken) {
        this.colorOutput = colorOutput;
        this.standardOutput = standardOutput;
        this.standardError = standardError;
        this.standardInput = standardInput;
        this.progressListeners = ImmutableList.copyOf(progressListeners);
        this.testProgressListeners = ImmutableList.copyOf(testProgressListeners);
        this.cancellationToken = cancellationToken;
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean isColorOutput() {
        return this.colorOutput;
    }

    @SuppressWarnings("UnusedDeclaration")
    public OutputStream getStandardOutput() {
        return this.standardOutput;
    }

    @SuppressWarnings("UnusedDeclaration")
    public OutputStream getStandardError() {
        return this.standardError;
    }

    @SuppressWarnings("UnusedDeclaration")
    public InputStream getStandardInput() {
        return this.standardInput;
    }

    @ImmutableCollection
    @SuppressWarnings("UnusedDeclaration")
    public List<ProgressListener> getProgressListeners() {
        return this.progressListeners;
    }

    @ImmutableCollection
    @SuppressWarnings("UnusedDeclaration")
    public ImmutableList<TestProgressListener> getTestProgressListeners() {
        return this.testProgressListeners;
    }

    @SuppressWarnings("UnusedDeclaration")
    public CancellationToken getCancellationToken() {
        return this.cancellationToken;
    }

    public void apply(Request<?> request) {
        request.colorOutput(this.colorOutput);
        request.standardOutput(this.standardOutput);
        request.standardError(this.standardError);
        request.standardInput(this.standardInput);
        request.progressListeners(this.progressListeners.toArray(new ProgressListener[this.progressListeners.size()]));
        request.testProgressListeners(this.testProgressListeners.toArray(new TestProgressListener[this.testProgressListeners.size()]));
        request.cancellationToken(this.cancellationToken);
    }

}
