package org.gradle.tooling.composite.internal

import org.gradle.internal.concurrent.ExecutorFactory
import org.gradle.tooling.composite.CompositeBuildConnector
import org.gradle.tooling.internal.consumer.ConnectionParameters
import org.gradle.tooling.internal.consumer.Distribution
import org.gradle.tooling.internal.consumer.LoggingProvider
import org.gradle.tooling.internal.consumer.loader.ToolingImplementationLoader
import spock.lang.Specification

class CompositeBuildConnectorFactoryTest extends Specification {

    ToolingImplementationLoader toolingImplementationLoader = Mock(ToolingImplementationLoader)
    ExecutorFactory executorFactory = Mock(ExecutorFactory)
    LoggingProvider loggingProvider = Mock(LoggingProvider)
    CompositeBuildConnectorFactory factory = new CompositeBuildConnectorFactory(toolingImplementationLoader, executorFactory, loggingProvider)

    def "can create new instance"() {
        Distribution distribution = Mock(Distribution)
        ConnectionParameters connectionParameters = Mock(ConnectionParameters)

        when:
        CompositeBuildConnector connector = factory.create(distribution, connectionParameters)

        then:
        connector instanceof DefaultCompositeBuildConnector
    }
}
