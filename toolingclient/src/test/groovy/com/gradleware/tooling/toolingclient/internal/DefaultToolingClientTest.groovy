package com.gradleware.tooling.toolingclient.internal

import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingclient.LaunchableConfig
import com.gradleware.tooling.toolingclient.ToolingClient
import org.gradle.internal.Factory
import org.gradle.tooling.BuildAction
import org.gradle.tooling.BuildController
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.internal.consumer.DefaultGradleConnector
import org.gradle.tooling.model.build.BuildEnvironment
import org.junit.Rule
import spock.lang.Specification

@SuppressWarnings("GroovyAssignabilityCheck")
class DefaultToolingClientTest extends Specification {

  @Rule
  TestDirectoryProvider directoryProvider = new TestDirectoryProvider();

  def "newModelRequestSetsBuildSpecificGradleDistributionByDefault"() {
    setup:
    DefaultToolingClient toolingClient = new DefaultToolingClient()
    InspectableModelRequest<String> modelRequest = (InspectableModelRequest) toolingClient.newModelRequest(String.class)
    modelRequest.getGradleDistribution() == GradleDistribution.fromBuild()

    cleanup:
    toolingClient.stop(ToolingClient.CleanUpStrategy.GRACEFULLY)
  }

  def "newBuildActionRequestSetsBuildSpecificGradleDistributionByDefault"() {
    setup:
    DefaultToolingClient toolingClient = new DefaultToolingClient()
    InspectableBuildActionRequest<String> buildActionRequest = (InspectableBuildActionRequest) toolingClient.newBuildActionRequest(new EmptyBuildAction())
    buildActionRequest.getGradleDistribution() == GradleDistribution.fromBuild()

    cleanup:
    toolingClient.stop(ToolingClient.CleanUpStrategy.GRACEFULLY)
  }

  def "newBuildLaunchRequestSetsBuildSpecificGradleDistributionByDefault"() {
    setup:
    DefaultToolingClient toolingClient = new DefaultToolingClient()
    InspectableBuildLaunchRequest buildLaunchRequest = (InspectableBuildLaunchRequest) toolingClient.newBuildLaunchRequest(LaunchableConfig.forTasks())
    buildLaunchRequest.getGradleDistribution() == GradleDistribution.fromBuild()

    cleanup:
    toolingClient.stop(ToolingClient.CleanUpStrategy.GRACEFULLY)
  }

  def "customGradleConnectorFactory"() {
    given:
    Factory<GradleConnector> connectorFactory = Mock(Factory.class)
    1 * connectorFactory.create() >> {
      def connector = GradleConnector.newConnector()
      ((DefaultGradleConnector) connector).embedded(true)
      return connector
    } // must combine mocking with stubbing

    DefaultToolingClient toolingClient = new DefaultToolingClient(connectorFactory)
    def modelRequest = toolingClient.newModelRequest(BuildEnvironment.class)
    modelRequest.projectDir(directoryProvider.testDirectory)

    when:
    def model = modelRequest.executeAndWait()

    then:
    model != null

    cleanup:
    toolingClient.stop(ToolingClient.CleanUpStrategy.GRACEFULLY)
  }

  def "stop - forceful stop strategy not implemented yet"() {
    given:
    DefaultToolingClient toolingClient = new DefaultToolingClient()

    when:
    toolingClient.stop(ToolingClient.CleanUpStrategy.FORCEFULLY)

    then:
    thrown(UnsupportedOperationException)
  }

  private static final class EmptyBuildAction<String> implements BuildAction<String> {

    @Override
    String execute(BuildController controller) {
      return null
    }

  }

}
