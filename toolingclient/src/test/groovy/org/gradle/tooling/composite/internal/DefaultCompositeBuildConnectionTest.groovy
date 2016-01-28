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
        new DefaultCompositeBuildConnection(connection, parameters, [] as Set<ProjectConnection>)

        then:
        Throwable t = thrown(IllegalStateException)
        t.message == 'A composite build requires at least one participating project.'
    }

    def "cannot request model that is not an interface"(Action<CompositeBuildConnection> configurer) {
        given:
        def projectConnection = Mock(ProjectConnection)
        Set<ProjectConnection> projectConnections = [projectConnection] as Set<ProjectConnection>

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
        Set<ProjectConnection> projectConnections = [projectConnection] as Set<ProjectConnection>

        when:
        DefaultCompositeBuildConnection defaultCompositeBuildConnection = new DefaultCompositeBuildConnection(connection, parameters, projectConnections)
        configurer.execute(defaultCompositeBuildConnection)

        then:
        Throwable t = thrown(IllegalArgumentException)
        t.message == "The only supported model for a Gradle composite is EclipseProject.class."

        where:
        configurer << [{ it.getModels(List) },
                       { it.models(List) }]
    }

    def "closes all participants"() {
        given:
        def projectConnection1 = Mock(ProjectConnection)
        def projectConnection2 = Mock(ProjectConnection)
        Set<ProjectConnection> projectConnections = [projectConnection1, projectConnection2] as Set<ProjectConnection>

        when:
        DefaultCompositeBuildConnection defaultCompositeBuildConnection = new DefaultCompositeBuildConnection(connection, parameters, projectConnections)
        defaultCompositeBuildConnection.close()

        then:
        1 * projectConnection1.close()
        1 * projectConnection2.close()
    }

    def "propagates exception if thrown when closing participant"() {
        given:
        def projectConnection1 = Mock(ProjectConnection)
        Set<ProjectConnection> projectConnections = [projectConnection1] as Set<ProjectConnection>

        when:
        DefaultCompositeBuildConnection defaultCompositeBuildConnection = new DefaultCompositeBuildConnection(connection, parameters, projectConnections)
        defaultCompositeBuildConnection.close()

        then:
        1 * projectConnection1.close() >> { throw new RuntimeException('Something went wrong') }
        Throwable t = thrown(RuntimeException)
        t.message == 'Something went wrong'
    }

    def "attempts to close all participants even though exception is thrown"() {
        given:
        def projectConnection1 = Mock(ProjectConnection)
        def projectConnection2 = Mock(ProjectConnection)
        def projectConnection3 = Mock(ProjectConnection)
        Set<ProjectConnection> projectConnections = [projectConnection1, projectConnection2, projectConnection3] as Set<ProjectConnection>

        when:
        DefaultCompositeBuildConnection defaultCompositeBuildConnection = new DefaultCompositeBuildConnection(connection, parameters, projectConnections)
        defaultCompositeBuildConnection.close()

        then:
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
        Set<ProjectConnection> projectConnections = [projectConnection1, projectConnection2, projectConnection3] as Set<ProjectConnection>

        when:
        DefaultCompositeBuildConnection defaultCompositeBuildConnection = new DefaultCompositeBuildConnection(connection, parameters, projectConnections)
        defaultCompositeBuildConnection.close()

        then:
        1 * projectConnection1.close() >> { throw new RuntimeException('Something went wrong') }
        1 * projectConnection2.close() >> { throw new RuntimeException('More failures') }
        1 * projectConnection3.close()
        Throwable t = thrown(RuntimeException)
        t.message == 'Something went wrong'
    }
}
