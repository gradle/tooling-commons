package com.gradleware.tooling.toolingclient.internal

import com.gradleware.tooling.toolingclient.LaunchableConfig
import spock.lang.Specification

class DefaultBuildLaunchRequestTest extends Specification {

  def "deriveForLaunchables"() {
    setup:
    ExecutableToolingClient toolingClient = Mock(ExecutableToolingClient)
    DefaultBuildLaunchRequest buildLaunchRequest = new DefaultBuildLaunchRequest(toolingClient, LaunchableConfig.forTasks("alpha"))

    def newLaunchables = LaunchableConfig.forTasks("beta")
    DefaultBuildLaunchRequest copy = buildLaunchRequest.deriveForLaunchables(newLaunchables)

    assert copy.launchables == newLaunchables
    assert copy.cancellationToken.is(buildLaunchRequest.cancellationToken)
  }

}
