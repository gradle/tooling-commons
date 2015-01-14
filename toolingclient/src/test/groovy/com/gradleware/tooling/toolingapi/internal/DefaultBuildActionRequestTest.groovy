package com.gradleware.tooling.toolingapi.internal

import org.gradle.tooling.BuildAction
import spock.lang.Specification

class DefaultBuildActionRequestTest extends Specification {

  def "deriveForBuildAction"() {
    setup:
    ExecutableToolingClient toolingClient = Mock(ExecutableToolingClient)
    BuildAction<Void> buildAction = Mock(BuildAction.class)
    DefaultBuildActionRequest<Void> buildActionRequest = new DefaultBuildActionRequest<Void>(toolingClient, buildAction)

    BuildAction<String> newBuildAction = Mock(BuildAction.class)
    DefaultBuildActionRequest<String> copy = buildActionRequest.deriveForBuildAction(newBuildAction)

    assert copy.buildAction == newBuildAction
    assert copy.cancellationToken.is(buildActionRequest.cancellationToken)
  }

}
