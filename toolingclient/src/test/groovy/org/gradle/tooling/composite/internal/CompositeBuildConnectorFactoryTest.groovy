/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
