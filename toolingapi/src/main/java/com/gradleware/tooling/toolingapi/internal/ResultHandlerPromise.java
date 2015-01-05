package com.gradleware.tooling.toolingapi.internal;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.gradleware.tooling.toolingapi.Consumer;
import com.gradleware.tooling.toolingapi.LongRunningOperationPromise;
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
        return resultHandler;
    }

    @Override
    public LongRunningOperationPromise<T> onComplete(Consumer<? super T> completeHandler) {
        Preconditions.checkNotNull(completeHandler);
        resultHandler.invokeOnComplete(completeHandler);
        return this;
    }

    @Override
    public LongRunningOperationPromise<T> onFailure(Consumer<? super GradleConnectionException> failureHandler) {
        Preconditions.checkNotNull(failureHandler);
        resultHandler.invokeOnFailure(failureHandler);
        return this;
    }

    private static final class PromiseCompatibleResultHandler<T> implements ResultHandler<T> {

        private final Object LOCK = new Object();

        private Optional<? extends Consumer<? super T>> completeHandler;
        private Optional<Optional<T>> completeResult;
        private Optional<? extends Consumer<? super GradleConnectionException>> failureHandler;
        private Optional<GradleConnectionException> failureException;

        public PromiseCompatibleResultHandler() {
            completeHandler = Optional.absent();
            completeResult = Optional.absent();
            failureHandler = Optional.absent();
            failureException = Optional.absent();
        }

        @Override
        public void onComplete(T result) {
            Consumer<? super T> handler;
            synchronized (LOCK) {
                this.completeResult = Optional.of(Optional.fromNullable(result));
                handler = completeHandler.orNull();
            }

            if (handler != null) {
                handler.accept(result);
            }
        }

        @Override
        public void onFailure(GradleConnectionException exception) {
            Consumer<? super GradleConnectionException> handler;
            synchronized (LOCK) {
                this.failureException = Optional.of(exception);
                handler = failureHandler.orNull();
            }

            if (handler != null) {
                handler.accept(exception);
            }
        }

        private void invokeOnComplete(Consumer<? super T> completeHandler) {
            Optional<T> result;
            synchronized (LOCK) {
                this.completeHandler = Optional.of(completeHandler);
                result = completeResult.orNull();
            }

            if (result != null) {
                completeHandler.accept(result.orNull());
            }
        }

        private void invokeOnFailure(Consumer<? super GradleConnectionException> failureHandler) {
            GradleConnectionException exception;
            synchronized (LOCK) {
                this.failureHandler = Optional.of(failureHandler);
                exception = failureException.orNull();
            }

            if (exception != null) {
                failureHandler.accept(exception);
            }
        }

    }

}
