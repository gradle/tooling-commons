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

package com.gradleware.tooling.toolingclient

import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.ToolingClientSpecification
import org.gradle.tooling.BuildAction
import org.gradle.tooling.BuildController
import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.model.gradle.BuildInvocations
import org.gradle.tooling.model.gradle.GradleBuild
import org.junit.Rule

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

class ToolingClientTest extends ToolingClientSpecification {

  @Rule
  TestDirectoryProvider directoryProvider = new TestDirectoryProvider();

  def setup() {
    directoryProvider.createFile('settings.gradle');
    directoryProvider.createFile('build.gradle') << 'task myTask {}'
  }

  def "newClientCreatesNewInstanceForEachInvocation"() {
    setup:
    def toolingClient1 = ToolingClient.newClient()
    def toolingClient2 = ToolingClient.newClient()
    assert !toolingClient1.is(toolingClient2)
  }

  def "fetchModel"() {
    setup:
    ModelRequest<GradleBuild> modelRequest = toolingClient.newModelRequest(GradleBuild.class)
    modelRequest.projectDir(directoryProvider.testDirectory)
    GradleBuild gradleBuild = modelRequest.executeAndWait()
    assert gradleBuild.rootProject.path == ':'
  }

  def "runAction"() {
    setup:
    BuildAction<Integer> action = new TaskCountBuildAction()
    BuildActionRequest<Integer> buildActionRequest = toolingClient.newBuildActionRequest(action)
    buildActionRequest.projectDir(directoryProvider.testDirectory)
    int numberOfTasks = buildActionRequest.executeAndWait()
    assert numberOfTasks == 13
  }

  def "executeBuild"() {
    setup:
    LaunchableConfig launchables = LaunchableConfig.forTasks("myTask")
    BuildLaunchRequest buildLaunchRequest = toolingClient.newBuildLaunchRequest(launchables)
    buildLaunchRequest.projectDir(directoryProvider.testDirectory)
    buildLaunchRequest.executeAndWait()
  }

  def "executeTests"() {
      setup:
      new File(directoryProvider.testDirectory, 'build.gradle') << """
         apply plugin: "java"
         repositories { jcenter() }
         dependencies { testCompile "junit:junit:4.12" }
      """
      directoryProvider.createDir('src/test/java')
      directoryProvider.createFile('src/test/java/MyTest.java') << """
         import org.junit.Test;
         import static org.junit.Assert.assertTrue;
         public class MyTest {
             public @Test void test() { assertTrue(true); }
         }
      """
      TestConfig tests = TestConfig.forJvmTestClasses("MyTest")
      TestLaunchRequest testLaunchRequest = toolingClient.newTestLaunchRequest(tests)
      testLaunchRequest.projectDir(directoryProvider.testDirectory)
      testLaunchRequest.executeAndWait()
  }

  def "fetchModelAsynchronously"() throws Exception {
    setup:
    Class<GradleBuild> modelClass = GradleBuild.class
    ModelRequest<GradleBuild> modelRequest = toolingClient.newModelRequest(modelClass);
    modelRequest.projectDir(directoryProvider.testDirectory);

    final AtomicReference<GradleBuild> modelRef = new AtomicReference<GradleBuild>();

    when:
    CountDownLatch latch = new CountDownLatch(1);
    LongRunningOperationPromise<GradleBuild> promise = modelRequest.execute();
    promise.onFailure(new Consumer<GradleConnectionException>() {

      @Override
      public void accept(GradleConnectionException input) {
        latch.countDown();
      }
    });
    promise.onComplete(new Consumer<GradleBuild>() {

      @Override
      public void accept(GradleBuild input) {
        modelRef.set(input);
        latch.countDown();
      }
    });
    latch.await();

    then:
    GradleBuild gradleBuild = modelRef.get()
    gradleBuild != null
    gradleBuild.rootProject.path == ':'
  }

  def "runActionAsynchronously"() {
    setup:
    BuildAction<Integer> action = new TaskCountBuildAction()
    BuildActionRequest<Integer> buildActionRequest = toolingClient.newBuildActionRequest(action)
    buildActionRequest.projectDir(directoryProvider.testDirectory)

    final AtomicReference<Integer> resultRef = new AtomicReference<Integer>();

    when:
    CountDownLatch latch = new CountDownLatch(1);
    LongRunningOperationPromise<Integer> promise = buildActionRequest.execute();
    promise.onFailure(new Consumer<GradleConnectionException>() {

      @Override
      public void accept(GradleConnectionException input) {
        latch.countDown();
      }
    });
    promise.onComplete(new Consumer<Integer>() {

      @Override
      public void accept(Integer input) {
        resultRef.set(input);
        latch.countDown();
      }
    });
    latch.await();


    then:
    Integer result = resultRef.get()
    result == 13
  }

  def "executeBuildAsynchronously"() {
    setup:
    LaunchableConfig launchables = LaunchableConfig.forTasks("myTask")
    BuildLaunchRequest buildLaunchRequest = toolingClient.newBuildLaunchRequest(launchables)
    buildLaunchRequest.projectDir(directoryProvider.testDirectory)

    final AtomicReference<Void> resultRef = new AtomicReference<Void>();

    when:
    CountDownLatch latch = new CountDownLatch(1);
    LongRunningOperationPromise<Void> promise = buildLaunchRequest.execute();
    promise.onFailure(new Consumer<GradleConnectionException>() {

      @Override
      public void accept(GradleConnectionException input) {
        latch.countDown();
      }
    });
    promise.onComplete(new Consumer<Void>() {

      @Override
      public void accept(Void input) {
        resultRef.set(input);
        latch.countDown();
      }
    });
    latch.await();


    then:
    Void result = resultRef.get()
    result == null
  }

  def "executeTestAsynchronously"() {
      setup:
      TestConfig tests = TestConfig.forJvmTestClasses("MyTest")
      TestLaunchRequest testLaunchRequest = toolingClient.newTestLaunchRequest(tests)
      testLaunchRequest.projectDir(directoryProvider.testDirectory)

      final AtomicReference<Void> resultRef = new AtomicReference<Void>();

      when:
      CountDownLatch latch = new CountDownLatch(1);
      LongRunningOperationPromise<Void> promise = testLaunchRequest.execute();
      promise.onFailure(new Consumer<GradleConnectionException>() {

        @Override
        public void accept(GradleConnectionException input) {
          latch.countDown();
        }
      });
      promise.onComplete(new Consumer<Void>() {

        @Override
        public void accept(Void input) {
          resultRef.set(input);
          latch.countDown();
        }
      });
      latch.await();


      then:
      Void result = resultRef.get()
      result == null
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
