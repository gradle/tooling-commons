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

import org.gradle.api.Action
import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.composite.CompositeBuildConnection
import org.gradle.tooling.internal.consumer.ConnectionParameters
import org.gradle.tooling.internal.consumer.async.AsyncConsumerActionExecutor
import spock.lang.Specification

class DefaultCompositeBuildConnectionTest extends Specification {
    AsyncConsumerActionExecutor connection = Mock(AsyncConsumerActionExecutor)
    ConnectionParameters parameters = Mock(ConnectionParameters)

    def "cannot create composite with no participants"() {
        when:
        new DefaultCompositeBuildConnection(connection, parameters, [:])

        then:
        Throwable t = thrown(IllegalStateException)
        t.message == 'A composite build requires at least one participating project.'
    }

    def "cannot request model that is not an interface"(Action<CompositeBuildConnection> configurer) {
        given:
        def projectConnection = Mock(ProjectConnection)
        def participant = Mock(DefaultCompositeParticipant)
        def projectConnections = [(participant) : projectConnection]

        when:
        DefaultCompositeBuildConnection defaultCompositeBuildConnection = new DefaultCompositeBuildConnection(connection, parameters, projectConnections)
        configurer.execute(defaultCompositeBuildConnection)

        then:
        Throwable t = thrown(IllegalArgumentException)
        t.message == "Cannot fetch a model of type 'java.lang.String' as this type is not an interface."

        where:
        configurer << [{ it.getModels(String) },
                       { it.models(String) }]
    }

    def "cannot request model for unknown model"(Action<CompositeBuildConnection> configurer) {
        given:
        def projectConnection = Mock(ProjectConnection)
        def participant = Mock(DefaultCompositeParticipant)
        def projectConnections = [(participant) : projectConnection]

        when:
        DefaultCompositeBuildConnection defaultCompositeBuildConnection = new DefaultCompositeBuildConnection(connection, parameters, projectConnections,)
        configurer.execute(defaultCompositeBuildConnection)

        then:
        Throwable t = thrown(IllegalArgumentException)
        t.message == "The only supported model for a Gradle composite is EclipseProject.class."

        where:
        configurer << [{ it.getModels(List) },
                       { it.models(List) }]
    }

    def "stops connection and closes all participants"() {
        given:
        def projectConnection1 = Mock(ProjectConnection)
        def projectConnection2 = Mock(ProjectConnection)
        def participant1 = Mock(DefaultCompositeParticipant)
        def participant2 = Mock(DefaultCompositeParticipant)
        def projectConnections = [(participant1) : projectConnection1, (participant2) : projectConnection2]

        when:
        DefaultCompositeBuildConnection defaultCompositeBuildConnection = new DefaultCompositeBuildConnection(connection, parameters, projectConnections)
        defaultCompositeBuildConnection.close()

        then:
        1 * connection.stop()
        1 * projectConnection1.close()
        1 * projectConnection2.close()
    }

    def "closes all participants even if stopping the connection failed"() {
        given:
        def projectConnection1 = Mock(ProjectConnection)
        def projectConnection2 = Mock(ProjectConnection)
        def participant1 = Mock(DefaultCompositeParticipant)
        def participant2 = Mock(DefaultCompositeParticipant)
        def projectConnections = [(participant1) : projectConnection1, (participant2) : projectConnection2]

        when:
        DefaultCompositeBuildConnection defaultCompositeBuildConnection = new DefaultCompositeBuildConnection(connection, parameters, projectConnections)
        defaultCompositeBuildConnection.close()

        then:
        1 * connection.stop() >> { throw new RuntimeException('Something went wrong') }
        1 * projectConnection1.close()
        1 * projectConnection2.close()
        Throwable t = thrown(RuntimeException)
        t.message == 'Something went wrong'
    }

    def "propagates exception if thrown when closing participant"() {
        given:
        def projectConnection = Mock(ProjectConnection)
        def participant = Mock(DefaultCompositeParticipant)
        def projectConnections = [(participant) : projectConnection]

        when:
        DefaultCompositeBuildConnection defaultCompositeBuildConnection = new DefaultCompositeBuildConnection(connection, parameters, projectConnections)
        defaultCompositeBuildConnection.close()

        then:
        1 * connection.stop()
        1 * projectConnection.close() >> { throw new RuntimeException('Something went wrong') }
        Throwable t = thrown(RuntimeException)
        t.message == 'Something went wrong'
    }

    def "attempts to close all participants even though exception is thrown"() {
        given:
        def projectConnection1 = Mock(ProjectConnection)
        def projectConnection2 = Mock(ProjectConnection)
        def projectConnection3 = Mock(ProjectConnection)
        def participant1 = Mock(DefaultCompositeParticipant)
        def participant2 = Mock(DefaultCompositeParticipant)
        def participant3 = Mock(DefaultCompositeParticipant)
        def projectConnections = [(participant1) : projectConnection1, (participant2) : projectConnection2, (participant3) : projectConnection3]

        when:
        DefaultCompositeBuildConnection defaultCompositeBuildConnection = new DefaultCompositeBuildConnection(connection, parameters, projectConnections)
        defaultCompositeBuildConnection.close()

        then:
        1 * connection.stop()
        1 * projectConnection1.close() >> { throw new RuntimeException('Something went wrong') }
        1 * projectConnection2.close()
        1 * projectConnection3.close()
        Throwable t = thrown(RuntimeException)
        t.message == 'Something went wrong'
    }

    def "throws the first encountered exception when closing participants"() {
        given:
        def projectConnection1 = Mock(ProjectConnection)
        def projectConnection2 = Mock(ProjectConnection)
        def projectConnection3 = Mock(ProjectConnection)
        def participant1 = Mock(DefaultCompositeParticipant)
        def participant2 = Mock(DefaultCompositeParticipant)
        def participant3 = Mock(DefaultCompositeParticipant)
        def projectConnections = [(participant1) : projectConnection1, (participant2) : projectConnection2, (participant3) : projectConnection3]

        when:
        DefaultCompositeBuildConnection defaultCompositeBuildConnection = new DefaultCompositeBuildConnection(connection, parameters, projectConnections)
        defaultCompositeBuildConnection.close()

        then:
        1 * connection.stop()
        1 * projectConnection1.close() >> { throw new RuntimeException('Something went wrong') }
        1 * projectConnection2.close() >> { throw new RuntimeException('More failures') }
        1 * projectConnection3.close()
        Throwable t = thrown(RuntimeException)
        t.message == 'Something went wrong'
    }
}
