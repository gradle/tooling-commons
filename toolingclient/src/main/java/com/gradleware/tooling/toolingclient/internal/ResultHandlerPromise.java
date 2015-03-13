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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.gradleware.tooling.toolingclient.Consumer;
import com.gradleware.tooling.toolingclient.LongRunningOperationPromise;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ResultHandler;

/**
 * Internal implementation of the {@link LongRunningOperationPromise} API.
 */
public final class ResultHandlerPromise<T> extends LongRunningOperationPromise<T> {

    private final PromiseCompatibleResultHandler<T> resultHandler;

    public ResultHandlerPromise() {
        this.resultHandler = new PromiseCompatibleResultHandler<T>();
    }

    public ResultHandler<T> getResultHandler() {
        return this.resultHandler;
    }

    @Override
    public LongRunningOperationPromise<T> onComplete(Consumer<? super T> completeHandler) {
        Preconditions.checkNotNull(completeHandler);
        this.resultHandler.invokeOnComplete(completeHandler);
        return this;
    }

    @Override
    public LongRunningOperationPromise<T> onFailure(Consumer<? super GradleConnectionException> failureHandler) {
        Preconditions.checkNotNull(failureHandler);
        this.resultHandler.invokeOnFailure(failureHandler);
        return this;
    }

    private static final class PromiseCompatibleResultHandler<T> implements ResultHandler<T> {

        private final Object LOCK = new Object();

        private Optional<? extends Consumer<? super T>> completeHandler;
        private Optional<Optional<T>> completeResult;
        private Optional<? extends Consumer<? super GradleConnectionException>> failureHandler;
        private Optional<GradleConnectionException> failureException;

        public PromiseCompatibleResultHandler() {
            this.completeHandler = Optional.absent();
            this.completeResult = Optional.absent();
            this.failureHandler = Optional.absent();
            this.failureException = Optional.absent();
        }

        @Override
        public void onComplete(T result) {
            Consumer<? super T> handler;
            synchronized (this.LOCK) {
                this.completeResult = Optional.of(Optional.fromNullable(result));
                handler = this.completeHandler.orNull();
            }

            if (handler != null) {
                handler.accept(result);
            }
        }

        @Override
        public void onFailure(GradleConnectionException exception) {
            Consumer<? super GradleConnectionException> handler;
            synchronized (this.LOCK) {
                this.failureException = Optional.of(exception);
                handler = this.failureHandler.orNull();
            }

            if (handler != null) {
                handler.accept(exception);
            }
        }

        private void invokeOnComplete(Consumer<? super T> completeHandler) {
            Optional<T> result;
            synchronized (this.LOCK) {
                this.completeHandler = Optional.of(completeHandler);
                result = this.completeResult.orNull();
            }

            if (result != null) {
                completeHandler.accept(result.orNull());
            }
        }

        private void invokeOnFailure(Consumer<? super GradleConnectionException> failureHandler) {
            GradleConnectionException exception;
            synchronized (this.LOCK) {
                this.failureHandler = Optional.of(failureHandler);
                exception = this.failureException.orNull();
            }

            if (exception != null) {
                failureHandler.accept(exception);
            }
        }

    }

}
