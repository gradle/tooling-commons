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

package com.gradleware.tooling.toolingmodel.repository.internal

import com.google.common.collect.ImmutableList
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.junit.TestFile
import com.gradleware.tooling.spock.ToolingModelToolingClientSpecification
import com.gradleware.tooling.spock.VerboseUnroll
import com.gradleware.tooling.testing.GradleVersionExtractor
import com.gradleware.tooling.testing.GradleVersionParameterization
import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment
import com.gradleware.tooling.toolingmodel.OmniBuildInvocationsContainer
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniGradleBuild
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure
import com.gradleware.tooling.toolingmodel.OmniGradleProject
import com.gradleware.tooling.toolingmodel.Path
import com.gradleware.tooling.toolingmodel.repository.BuildEnvironmentUpdateEvent
import com.gradleware.tooling.toolingmodel.repository.BuildInvocationsUpdateEvent
import com.gradleware.tooling.toolingmodel.repository.EclipseGradleBuildUpdateEvent
import com.gradleware.tooling.toolingmodel.repository.EclipseWorkspaceUpdateEvent;
import com.gradleware.tooling.toolingmodel.repository.Environment
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes
import com.gradleware.tooling.toolingmodel.repository.GradleBuildStructureUpdateEvent
import com.gradleware.tooling.toolingmodel.repository.GradleBuildUpdateEvent
import com.gradleware.tooling.toolingmodel.repository.ModelRepositoryProvider;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes
import com.gradleware.tooling.toolingmodel.repository.internal.DefaultModelRepositoryTest.RepositoryType;

import org.gradle.api.specs.Spec
import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProgressListener
import org.gradle.util.GradleVersion
import org.junit.Rule

import java.util.concurrent.atomic.AtomicReference

@VerboseUnroll(formatter = GradleDistributionFormatter.class)
class DefaultModelRepositoryTest extends ToolingModelToolingClientSpecification {

  @Rule
  TestDirectoryProvider directoryProvider = new TestDirectoryProvider("single-project");

  @Rule
  TestDirectoryProvider directoryProviderMultiProjectBuild = new TestDirectoryProvider("multi-project");

  @Rule
  TestDirectoryProvider directoryProviderErroneousBuildStructure = new TestDirectoryProvider("erroneous-build-structure");

  @Rule
  TestDirectoryProvider directoryProviderErroneousBuildFile = new TestDirectoryProvider("erroneous-build-file");

  def setup() {
    // prepare a Gradle build that has a root project and two child projects, and one gradle child project
    directoryProvider.createFile('settings.gradle') << '''
       rootProject.name = 'my root project'
       include 'sub1'
       include 'sub2'
       include 'sub2:subSub1'
    '''
    directoryProvider.createFile('build.gradle') << '''
       description = 'a sample root project'
       task myTask {}
    '''

    directoryProvider.createDir('sub1')
    directoryProvider.createFile('sub1', 'build.gradle') << '''
       description = 'sub project 1'
       task myFirstTaskOfSub1 {
         description = '1st task of sub1'
         group = 'build'
       }
       task mySecondTaskOfSub1 {
         description = '2nd task of sub1'
       }
    '''

    directoryProvider.createDir('sub2')
    directoryProvider.createFile('sub2', 'build.gradle') << '''
       description = 'sub project 2'
       task myFirstTaskOfSub2 {
         description = '1st task of sub2'
       }
       task mySecondTaskOfSub2 {
         description = '2nd task of sub2'
       }
       task myTask {
         description = 'another task of sub2'
         group = 'build'
       }
    '''

    directoryProvider.createDir('sub2', 'subSub1')
    directoryProvider.createFile('sub2', 'subSub1', 'build.gradle') << '''
       description = 'subSub project 1 of sub project 2'
       task myFirstTaskOfSub2subSub1{
         description = '1st task of sub2:subSub1'
       }
       task mySecondTaskOfSub2subSub1{
         description = '2nd task of sub2:subSub1'
       }
       task myTask {}
    '''

    // prepare a Gradle build with a multi-project structure
    directoryProviderMultiProjectBuild.createFile('settings.gradle') << '''
      rootProject.name = 'root project of multi-project build'
      include 'api'
      include 'impl'
    '''

    directoryProviderMultiProjectBuild.createDir('api')
    directoryProviderMultiProjectBuild.createFile('api', 'build.gradle') << '''
        apply plugin: 'java'
        repositories {
            mavenCentral()
            dependencies {
                compile 'com.google.guava:guava:18.0'
            }
        }
    '''

    directoryProviderMultiProjectBuild.createDir('impl')
    directoryProviderMultiProjectBuild.createFile('impl', 'build.gradle') << '''
        apply plugin: 'java'
        dependencies {
            compile project(':api')
        }
    '''

    // prepare a Gradle build that has an erroneous structure
    directoryProviderErroneousBuildStructure.createFile('settings.gradle') << 'include foo'

    // prepare a Gradle build that has a valid structure but an erroneous build script
    directoryProviderErroneousBuildFile.createFile('settings.gradle')
    directoryProviderErroneousBuildFile.createFile('build.gradle') << 'task myTask {'
  }

