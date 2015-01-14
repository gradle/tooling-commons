package com.gradleware.tooling.toolingclient;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.ProgressListener;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A {@code BuildActionRequest} allows to execute logic in the build process. Instances of {@code BuildActionRequest} are not thread-safe.
 * <p>
 * You use a {@code BuildActionRequest} as follows: <ul> <li>Create an instance of {@code BuildActionRequest} by calling {@link
 * ToolingClient#newBuildActionRequest(org.gradle.tooling.BuildAction)}. <li>Configure the request as appropriate. <li>Call either {@link #executeAndWait()} or {@link #execute()}
 * to execute the build action. <li>Optionally, you can reuse the request to execute the build action multiple times. </ul>
 *
 * @param <T> the type of result produced by the build action
 * @since 2.3
 */
public interface BuildActionRequest<T> extends Request<T> {

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildActionRequest<T> projectDir(File projectDir);

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildActionRequest<T> gradleUserHomeDir(File gradleUserHomeDir);

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildActionRequest<T> gradleDistribution(GradleDistribution gradleDistribution);

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildActionRequest<T> colorOutput(boolean colorOutput);

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildActionRequest<T> standardOutput(OutputStream outputStream);

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildActionRequest<T> standardError(OutputStream outputStream);

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildActionRequest<T> standardInput(InputStream inputStream);

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildActionRequest<T> javaHomeDir(File javaHomeDir);

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildActionRequest<T> jvmArguments(String... jvmArguments);

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildActionRequest<T> arguments(String... arguments);

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildActionRequest<T> progressListeners(ProgressListener... listeners);

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildActionRequest<T> cancellationToken(CancellationToken cancellationToken);

    /**
     * Derive a new build action request from this request and apply the given build action. This request and the new request do not share any state except the cancellation token.
     *
     * @param buildAction the build action to run
     * @param <S> the result type of running the build action
     * @return the new build action request instance
     */
    <S> BuildActionRequest<S> deriveForBuildAction(BuildAction<S> buildAction);

    /**
     * Executes the configured build action synchronously. Calling this method will block until the build action has been executed or a failure has occurred.
     *
     * @return the result of running the build action
     * @see org.gradle.tooling.BuildActionExecuter#run()
     * @since 2.3
     */
    @Override
    T executeAndWait();

    /**
     * Executes the configured build action asynchronously. Calling this method will return immediately. The returned promise is used to configure the success and failure
     * behavior.
     *
     * @return the promise of the result of running the build action
     * @see org.gradle.tooling.BuildActionExecuter#run(org.gradle.tooling.ResultHandler)
     * @since 2.3
     */
    @Override
    LongRunningOperationPromise<T> execute();

}
