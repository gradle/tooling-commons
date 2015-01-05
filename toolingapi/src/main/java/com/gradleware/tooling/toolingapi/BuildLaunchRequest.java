package com.gradleware.tooling.toolingapi;

import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.ProgressListener;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A {@code BuildLaunchRequest} allows you to configure and execute a Gradle build. Instances of {@code BuildLaunchRequest} are not thread-safe.
 * <p>
 * You use a {@code BuildLaunchRequest} as follows: <ul> <li>Create an instance of {@code BuildLaunchRequest} by calling {@link
 * ToolingClient#newBuildLaunchRequest(LaunchableConfig)}. <li>Configure the request as appropriate. <li>Call either {@link #executeAndWait()} or {@link #execute()} to execute the
 * Gradle build. <li>Optionally, you can reuse the request to execute the Gradle build multiple times. </ul>
 *
 * @since 2.3
 */
public interface BuildLaunchRequest extends Request<Void> {

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildLaunchRequest projectDir(File projectDir);

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildLaunchRequest gradleUserHomeDir(File gradleUserHomeDir);

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildLaunchRequest gradleDistribution(GradleDistribution gradleDistribution);

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildLaunchRequest colorOutput(boolean colorOutput);

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildLaunchRequest standardOutput(OutputStream outputStream);

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildLaunchRequest standardError(OutputStream outputStream);

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildLaunchRequest standardInput(InputStream inputStream);

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildLaunchRequest javaHomeDir(File javaHomeDir);

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildLaunchRequest jvmArguments(String... jvmArguments);

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildLaunchRequest arguments(String... arguments);

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildLaunchRequest progressListeners(ProgressListener... listeners);

    /**
     * {@inheritDoc}
     *
     * @since 2.3
     */
    @Override
    BuildLaunchRequest cancellationToken(CancellationToken cancellationToken);

    /**
     * Derive a new build launch request from this request and apply the given launchables. This request and the new request do not share any state except the cancellation token.
     *
     * @param launchables the launchables to execute
     * @return the new build launch request instance
     */
    BuildLaunchRequest deriveForLaunchables(LaunchableConfig launchables);

    /**
     * Executes the configured Gradle build synchronously. Calling this method will block until the Gradle build has been executed or a failure has occurred.
     *
     * @return the void result of executing the build
     * @see org.gradle.tooling.BuildLauncher#run()
     * @since 2.3
     */
    @Override
    Void executeAndWait();

    /**
     * Executes the configured Gradle build asynchronously. Calling this method will return immediately. The returned promise is used to configure the success and failure
     * behavior.
     *
     * @return the promise of the void result of executing the build
     * @see org.gradle.tooling.BuildLauncher#run(org.gradle.tooling.ResultHandler)
     * @since 2.3
     */
    @Override
    LongRunningOperationPromise<Void> execute();

}