  def "fetchBuildEnvironment - send event after cache update"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultSimpleModelRepository(fixedRequestAttributes, toolingClient, new EventBus(), environment)

    AtomicReference<BuildEnvironmentUpdateEvent> publishedEvent = new AtomicReference<>();
    AtomicReference<OmniBuildEnvironment> modelInRepository = new AtomicReference<>();
    repository.register(new Object() {

      @SuppressWarnings("GroovyUnusedDeclaration")
      @Subscribe
      public void listen(BuildEnvironmentUpdateEvent event) {
        publishedEvent.set(event)
        modelInRepository.set(repository.fetchBuildEnvironment(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY))
      }
    })

    when:
    OmniBuildEnvironment buildEnvironment = repository.fetchBuildEnvironment(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    buildEnvironment != null
    buildEnvironment.gradle != null
    if (higherOrEqual('2.4', distribution)) {
        assert buildEnvironment.gradle.gradleUserHome.get() == new File(System.getProperty('user.home'), '.gradle')
    } else {
        assert !buildEnvironment.gradle.gradleUserHome.isPresent()
    }
    buildEnvironment.gradle.gradleVersion == extractVersion(distribution)
    buildEnvironment.java != null
    buildEnvironment.java.javaHome != null
    buildEnvironment.java.jvmArguments.size() > 0

    def event = publishedEvent.get()
    event != null
    event.buildEnvironment == buildEnvironment

    def model = modelInRepository.get()
    model == buildEnvironment

    where:
    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  def "fetchGradleBuildStructure - send event after cache update"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultSimpleModelRepository(fixedRequestAttributes, toolingClient, new EventBus(), environment)

    AtomicReference<GradleBuildStructureUpdateEvent> publishedEvent = new AtomicReference<>();
    AtomicReference<OmniGradleBuildStructure> modelInRepository = new AtomicReference<>();
    repository.register(new Object() {

      @SuppressWarnings("GroovyUnusedDeclaration")
      @Subscribe
      public void listen(GradleBuildStructureUpdateEvent event) {
        publishedEvent.set(event)
        modelInRepository.set(repository.fetchGradleBuildStructure(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY))
      }
    })

    when:
    OmniGradleBuildStructure gradleBuildStructure = repository.fetchGradleBuildStructure(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    gradleBuildStructure != null
    gradleBuildStructure.rootProject != null
    gradleBuildStructure.rootProject.name == 'my root project'
    gradleBuildStructure.rootProject.path == Path.from(':')
    if (higherOrEqual('1.8', distribution)) {
      assert gradleBuildStructure.rootProject.projectDirectory.get().absolutePath == directoryProvider.testDirectory.absolutePath
    } else {
      assert !gradleBuildStructure.rootProject.projectDirectory.isPresent()
    }
    gradleBuildStructure.rootProject.root == gradleBuildStructure.rootProject
    gradleBuildStructure.rootProject.parent == null
    gradleBuildStructure.rootProject.children.size() == 2
    gradleBuildStructure.rootProject.children*.name == ['sub1', 'sub2']
    gradleBuildStructure.rootProject.children*.path.path == [':sub1', ':sub2']
    gradleBuildStructure.rootProject.children*.projectDirectory.collect {
      it.present ? it.get().absolutePath : null
    } == (higherOrEqual('1.8', distribution) ? ['sub1', 'sub2'].collect { new File(directoryProvider.testDirectory, it).absolutePath } : [null, null])
    gradleBuildStructure.rootProject.children*.root == [gradleBuildStructure.rootProject, gradleBuildStructure.rootProject]
    gradleBuildStructure.rootProject.children*.parent == [gradleBuildStructure.rootProject, gradleBuildStructure.rootProject]
    gradleBuildStructure.rootProject.all.size() == 4
    gradleBuildStructure.rootProject.all*.name == ['my root project', 'sub1', 'sub2', 'subSub1']

    def event = publishedEvent.get()
    event != null
    event.gradleBuildStructure == gradleBuildStructure

    def model = modelInRepository.get()
    model == gradleBuildStructure

    where:
    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  def "fetchGradleBuildStructure - when exception is thrown"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildStructure.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultSimpleModelRepository(fixedRequestAttributes, toolingClient, new EventBus(), environment)

    AtomicReference<GradleBuildStructureUpdateEvent> publishedEvent = new AtomicReference<>();
    repository.register(new Object() {

      @SuppressWarnings("GroovyUnusedDeclaration")
      @Subscribe
      public void listen(GradleBuildStructureUpdateEvent event) {
        publishedEvent.set(event)
      }
    })

    when:
    repository.fetchGradleBuildStructure(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    thrown(GradleConnectionException)

    publishedEvent.get() == null

    where:
    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  def "fetchGradleBuild - send event after cache update"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultSimpleModelRepository(fixedRequestAttributes, toolingClient, new EventBus(), environment)

    AtomicReference<GradleBuildUpdateEvent> publishedEvent = new AtomicReference<>();
    AtomicReference<OmniGradleBuild> modelInRepository = new AtomicReference<>();
    repository.register(new Object() {

      @SuppressWarnings("GroovyUnusedDeclaration")
      @Subscribe
      public void listen(GradleBuildUpdateEvent event) {
        publishedEvent.set(event)
        modelInRepository.set(repository.fetchGradleBuild(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY))
      }
    })

    when:
    OmniGradleBuild gradleBuild = repository.fetchGradleBuild(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    gradleBuild != null
    gradleBuild.rootProject != null
    gradleBuild.rootProject.name == 'my root project'
    gradleBuild.rootProject.description == 'a sample root project'
    gradleBuild.rootProject.path == Path.from(':')
    if (higherOrEqual('2.4', distribution)) {
      assert gradleBuild.rootProject.projectDirectory.get().absolutePath == directoryProvider.testDirectory.absolutePath
    } else {
      assert !gradleBuild.rootProject.projectDirectory.isPresent()
    }
    if (higherOrEqual('2.0', distribution)) {
      assert gradleBuild.rootProject.buildDirectory.get().absolutePath == directoryProvider.file('build').absolutePath
    } else {
      assert !gradleBuild.rootProject.buildDirectory.isPresent()
    }
    if (higherOrEqual('1.8', distribution)) {
      assert gradleBuild.rootProject.buildScript.get().sourceFile.absolutePath == directoryProvider.file('build.gradle').absolutePath
    } else {
      assert !gradleBuild.rootProject.buildScript.isPresent()
    }
    gradleBuild.rootProject.projectTasks.findAll { !ImplicitTasks.ALL.contains(it.name) }.size() == 1
    gradleBuild.rootProject.root == gradleBuild.rootProject
    gradleBuild.rootProject.parent == null
    gradleBuild.rootProject.children.size() == 2
    gradleBuild.rootProject.children*.name == ['sub1', 'sub2']
    gradleBuild.rootProject.children*.description == ['sub project 1', 'sub project 2']
    gradleBuild.rootProject.children*.path.path == [':sub1', ':sub2']
    gradleBuild.rootProject.children*.root == [gradleBuild.rootProject, gradleBuild.rootProject]
    gradleBuild.rootProject.children*.parent == [gradleBuild.rootProject, gradleBuild.rootProject]
    gradleBuild.rootProject.all.size() == 4
    gradleBuild.rootProject.all*.name == ['my root project', 'sub1', 'sub2', 'subSub1']

    def projectSub1 = gradleBuild.rootProject.tryFind({ OmniGradleProject input ->
      input.path.path == ':sub1'
    } as Spec).get()
    projectSub1.projectTasks.findAll { !ImplicitTasks.ALL.contains(it.name) }.size() == 2

    def myFirstTaskOfSub1 = projectSub1.projectTasks.findAll { !ImplicitTasks.ALL.contains(it.name) }[0]
    myFirstTaskOfSub1.name == 'myFirstTaskOfSub1'
    myFirstTaskOfSub1.description == '1st task of sub1'
    myFirstTaskOfSub1.path.path == ':sub1:myFirstTaskOfSub1'
    myFirstTaskOfSub1.isPublic()
    if (higherOrEqual('2.5', distribution)) {
      assert myFirstTaskOfSub1.group.get() == 'build'
    } else {
      assert !myFirstTaskOfSub1.group.present
    }

    def mySecondTaskOfSub1 = projectSub1.projectTasks.findAll { !ImplicitTasks.ALL.contains(it.name) }[1]
    mySecondTaskOfSub1.name == 'mySecondTaskOfSub1'
    mySecondTaskOfSub1.description == '2nd task of sub1'
    mySecondTaskOfSub1.path.path == ':sub1:mySecondTaskOfSub1'
    mySecondTaskOfSub1.isPublic() == !higherOrEqual('2.3', distribution) // all versions < 2.3 are corrected to or default to 'true'
    if (higherOrEqual('2.5', distribution)) {
      assert mySecondTaskOfSub1.group.get() == null
    } else {
      assert !mySecondTaskOfSub1.group.present
    }

    def projectSub2 = gradleBuild.rootProject.tryFind({ OmniGradleProject input ->
      input.path.path == ':sub2'
    } as Spec).get()
    projectSub2.taskSelectors.findAll { !ImplicitTasks.ALL.contains(it.name) }.size() == 5

    def myTaskSelector = projectSub2.taskSelectors.find { it.name == 'myTask' }
    myTaskSelector.name == 'myTask'
    myTaskSelector.description == 'another task of sub2'
    myTaskSelector.projectPath.path == ':sub2'
    myTaskSelector.isPublic()
    myTaskSelector.selectedTaskPaths*.path as List == [':sub2:myTask', ':sub2:subSub1:myTask']

    def event = publishedEvent.get()
    event != null
    event.gradleBuild == gradleBuild

    def model = modelInRepository.get()
    model == gradleBuild

    where:
    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  def "fetchGradleBuild - when exception is thrown"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildFile.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultSimpleModelRepository(fixedRequestAttributes, toolingClient, new EventBus(), environment)

    AtomicReference<GradleBuildUpdateEvent> publishedEvent = new AtomicReference<>();
    repository.register(new Object() {

      @SuppressWarnings("GroovyUnusedDeclaration")
      @Subscribe
      public void listen(GradleBuildUpdateEvent event) {
        publishedEvent.set(event)
      }
    })

    when:
    repository.fetchGradleBuild(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    thrown(GradleConnectionException)

    publishedEvent.get() == null

    where:
    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  def "fetchEclipseGradleBuild - projects have correct structure and tasks" (GradleDistribution distribution, Environment environment, RepositoryType repositoryType) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultSimpleModelRepository(fixedRequestAttributes, toolingClient, new EventBus(), environment)

    when:
    OmniEclipseProject rootProject = fetchRootEclipseProject(fixedRequestAttributes, transientRequestAttributes, environment, repositoryType)

    then:
    rootProject != null
    rootProject.name == 'my root project'
    rootProject.description == 'a sample root project'
    rootProject.path == Path.from(':')
    rootProject.projectDirectory.absolutePath == directoryProvider.testDirectory.absolutePath
    rootProject.parent == null
    rootProject.children.size() == 2
    rootProject.children*.name == ['sub1', 'sub2']
    rootProject.children*.description == ['sub project 1', 'sub project 2']
    rootProject.children*.path.path == [':sub1', ':sub2']
    rootProject.children*.parent == [rootProject, rootProject]
    rootProject.all.size() == 4
    rootProject.all*.name == ['my root project', 'sub1', 'sub2', 'subSub1']

    def projectSub1 = rootProject.gradleProject.tryFind({ OmniGradleProject input ->
      return input.path.path == ':sub1'
    } as Spec).get()
    projectSub1.projectTasks.findAll { !ImplicitTasks.ALL.contains(it.name) }.size() == 2

    def myFirstTaskOfSub1 = projectSub1.projectTasks.findAll { !ImplicitTasks.ALL.contains(it.name) }[0]
    myFirstTaskOfSub1.name == 'myFirstTaskOfSub1'
    myFirstTaskOfSub1.description == '1st task of sub1'
    myFirstTaskOfSub1.path.path == ':sub1:myFirstTaskOfSub1'
    myFirstTaskOfSub1.isPublic()
    if (higherOrEqual('2.5', distribution)) {
      assert myFirstTaskOfSub1.group.get() == 'build'
    } else {
      assert !myFirstTaskOfSub1.group.present
    }

    def mySecondTaskOfSub1 = projectSub1.projectTasks.findAll { !ImplicitTasks.ALL.contains(it.name) }[1]
    mySecondTaskOfSub1.name == 'mySecondTaskOfSub1'
    mySecondTaskOfSub1.description == '2nd task of sub1'
    mySecondTaskOfSub1.path.path == ':sub1:mySecondTaskOfSub1'
    mySecondTaskOfSub1.isPublic() == !higherOrEqual('2.3', distribution) // all versions < 2.3 are corrected to or default to 'true'
    if (higherOrEqual('2.5', distribution)) {
      assert mySecondTaskOfSub1.group.get() == null
    } else {
      assert !mySecondTaskOfSub1.group.present
    }

    def projectSub2 = rootProject.gradleProject.tryFind({ OmniGradleProject input ->
      return input.path.path == ':sub2'
    } as Spec).get()
    projectSub2.taskSelectors.findAll { !ImplicitTasks.ALL.contains(it.name) }.size() == 5

    def myTaskSelector = projectSub2.taskSelectors.find { it.name == 'myTask' }
    myTaskSelector.name == 'myTask'
    myTaskSelector.description == 'another task of sub2'
    myTaskSelector.projectPath.path == ':sub2'
    myTaskSelector.isPublic()
    myTaskSelector.selectedTaskPaths*.path as List == [':sub2:myTask', ':sub2:subSub1:myTask']

    where:
    [distribution, environment, repositoryType] << fetchFromBothRepositoriesInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  def "fetchEclipseGradleBuild - send event after cache update"(GradleDistribution distribution, Environment environment) {
      given:
      def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
      def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
      def repository = new DefaultSimpleModelRepository(fixedRequestAttributes, toolingClient, new EventBus(), environment)

      AtomicReference<EclipseGradleBuildUpdateEvent> publishedEvent = new AtomicReference<>();
      AtomicReference<OmniEclipseGradleBuild> modelInRepository = new AtomicReference<>();
      repository.register(new Object() {

        @SuppressWarnings("GroovyUnusedDeclaration")
        @Subscribe
        public void listen(EclipseGradleBuildUpdateEvent event) {
          publishedEvent.set(event)
          modelInRepository.set(repository.fetchEclipseGradleBuild(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY))
        }
      })

      when:
      OmniEclipseGradleBuild eclipseGradleBuild = repository.fetchEclipseGradleBuild(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

      then:
      def model = modelInRepository.get()
      model == eclipseGradleBuild

      where:
      [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.0")
    }

  @SuppressWarnings("GroovyTrivialConditional")
  def "fetchEclipseGradleBuild - sources and project/external dependencies "(GradleDistribution distribution, Environment environment, RepositoryType repositoryType) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderMultiProjectBuild.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())

    def apiProjectDir = new TestFile(this.directoryProviderMultiProjectBuild.testDirectory, 'api')
    apiProjectDir.create {
      src {
        main {
          java {}
          resources {}
        }
        test {
          java {}
        }
      }
    }

    when:
    def rootProject = fetchRootEclipseProject(fixedRequestAttributes, transientRequestAttributes, environment, repositoryType)

    then:
    rootProject != null
    rootProject.name == 'root project of multi-project build'
    rootProject.sourceDirectories == []
    rootProject.projectDependencies == []
    rootProject.externalDependencies == []

    // verify source directories
    def apiSourceDirectories = rootProject.tryFind({ it.name == 'api' } as Spec).get().sourceDirectories
    apiSourceDirectories.size() == 3
    apiSourceDirectories[0].path == 'src/main/java'
    apiSourceDirectories[0].directory == apiProjectDir.file('src/main/java')
    apiSourceDirectories[1].path == 'src/main/resources'
    apiSourceDirectories[1].directory == apiProjectDir.file('src/main/resources')
    apiSourceDirectories[2].path == 'src/test/java'
    apiSourceDirectories[2].directory == apiProjectDir.file('src/test/java')
    rootProject.tryFind({ it.name == 'impl' } as Spec).get().sourceDirectories == []

    // verify project dependencies
    rootProject.tryFind({ it.name == 'api' } as Spec).get().projectDependencies == []
    def implProjectDependencies = rootProject.tryFind({ it.name == 'impl' } as Spec).get().projectDependencies
    implProjectDependencies.size() == 1
    def apiProjectDependency = implProjectDependencies[0]
    apiProjectDependency.targetProjectDir.name == 'api'
    apiProjectDependency.exported == higherOrEqual('2.5', distribution) ? false : true

    // verify external dependencies
    def apiExternalDependencies = rootProject.tryFind({ it.name == 'api' } as Spec).get().externalDependencies
    apiExternalDependencies.size() == 1
    def guavaDependency = apiExternalDependencies[0]
    guavaDependency.file != null
    guavaDependency.source != null
    guavaDependency.javadoc == null
    guavaDependency.exported == higherOrEqual('2.5', distribution) ? false : true
    if (higherOrEqual('1.1', distribution)) {
      assert guavaDependency.gradleModuleVersion.get().group == 'com.google.guava'
      assert guavaDependency.gradleModuleVersion.get().name == 'guava'
      assert guavaDependency.gradleModuleVersion.get().version == '18.0'
    } else {
      assert !guavaDependency.gradleModuleVersion.isPresent()
    }
    rootProject.tryFind({ it.name == 'impl' } as Spec).get().externalDependencies == []

    where:
    [distribution, environment, repositoryType] << fetchFromBothRepositoriesInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  def "fetchEclipseGradleBuild - build commands and natures"(GradleDistribution distribution, Environment environment, RepositoryType repositoryType) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderMultiProjectBuild.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())

    new TestFile(this.directoryProviderMultiProjectBuild.testDirectory, 'api/build.gradle') << """
      apply plugin: 'eclipse'
      eclipse {
        project {
          natures = ['customNature']
          buildCommands = [new org.gradle.plugins.ide.eclipse.model.BuildCommand('buildCommand', [argKey: 'argValue'])]
        }
      }

    """

    when:
    def rootProject = fetchRootEclipseProject(fixedRequestAttributes, transientRequestAttributes, environment, repositoryType)

    then:
    def apiEclipseProject = rootProject.tryFind({ it.name == 'api' } as Spec).get()
    def projectNatures = apiEclipseProject.projectNatures
    def buildCommands = apiEclipseProject.buildCommands
    if (higherOrEqual('2.9', distribution)) {
      assert projectNatures.isPresent()
      assert projectNatures.get().collect{ it.id } == ['customNature']
      assert buildCommands.isPresent()
      assert buildCommands.get().collect { it.name } == ['buildCommand']
      assert buildCommands.get().collect { it.arguments }.size() == 1
      assert buildCommands.get().collect { it.arguments }[0]['argKey'] == 'argValue'
    } else {
      assert !projectNatures.isPresent()
      assert !buildCommands.isPresent()
    }

    where:
    [distribution, environment, repositoryType] << fetchFromBothRepositoriesInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  def "fetchEclipseGradleBuild - source version settings for non-JVM projects"(GradleDistribution distribution, Environment environment, RepositoryType repositoryType) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())

    when:
    def rootProject = fetchRootEclipseProject(fixedRequestAttributes, transientRequestAttributes, environment, repositoryType)

    then:
    def eclipseProject = rootProject.tryFind({ it.name == 'sub1' } as Spec).get()
    def sourceCompatibility = eclipseProject.javaSourceSettings
    if (higherOrEqual('2.10', distribution)) {
      assert sourceCompatibility.isPresent()
      assert sourceCompatibility.get() == null
    } else {
      assert !sourceCompatibility.isPresent()
    }

    where:
    [distribution, environment, repositoryType] << fetchFromBothRepositoriesInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  def "fetchEclipseGradleBuild - source version settings for JVM projects"(GradleDistribution distribution, Environment environment, RepositoryType repositoryType) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    new TestFile(this.directoryProvider.testDirectory, 'sub1/build.gradle') << """
      apply plugin: 'java'
      apply plugin: 'eclipse'
      eclipse {
        project {
          sourceCompatibility = 1.2
        }
      }
    """

    when:
    def rootProject = fetchRootEclipseProject(fixedRequestAttributes, transientRequestAttributes, environment, repositoryType)

    then:
    def eclipseProject = rootProject.tryFind({ it.name == 'sub1' } as Spec).get()
    def sourceCompatibility = eclipseProject.javaSourceSettings
    if (higherOrEqual('2.10', distribution)) {
      assert sourceCompatibility.isPresent()
      assert sourceCompatibility.get().sourceLanguageLevel.name == '1.2'
    } else {
      assert !sourceCompatibility.isPresent()
    }

    where:
    [distribution, environment, repositoryType] << fetchFromBothRepositoriesInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  def "fetchEclipseGradleBuild - when exception is thrown"(GradleDistribution distribution, Environment environment, RepositoryType repositoryType) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildFile.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())

    AtomicReference<?> publishedEvent = new AtomicReference<>();
    def listener = new Object() {
      @SuppressWarnings("GroovyUnusedDeclaration")
      @Subscribe
      public void listen(EclipseGradleBuildUpdateEvent event) {
        publishedEvent.set(event)
      }

      @SuppressWarnings("GroovyUnusedDeclaration")
      @Subscribe
      public void listen(EclipseWorkspaceUpdateEvent event) {
          publishedEvent.set(event)
      }
    }

    when:
    fetchRootEclipseProject(fixedRequestAttributes, transientRequestAttributes, environment, repositoryType, listener)

    then:
    thrown(GradleConnectionException)

    publishedEvent.get() == null

    where:
    [distribution, environment, repositoryType] << fetchFromBothRepositoriesInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  def "fetchBuildInvocations - send event after cache update"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultSimpleModelRepository(fixedRequestAttributes, toolingClient, new EventBus(), environment)

    AtomicReference<BuildInvocationsUpdateEvent> publishedEvent = new AtomicReference<>();
    AtomicReference<OmniBuildInvocationsContainer> modelInRepository = new AtomicReference<>();
    repository.register(new Object() {

      @SuppressWarnings("GroovyUnusedDeclaration")
      @Subscribe
      public void listen(BuildInvocationsUpdateEvent event) {
        publishedEvent.set(event)
        modelInRepository.set(repository.fetchBuildInvocations(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY))
      }
    })

    when:
    OmniBuildInvocationsContainer buildInvocations = repository.fetchBuildInvocations(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    buildInvocations != null
    buildInvocations.asMap().size() == 4
    def rootProjectExplicitTasks = buildInvocations.get(Path.from(':')).get().projectTasks.findAll { !ImplicitTasks.ALL.contains(it.name) }
    rootProjectExplicitTasks.size() == 1

    def projectSub1 = buildInvocations.get(Path.from(':sub1')).get()
    def sub1ExplicitTasks = projectSub1.projectTasks.findAll { !ImplicitTasks.ALL.contains(it.name) }
    sub1ExplicitTasks.size() == 2

    def myFirstTaskOfSub1 = sub1ExplicitTasks[0]
    myFirstTaskOfSub1.name == 'myFirstTaskOfSub1'
    myFirstTaskOfSub1.description == '1st task of sub1'
    myFirstTaskOfSub1.path.path == ':sub1:myFirstTaskOfSub1'
    myFirstTaskOfSub1.isPublic()

    def mySecondTaskOfSub1 = sub1ExplicitTasks[1]
    mySecondTaskOfSub1.name == 'mySecondTaskOfSub1'
    mySecondTaskOfSub1.description == '2nd task of sub1'
    mySecondTaskOfSub1.path.path == ':sub1:mySecondTaskOfSub1'
    mySecondTaskOfSub1.isPublic() == (!higherOrEqual('2.1', distribution) || !higherOrEqual('2.3', distribution) && environment == Environment.ECLIPSE)
    // only 'authentic' build invocations know the task is private prior to version 2.3

    def projectSub2 = buildInvocations.get(Path.from(':sub2')).get()
    def sub2ExplicitTaskSelectors = projectSub2.taskSelectors.findAll { !ImplicitTasks.ALL.contains(it.name) }
    sub2ExplicitTaskSelectors.size() == 5

    def myTaskSelector = sub2ExplicitTaskSelectors.find { it.name == 'myTask' }
    myTaskSelector.name == 'myTask'
    myTaskSelector.description == higherOrEqual('2.3', distribution) ? 'another task of sub2' : 'sub2:myTask task selector'
    myTaskSelector.projectPath.path == ':sub2'
    myTaskSelector.isPublic()
    myTaskSelector.selectedTaskPaths*.path as List == (!higherOrEqual('1.12', distribution) || !higherOrEqual('2.3', distribution) && environment == Environment.ECLIPSE ? [':sub2:myTask', ':sub2:subSub1:myTask'] : [])
    // empty selected task paths for task selectors from 'authentic' build invocations

    def event = publishedEvent.get()
    event != null
    event.buildInvocations == buildInvocations

    def model = modelInRepository.get()
    model == buildInvocations

    where:
    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  def "fetchBuildInvocations - when exception is thrown"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildFile.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultSimpleModelRepository(fixedRequestAttributes, toolingClient, new EventBus(), environment)

    AtomicReference<BuildInvocationsUpdateEvent> publishedEvent = new AtomicReference<>();
    repository.register(new Object() {

      @SuppressWarnings("GroovyUnusedDeclaration")
      @Subscribe
      public void listen(BuildInvocationsUpdateEvent event) {
        publishedEvent.set(event)
      }
    })

    when:
    repository.fetchBuildInvocations(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    thrown(GradleConnectionException)

    publishedEvent.get() == null

    where:
    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.12")
  }

  def "registerUnregister - no more events are sent to receiver once he is unregistered"() {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, GradleDistribution.fromBuild(), null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultSimpleModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

    AtomicReference<BuildEnvironmentUpdateEvent> publishedEvent = new AtomicReference<>();
    def listener = new Object() {

      @SuppressWarnings("GroovyUnusedDeclaration")
      @Subscribe
      public void listen(BuildEnvironmentUpdateEvent event) {
        publishedEvent.set(event)
      }
    }

    when:
    publishedEvent.set(null)
    repository.register(listener)
    repository.fetchBuildEnvironment(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

    then:
    publishedEvent.get() != null

    when:
    publishedEvent.set(null)
    repository.unregister(listener)
    repository.fetchBuildEnvironment(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

    then:
    publishedEvent.get() == null
  }

  private fetchRootEclipseProject(FixedRequestAttributes fixedRequestAttributes, TransientRequestAttributes transientRequestAttributes, Environment environment, RepositoryType repositoryType, Object listener = new Object()) {
      if (repositoryType == RepositoryType.SIMPLE) {
          def repository = new DefaultSimpleModelRepository(fixedRequestAttributes, toolingClient, new EventBus(), environment)
          repository.register(listener)
          OmniEclipseGradleBuild eclipseGradleBuild = repository.fetchEclipseGradleBuild(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
          return eclipseGradleBuild.rootEclipseProject
      } else {
          def repoProvider = Mock(ModelRepositoryProvider)
          repoProvider.getModelRepository(_) >> new DefaultSimpleModelRepository(fixedRequestAttributes, toolingClient, new EventBus())
          def DefaultCompositeModelRepository repository = new DefaultCompositeModelRepository(repoProvider, [fixedRequestAttributes] as Set, toolingClient, new EventBus())
          repository.register(listener)
          def eclipseWorkspace = repository.fetchEclipseWorkspace(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
          eclipseWorkspace.tryFind{p -> p.parent == null}.get()
      }
  }

  private static enum RepositoryType {
      SIMPLE,
      COMPOSITE
  }

  private static boolean higherOrEqual(String minVersion, GradleDistribution distribution) {
    def gradleVersion = GradleVersion.version(extractVersion(distribution))
    gradleVersion.baseVersion.compareTo(GradleVersion.version(minVersion)) >= 0
  }

  @SuppressWarnings(["GroovyAssignabilityCheck", "GroovyAccessibility"])
  private static String extractVersion(GradleDistribution distribution) {
    if (distribution.version) {
      distribution.version
    } else if (distribution.remoteDistributionUri) {
      GradleVersionExtractor.getVersion(distribution.remoteDistributionUri).get()
    } else {
      throw new IllegalStateException("Cannot extract version from distribution: " + distribution)
    }
  }

  private static ImmutableList<List<Object>> runInAllEnvironmentsForGradleTargetVersions(String versionPattern) {
    GradleVersionParameterization.Default.INSTANCE.getPermutations(versionPattern, Environment.values() as List)
  }

  private static ImmutableList<List<Object>> fetchFromBothRepositoriesInAllEnvironmentsForGradleTargetVersions(String versionPattern) {
      GradleVersionParameterization.Default.INSTANCE.getPermutations(versionPattern, Environment.values() as List, RepositoryType.values() as List)
  }

}
