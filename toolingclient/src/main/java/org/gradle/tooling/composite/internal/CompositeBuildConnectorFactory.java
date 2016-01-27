package org.gradle.tooling.composite.internal;

import org.gradle.internal.concurrent.ExecutorFactory;
import org.gradle.tooling.composite.CompositeBuildConnector;
import org.gradle.tooling.internal.consumer.ConnectionParameters;
import org.gradle.tooling.internal.consumer.Distribution;
import org.gradle.tooling.internal.consumer.LoggingProvider;
import org.gradle.tooling.internal.consumer.async.AsyncConsumerActionExecutor;
import org.gradle.tooling.internal.consumer.async.DefaultAsyncConsumerActionExecutor;
import org.gradle.tooling.internal.consumer.connection.ConsumerActionExecutor;
import org.gradle.tooling.internal.consumer.connection.LazyConsumerActionExecutor;
import org.gradle.tooling.internal.consumer.connection.ProgressLoggingConsumerActionExecutor;
import org.gradle.tooling.internal.consumer.connection.RethrowingErrorsConsumerActionExecutor;
import org.gradle.tooling.internal.consumer.loader.ToolingImplementationLoader;

/**
 * Create a new instance of a {@link CompositeBuildConnector}.
 *
 * @author Benjamin Muschko
 */
public class CompositeBuildConnectorFactory {

    private final ToolingImplementationLoader toolingImplementationLoader;
    private final ExecutorFactory executorFactory;
    private final LoggingProvider loggingProvider;

    public CompositeBuildConnectorFactory(ToolingImplementationLoader toolingImplementationLoader,
                                          ExecutorFactory executorFactory, LoggingProvider loggingProvider) {
        this.toolingImplementationLoader = toolingImplementationLoader;
        this.executorFactory = executorFactory;
        this.loggingProvider = loggingProvider;
    }

    public CompositeBuildConnector create(Distribution distribution, ConnectionParameters parameters) {
        ConsumerActionExecutor lazyConnection = new LazyConsumerActionExecutor(distribution, toolingImplementationLoader, loggingProvider, parameters);
        ConsumerActionExecutor progressLoggingConnection = new ProgressLoggingConsumerActionExecutor(lazyConnection, loggingProvider);
        ConsumerActionExecutor rethrowingErrorsConnection = new RethrowingErrorsConsumerActionExecutor(progressLoggingConnection);
        AsyncConsumerActionExecutor asyncConnection = new DefaultAsyncConsumerActionExecutor(rethrowingErrorsConnection, executorFactory);
        return new DefaultCompositeBuildConnector(asyncConnection, parameters);
    }
}
