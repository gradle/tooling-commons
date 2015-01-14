package com.gradleware.tooling.toolingclient.internal

import org.gradle.tooling.model.gradle.BuildInvocations
import org.gradle.tooling.model.gradle.ProjectPublications
import spock.lang.Specification

@SuppressWarnings("GroovyAssignabilityCheck")
class DefaultModelRequestTest extends Specification {

  def "handleNullPropertiesArguments"() {
    setup:
    ExecutableToolingClient toolingClient = Mock(ExecutableToolingClient)
    DefaultModelRequest modelRequest = new DefaultModelRequest<Void>(toolingClient, Void.class)

    def jvmArguments = modelRequest.getJvmArguments()
    assert jvmArguments.length == 0

    def arguments = modelRequest.getArguments()
    assert arguments.length == 0

    def tasks = modelRequest.getTasks()
    assert tasks.length == 0

    def progressListeners = modelRequest.getProgressListeners()
    assert progressListeners.length == 0
  }

  def "deriveForModel"() {
    setup:
    ExecutableToolingClient toolingClient = Mock(ExecutableToolingClient)
    DefaultModelRequest<Void> modelRequest = new DefaultModelRequest<Void>(toolingClient, Void.class)

    def newModelType = String.class
    DefaultModelRequest<String> copy = modelRequest.deriveForModel(newModelType)

    assert copy.modelType == newModelType
    assert copy.tasks == modelRequest.tasks
    assert copy.cancellationToken.is(modelRequest.cancellationToken)
  }

  def "failValidationForModelsThatAreNotBuildScoped"() {
    setup:
    ExecutableToolingClient toolingClient = Mock(ExecutableToolingClient)

    when:
    new DefaultModelRequest<BuildInvocations>(toolingClient, BuildInvocations.class)

    then:
    thrown(IllegalArgumentException)

    when:
    new DefaultModelRequest<BuildInvocations>(toolingClient, ProjectPublications.class)

    then:
    thrown(IllegalArgumentException)
  }

  def "executeInvokesToolingClient"() {
    setup:
    ExecutableToolingClient toolingClient = Mock(ExecutableToolingClient)
    DefaultModelRequest modelRequest = new DefaultModelRequest<Void>(toolingClient, Void.class)

    when:
    modelRequest.execute()

    then:
    1 * toolingClient.execute(modelRequest)

    when:
    modelRequest.executeAndWait()

    then:
    1 * toolingClient.executeAndWait(modelRequest)
  }

}
