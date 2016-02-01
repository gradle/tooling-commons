/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.tooling.composite.internal;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.ResultHandler;
import org.gradle.tooling.events.OperationType;

/**
 * @param <T> the model type
 * @author Stefan Oehme
 */
public class DelegatingModelBuilder<T> implements ModelBuilder<T> {

    protected final ModelBuilder<T> delegate;

    public DelegatingModelBuilder(ModelBuilder<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public ModelBuilder<T> withArguments(String... arguments) {
        this.delegate.withArguments(arguments);
        return this;
    }

    @Override
    public ModelBuilder<T> withArguments(Iterable<String> arguments) {
        this.delegate.withArguments(arguments);
        return this;
    }

    @Override
    public ModelBuilder<T> setStandardOutput(OutputStream outputStream) {
        this.delegate.setStandardOutput(outputStream);
        return this;
    }

    @Override
    public ModelBuilder<T> setStandardError(OutputStream outputStream) {
        this.delegate.setStandardError(outputStream);
        return this;
    }

    @Override
    public ModelBuilder<T> setColorOutput(boolean colorOutput) {
        this.delegate.setColorOutput(colorOutput);
        return this;
    }

    @Override
    public ModelBuilder<T> setStandardInput(InputStream inputStream) {
        this.delegate.setStandardInput(inputStream);
        return this;
    }

    @Override
    public ModelBuilder<T> setJavaHome(File javaHome) {
        this.delegate.setJavaHome(javaHome);
        return this;
    }

    @Override
    public ModelBuilder<T> setJvmArguments(String... jvmArguments) {
        this.delegate.setJvmArguments(jvmArguments);
        return this;
    }

    @Override
    public ModelBuilder<T> setJvmArguments(Iterable<String> jvmArguments) {
        this.delegate.setJvmArguments(jvmArguments);
        return this;
    }

    @Override
    public ModelBuilder<T> addProgressListener(ProgressListener listener) {
        this.delegate.addProgressListener(listener);
        return this;
    }

    @Override
    public ModelBuilder<T> addProgressListener(org.gradle.tooling.events.ProgressListener listener) {
        this.delegate.addProgressListener(listener);
        return this;
    }

    @Override
    public ModelBuilder<T> addProgressListener(org.gradle.tooling.events.ProgressListener listener, Set<OperationType> eventTypes) {
        this.delegate.addProgressListener(listener, eventTypes);
        return this;
    }

    @Override
    public ModelBuilder<T> addProgressListener(org.gradle.tooling.events.ProgressListener listener, OperationType... operationTypes) {
        this.delegate.addProgressListener(listener, operationTypes);
        return this;
    }

    @Override
    public ModelBuilder<T> withCancellationToken(CancellationToken cancellationToken) {
        this.delegate.withCancellationToken(cancellationToken);
        return this;
    }

    @Override
    public ModelBuilder<T> forTasks(String... tasks) {
        this.delegate.forTasks(tasks);
        return this;
    }

    @Override
    public ModelBuilder<T> forTasks(Iterable<String> tasks) {
        this.delegate.forTasks(tasks);
        return this;
    }

    @Override
    public T get() throws GradleConnectionException, IllegalStateException {
        return this.delegate.get();
    }

    @Override
    public void get(ResultHandler<? super T> handler) throws IllegalStateException {
        this.delegate.get(handler);
    }

}
