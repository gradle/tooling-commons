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
import org.gradle.tooling.internal.consumer.DefaultGradleConnector
import org.gradle.tooling.model.GradleProject
import org.gradle.tooling.model.build.BuildEnvironment
import org.gradle.tooling.model.gradle.BuildInvocations
import org.junit.Rule
import spock.lang.Ignore
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
    InspectableBuildActionRequest<String> buildActionRequest = (InspectableBuildActionRequest) toolingClient.newBuildActionRequest(new TaskCountBuildAction())
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

  def "progressListenersInvokedForModelRequest"() {
    setup:
    AtomicBoolean progressListenerInvoked = new AtomicBoolean(false)
    AtomicBoolean typedProgressListenerInvoked = new AtomicBoolean(false)

    DefaultToolingClient toolingClient = new DefaultToolingClient()
    def modelRequest = toolingClient.newModelRequest(GradleProject.class)
    modelRequest.projectDir(directoryProvider.testDirectory)
    modelRequest.progressListeners({ progressListenerInvoked.set(true) } as ProgressListener)
    modelRequest.typedProgressListeners({ typedProgressListenerInvoked.set(true) } as org.gradle.tooling.events.ProgressListener)

    when:
    modelRequest.executeAndWait()

    then:
    assert progressListenerInvoked.get()
    assert typedProgressListenerInvoked.get()

    cleanup:
    toolingClient.stop(ToolingClient.CleanUpStrategy.GRACEFULLY)
  }

  def "progressListenersInvokedForBuildLaunchRequest"() {
    setup:
    createGradleProject()

    AtomicBoolean progressListenerInvoked = new AtomicBoolean(false)
    AtomicBoolean typedProgressListenerInvoked = new AtomicBoolean(false)

    DefaultToolingClient toolingClient = new DefaultToolingClient()
    def launchRequest = toolingClient.newBuildLaunchRequest(LaunchableConfig.forTasks('build'))
    launchRequest.projectDir(directoryProvider.testDirectory)
    launchRequest.progressListeners({ progressListenerInvoked.set(true) } as ProgressListener)
    launchRequest.typedProgressListeners({ typedProgressListenerInvoked.set(true) } as org.gradle.tooling.events.ProgressListener)

    when:
    launchRequest.executeAndWait()

    then:
    assert progressListenerInvoked.get()
    assert typedProgressListenerInvoked.get()

    cleanup:
    toolingClient.stop(ToolingClient.CleanUpStrategy.GRACEFULLY)
  }

  def "progressListenersInvokedForBuildActionRequest"() {
    setup:
    AtomicBoolean progressListenerInvoked = new AtomicBoolean(false)
    AtomicBoolean typedProgressListenerInvoked = new AtomicBoolean(false)

    DefaultToolingClient toolingClient = new DefaultToolingClient()
    def modelRequest = toolingClient.newBuildActionRequest(new TaskCountBuildAction())
    modelRequest.projectDir(directoryProvider.testDirectory)
    modelRequest.progressListeners({ progressListenerInvoked.set(true) } as ProgressListener)
    modelRequest.typedProgressListeners({ typedProgressListenerInvoked.set(true) } as org.gradle.tooling.events.ProgressListener)

    when:
    modelRequest.executeAndWait()

    then:
    assert progressListenerInvoked.get()
    assert typedProgressListenerInvoked.get()

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

  private static final class TaskCountBuildAction implements BuildAction<Integer> {

    @Override
    Integer execute(BuildController controller) {
      def rootProject = controller.getBuildModel().getRootProject()
      BuildInvocations buildInvocations = controller.findModel(rootProject, BuildInvocations.class)
      return buildInvocations.getTasks().size()
    }

  }

}
