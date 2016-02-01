package org.gradle.tooling.composite.internal

import org.gradle.api.Action
import org.gradle.internal.classpath.ClassPath
import org.gradle.tooling.ModelBuilder
import org.gradle.tooling.composite.CompositeParticipant
import org.gradle.tooling.internal.consumer.ConnectionParameters
import org.gradle.tooling.internal.consumer.async.AsyncConsumerActionExecutor
import org.gradle.tooling.model.UnsupportedMethodException
import org.gradle.tooling.model.eclipse.EclipseProject
import spock.lang.Specification

class EclipseModelResultSetModelBuilderTest extends Specification {

    AsyncConsumerActionExecutor connection = Mock(AsyncConsumerActionExecutor)
    ConnectionParameters parameters = Mock(ConnectionParameters)
    EclipseModelResultSetModelBuilder modelBuilder = new EclipseModelResultSetModelBuilder(EclipseProject, connection, parameters, [] as Set<CompositeParticipant>)

    def "does not support certain API methods"(String message, Action<ModelBuilder> configurer) {
        when:
        configurer.execute(modelBuilder)

        then:
        Throwable t = thrown(UnsupportedMethodException)
        t.message == "ModelBuilder for a composite cannot $message"

        where:
        message                 | configurer
        'execute tasks'         | { it.forTasks([] as String[]) }
        'execute tasks'         | { it.forTasks([]) }
        'provide arguments'     | { it.withArguments([] as String[]) }
        'provide arguments'     | { it.withArguments([]) }
        'set standard output'   | { it.setStandardOutput(System.out) }
        'set standard error'    | { it.setStandardError(System.err) }
        'set standard input'    | { it.setStandardInput(System.in) }
        'set color output'      | { it.setColorOutput(true) }
        'set Java home'         | { it.setJavaHome(new File('.')) }
        'provide JVM arguments' | { it.setJvmArguments([] as String[]) }
        'provide JVM arguments' | { it.setJvmArguments([]) }
        'inject a classpath'    | { it.withInjectedClassPath(ClassPath.EMPTY) }
    }
}
