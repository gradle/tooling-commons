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
import com.gradleware.tooling.toolingclient.ModelRequest;
import com.gradleware.tooling.toolingclient.TestConfig
import com.gradleware.tooling.toolingclient.ToolingClient
import com.gradleware.tooling.toolingclient.ToolingClient.ConnectionStrategy

import org.gradle.internal.Factory
import org.gradle.tooling.BuildAction
import org.gradle.tooling.BuildController
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProgressListener
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.events.ProgressEvent
import org.gradle.tooling.events.test.JvmTestOperationDescriptor
import org.gradle.tooling.events.test.TestProgressEvent
import org.gradle.tooling.internal.consumer.DefaultGradleConnector
import org.gradle.tooling.model.GradleProject
import org.gradle.tooling.model.build.BuildEnvironment
import org.gradle.tooling.model.gradle.BuildInvocations
import org.junit.Rule
import spock.lang.Specification

import java.util.concurrent.CountDownLatch;
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

  def "newTestLaunchRequestSetsBuildSpecificGradleDistributionByDefault"() {
    setup:
    DefaultToolingClient toolingClient = new DefaultToolingClient()
    InspectableTestLaunchRequest testLaunchRequest = (InspectableTestLaunchRequest) toolingClient.newTestLaunchRequest(TestConfig.forJvmTestClasses('Foo'))
    testLaunchRequest.getGradleDistribution() == GradleDistribution.fromBuild()

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

  def "if REUSE strategy is specified, a new connection is only opened when fixed request attributes change"() {
    given:
    Factory<GradleConnector> connectorFactory = Mock(Factory.class)
    DefaultToolingClient toolingClient = new DefaultToolingClient(connectorFactory, ConnectionStrategy.REUSE)
    def modelRequest = toolingClient.newModelRequest(BuildEnvironment.class)
    modelRequest.projectDir(directoryProvider.testDirectory)
    modelRequest.gradleDistribution(GradleDistribution.fromBuild())

    when:
    modelRequest.executeAndWait()

    then:
    1 * connectorFactory.create() >> Stub(GradleConnector)

    when:
    modelRequest.executeAndWait()

    then:
    0 * connectorFactory.create()

    when:
    modelRequest.gradleDistribution(GradleDistribution.forVersion("2.1"))
    modelRequest.executeAndWait()

    then:
    1 * connectorFactory.create() >> Stub(GradleConnector)

    when:
    modelRequest.progressListeners(Stub(ProgressListener))
    modelRequest.executeAndWait()

    then:
    0 * connectorFactory.create()

    cleanup:
    toolingClient.stop(ToolingClient.CleanUpStrategy.GRACEFULLY)
  }

  def "if PER_REQUEST strategy is specified, a new connection is opened for every request"() {
    given:
    Factory<GradleConnector> connectorFactory = Mock(Factory.class)
    DefaultToolingClient toolingClient = new DefaultToolingClient(connectorFactory, ConnectionStrategy.PER_REQUEST)
    def modelRequest = toolingClient.newModelRequest(BuildEnvironment.class)
    modelRequest.projectDir(directoryProvider.testDirectory)
    modelRequest.gradleDistribution(GradleDistribution.fromBuild())

    when:
    modelRequest.executeAndWait()
    modelRequest.executeAndWait()

    then:
    2 * connectorFactory.create() >> Stub(GradleConnector) {
      connect() >> {
        Mock(ProjectConnection) {
          model(_) >> Stub(ModelBuilder)
          1 * close()
        }
      }
    }

    cleanup:
    toolingClient.stop(ToolingClient.CleanUpStrategy.GRACEFULLY)
  }

  def "Multiple requests can run in parallel without closing each other."() {
      given:
      Factory<GradleConnector> connectorFactory = { GradleConnector.newConnector() }
      DefaultToolingClient toolingClient = new DefaultToolingClient(connectorFactory, ConnectionStrategy.PER_REQUEST)
      ModelRequest<BuildEnvironment> modelRequest = toolingClient.newModelRequest(BuildEnvironment.class)
      modelRequest.projectDir(directoryProvider.testDirectory)
      modelRequest.gradleDistribution(GradleDistribution.fromBuild())
      def latch = new CountDownLatch(5)
      def successCount = new AtomicInteger();

      when:
      5.times {
          new Thread({
              try {
                  modelRequest.executeAndWait()
                  successCount.incrementAndGet()
              } finally {
                latch.countDown()
              }
          }).start()
      }
      latch.await()

      then:
      successCount.get() == 5

      cleanup:
      toolingClient.stop(ToolingClient.CleanUpStrategy.GRACEFULLY)
    }

  def "progressListenersInvokedForModelRequest"() {
    setup:
    // settings.gradle file to ensure test does not pick up Gradle version defined in the wrapper of the commons build itself
    directoryProvider.createFile('settings.gradle')

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

  def "progressListenersInvokedForBuildActionRequest"() {
    setup:
    // settings.gradle file to ensure test does not pick up Gradle version defined in the wrapper of the commons build itself
    directoryProvider.createFile('settings.gradle')

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

  def "progressListenersInvokedForBuildLaunchRequest"() {
    setup:
    // settings.gradle file to ensure test does not pick up Gradle version defined in the wrapper of the commons build itself
    directoryProvider.createFile('settings.gradle')

    createGradleProject()

    AtomicBoolean progressListenerInvoked = new AtomicBoolean(false)
    AtomicBoolean typedProgressListenerInvoked = new AtomicBoolean(false)

    DefaultToolingClient toolingClient = new DefaultToolingClient()
    def buildLaunchRequest = toolingClient.newBuildLaunchRequest(LaunchableConfig.forTasks('build'))
    buildLaunchRequest.projectDir(directoryProvider.testDirectory)
    buildLaunchRequest.progressListeners({ progressListenerInvoked.set(true) } as ProgressListener)
    buildLaunchRequest.typedProgressListeners({ typedProgressListenerInvoked.set(true) } as org.gradle.tooling.events.ProgressListener)

    when:
    buildLaunchRequest.executeAndWait()

    then:
    assert progressListenerInvoked.get()
    assert typedProgressListenerInvoked.get()

    cleanup:
    toolingClient.stop(ToolingClient.CleanUpStrategy.GRACEFULLY)
  }

  def "progressListenersInvokedForTestLaunchRequest"() {
    setup:
    // settings.gradle file to ensure test does not pick up Gradle version defined in the wrapper of the commons build itself
    directoryProvider.createFile('settings.gradle')

    createGradleProject()

    AtomicBoolean progressListenerInvoked = new AtomicBoolean(false)
    AtomicBoolean typedProgressListenerInvoked = new AtomicBoolean(false)

    DefaultToolingClient toolingClient = new DefaultToolingClient()
    def testLaunchRequest = toolingClient.newTestLaunchRequest(TestConfig.forJvmTestClasses('example.MyTest1'))
    testLaunchRequest.projectDir(directoryProvider.testDirectory)
    testLaunchRequest.progressListeners({ progressListenerInvoked.set(true) } as ProgressListener)
    testLaunchRequest.typedProgressListeners({ typedProgressListenerInvoked.set(true) } as org.gradle.tooling.events.ProgressListener)

    when:
    testLaunchRequest.executeAndWait()

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

  def "running tasks with task-specific arguments"() {
    setup:
    createGradleProjectWithFailingTest()

    List<TestProgressEvent> result = new ArrayList<TestProgressEvent>()

    DefaultToolingClient toolingClient = new DefaultToolingClient()
    def launchRequest = toolingClient.newBuildLaunchRequest(LaunchableConfig.forTasks())
    launchRequest.projectDir(directoryProvider.testDirectory)
    launchRequest.arguments('test', '--tests', 'example.MyTest1')
    launchRequest.typedProgressListeners(new org.gradle.tooling.events.ProgressListener() {

      @Override
      void statusChanged(ProgressEvent event) {
        if (event instanceof TestProgressEvent) {
          result << (event as TestProgressEvent)
        }
      }
    })

    when:
    launchRequest.executeAndWait()

    then:
    noExceptionThrown()

    result.find { ((JvmTestOperationDescriptor) it.descriptor).className == 'example.MyTest1' } != null
    result.find { ((JvmTestOperationDescriptor) it.descriptor).className == 'example.MyTest2' } == null

    result.find { ((JvmTestOperationDescriptor) it.descriptor).displayName == 'Test foo(example.MyTest1)' } != null
    result.find { ((JvmTestOperationDescriptor) it.descriptor).displayName == 'Test bar(example.MyTest2)' } == null
  }

  def createGradleProject() {
    directoryProvider.file('build.gradle') << """
            apply plugin: 'java'
            repositories { mavenCentral() }
            dependencies { testCompile 'junit:junit:4.12' }
            compileTestJava.options.fork = true
"""

    directoryProvider.createDir('src/test/java/example')
    directoryProvider.file('src/test/java/example/MyTest1.java') << """
            package example;
            import org.junit.Test;
            public class MyTest1 {
                @org.junit.Test public void foo() {
                    org.junit.Assert.assertEquals(1, 1);
                }
            }
"""
  }

  def createGradleProjectWithFailingTest() {
    createGradleProject()

    directoryProvider.file('src/test/java/example/MyTest2.java') << """
            package example;
            import org.junit.Test;
            public class MyTest2 {
                @org.junit.Test public void bar() {
                    throw new RuntimeException();
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
