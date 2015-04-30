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

import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingclient.LaunchableConfig
import com.gradleware.tooling.toolingclient.ToolingClient
import org.gradle.internal.Factory
import org.gradle.tooling.BuildAction
import org.gradle.tooling.BuildController
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProgressListener
import org.gradle.tooling.events.task.TaskProgressListener
import org.gradle.tooling.events.test.TestProgressListener
import org.gradle.tooling.internal.consumer.DefaultGradleConnector
import org.gradle.tooling.model.build.BuildEnvironment
import org.junit.Rule
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

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

  def "connectionPooling"() {
    given:
    AtomicInteger connectionCreationCount = new AtomicInteger(0)
    Factory<GradleConnector> connectorFactory = Mock(Factory.class)
    2 * connectorFactory.create() >> {
      connectionCreationCount.incrementAndGet()
      def connector = GradleConnector.newConnector()
      ((DefaultGradleConnector) connector).embedded(true)
      return connector
    } // must combine mocking with stubbing

    DefaultToolingClient toolingClient = new DefaultToolingClient(connectorFactory)
    def modelRequest = toolingClient.newModelRequest(BuildEnvironment.class)

    when:
    modelRequest.projectDir(directoryProvider.testDirectory)
    modelRequest.gradleUserHomeDir(new File(System.getProperty("user.home") + File.separator + ".gradle"))
    modelRequest.gradleDistribution(GradleDistribution.fromBuild())
    modelRequest.executeAndWait()

    then:
    connectionCreationCount.get() == 1

    when:
    modelRequest.executeAndWait()

    then:
    connectionCreationCount.get() == 1

    when:
    modelRequest.gradleDistribution(GradleDistribution.forVersion("2.1"))
    modelRequest.executeAndWait()

    then:
    // new connection is created iff combination of fixed request attributes is new
    connectionCreationCount.get() == 2

    when:
    modelRequest.progressListeners(Mock(ProgressListener))
    modelRequest.executeAndWait()

    then:
    // no new connection is created if only combination of transient request attributes is new
    connectionCreationCount.get() == 2

    cleanup:
    toolingClient.stop(ToolingClient.CleanUpStrategy.GRACEFULLY)
  }

  def "progressListenersInvoked"() {
    setup:
    AtomicBoolean invoked = new AtomicBoolean(false)
    DefaultToolingClient toolingClient = new DefaultToolingClient()
    def modelRequest = toolingClient.newModelRequest(BuildEnvironment.class)
    modelRequest.projectDir(directoryProvider.testDirectory)
    modelRequest.progressListeners({ invoked.set(true) } as ProgressListener)

    when:
    modelRequest.executeAndWait()

    then:
    assert invoked.get()

    cleanup:
    toolingClient.stop(ToolingClient.CleanUpStrategy.GRACEFULLY)
  }

  def "taskProgressListenersInvoked"() {
    setup:
    createGradleProject()

    AtomicBoolean invoked = new AtomicBoolean(false)
    DefaultToolingClient toolingClient = new DefaultToolingClient()
    def launchRequest = toolingClient.newBuildLaunchRequest(LaunchableConfig.forTasks('build'))
    launchRequest.projectDir(directoryProvider.testDirectory)
    launchRequest.taskProgressListeners({ invoked.set(true) } as TaskProgressListener)

    when:
    launchRequest.executeAndWait()

    then:
    assert invoked.get()

    cleanup:
    toolingClient.stop(ToolingClient.CleanUpStrategy.GRACEFULLY)
  }

  def "testProgressListenersInvoked"() {
    setup:
    createGradleProject()

    AtomicBoolean invoked = new AtomicBoolean(false)
    DefaultToolingClient toolingClient = new DefaultToolingClient()
    def launchRequest = toolingClient.newBuildLaunchRequest(LaunchableConfig.forTasks('build'))
    launchRequest.projectDir(directoryProvider.testDirectory)
    launchRequest.testProgressListeners({ invoked.set(true) } as TestProgressListener)

    when:
    launchRequest.executeAndWait()

    then:
    assert invoked.get()

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

    cleanup:
    toolingClient.stop(ToolingClient.CleanUpStrategy.GRACEFULLY)
  }

  def createGradleProject() {
    // settings.gradle file to ensure test does not pick up Gradle version defined in the wrapper of the commons build itself
    directoryProvider.createFile('settings.gradle')
    directoryProvider.file('build.gradle') << """
            apply plugin: 'java'
            repositories { mavenCentral() }
            dependencies { testCompile 'junit:junit:4.12' }
            compileTestJava.options.fork = true
"""

    directoryProvider.createDir('src/test/java/example')
    directoryProvider.file('src/test/java/example/MyTest.java') << """
            package example;
            import org.junit.Test;
            public class MyTest {
                @org.junit.Test public void foo() {
                    org.junit.Assert.assertEquals(1, 1);
                }
            }
"""
  }

  private static final class EmptyBuildAction<String> implements BuildAction<String> {

    @Override
    String execute(BuildController controller) {
      return null
    }

  }

}
