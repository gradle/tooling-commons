/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
