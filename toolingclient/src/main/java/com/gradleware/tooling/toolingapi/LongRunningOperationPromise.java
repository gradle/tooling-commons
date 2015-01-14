package com.gradleware.tooling.toolingapi;

import com.google.common.base.Preconditions;
import com.gradleware.tooling.toolingapi.internal.ResultHandlerPromise;
import org.gradle.tooling.BuildActionExecuter;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.LongRunningOperation;
import org.gradle.tooling.ModelBuilder;

/**
 * Promise in the context of long running operations, i.e. getting a model, launching a build, executing an action.
 *
 * @see LongRunningOperation
 * @since 2.3
 */
public abstract class LongRunningOperationPromise<T> {

    /**
     * Asynchronously runs the model builder and returns its promise.
     *
     * @param modelBuilder the modeler builder to run
     * @param <T> the type of model queried and returned
     * @return the promise of the long running operation
     * @since 2.3
     */
    public static <T> LongRunningOperationPromise<T> forModelBuilder(ModelBuilder<T> modelBuilder) {
        Preconditions.checkNotNull(modelBuilder);
        ResultHandlerPromise<T> promise = new ResultHandlerPromise<T>();
        modelBuilder.get(promise.getResultHandler());
        return promise;
    }

    /**
     * Asynchronously runs the build launcher and returns its promise.
     *
     * @param buildLauncher the build launcher to run
     * @return the promise of the long running operation
     * @since 2.3
     */
    public static LongRunningOperationPromise<Void> forBuildLauncher(BuildLauncher buildLauncher) {
        Preconditions.checkNotNull(buildLauncher);
        ResultHandlerPromise<Void> promise = new ResultHandlerPromise<Void>();
        buildLauncher.run(promise.getResultHandler());
        return promise;
    }

    /**
     * Asynchronously runs the build action executor and returns its promise.
     *
     * @param buildActionExecuter the build action executor to run
     * @param <T> the type of model queried and returned
     * @return the promise of the long running operation
     * @since 2.3
     */
    public static <T> LongRunningOperationPromise<T> forBuildActionExecuter(BuildActionExecuter<T> buildActionExecuter) {
        Preconditions.checkNotNull(buildActionExecuter);
        ResultHandlerPromise<T> promise = new ResultHandlerPromise<T>();
        buildActionExecuter.run(promise.getResultHandler());
        return promise;
    }

    /**
     * The action to invoke when the long running operation completes successfully. The action is invoked immediately if the operation has already completed. It is left to the
     * implementation whether to support multiple handlers or just the most recent one.
     *
     * @param completeHandler the handler to invoke in case of successful completion of the operation
     * @return the promise
     * @since 2.3
     */
    public abstract LongRunningOperationPromise<T> onComplete(Consumer<? super T> completeHandler);

    /**
     * The action to invoke when the long running operation fails. The action is invoked immediately if the operation has already failed. It is left to the implementation whether
     * to support multiple handlers or just the most recent one.
     *
     * @param failureHandler the handler to invoke in case of a failure while running the operation
     * @return the promise
     * @since 2.3
     */
    public abstract LongRunningOperationPromise<T> onFailure(Consumer<? super GradleConnectionException> failureHandler);

}
