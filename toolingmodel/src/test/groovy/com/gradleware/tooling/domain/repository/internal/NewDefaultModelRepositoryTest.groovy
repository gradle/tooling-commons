package com.gradleware.tooling.domain.repository.internal

import com.google.common.base.Predicate
import com.google.common.collect.ImmutableList
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.gradleware.tooling.domain.OmniBuildEnvironment
import com.gradleware.tooling.domain.OmniGradleBuild
import com.gradleware.tooling.domain.OmniGradleBuildStructure
import com.gradleware.tooling.domain.OmniGradleProject
import com.gradleware.tooling.domain.repository.Environment
import com.gradleware.tooling.domain.repository.FetchStrategy
import com.gradleware.tooling.domain.repository.FixedRequestAttributes
import com.gradleware.tooling.domain.repository.NewBuildEnvironmentUpdateEvent
import com.gradleware.tooling.domain.repository.NewGradleBuildStructureUpdateEvent
import com.gradleware.tooling.domain.repository.NewGradleBuildUpdateEvent
import com.gradleware.tooling.domain.repository.TransientRequestAttributes
import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.DataValueFormatter
import com.gradleware.tooling.spock.DomainToolingClientSpecification
import com.gradleware.tooling.spock.VerboseUnroll
import com.gradleware.tooling.testing.GradleVersionExtractor
import com.gradleware.tooling.testing.GradleVersionParameterization
import com.gradleware.tooling.toolingclient.GradleDistribution
import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProgressListener
import org.gradle.util.GradleVersion
import org.junit.Rule

import java.util.concurrent.atomic.AtomicReference

@VerboseUnroll(formatter = GradleDistributionFormatter.class)
class NewDefaultModelRepositoryTest extends DomainToolingClientSpecification {

  @Rule
  TestDirectoryProvider directoryProvider = new TestDirectoryProvider();

  @Rule
  TestDirectoryProvider directoryProviderErroneousBuildStructure = new TestDirectoryProvider();

  @Rule
  TestDirectoryProvider directoryProviderErroneousBuildFile = new TestDirectoryProvider();

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

    // prepare a Gradle build that has an erroneous structure
    directoryProviderErroneousBuildStructure.createFile('settings.gradle') << 'include foo'

    // prepare a Gradle build that has a valid structure but an erroneous build script
    directoryProviderErroneousBuildFile.createFile('settings.gradle')
    directoryProviderErroneousBuildFile.createFile('build.gradle') << 'task myTask {'
  }

  def "fetchBuildEnvironmentAndWait - send event after cache update"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new NewDefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

    AtomicReference<NewBuildEnvironmentUpdateEvent> publishedEvent = new AtomicReference<>();
    AtomicReference<OmniBuildEnvironment> modelInRepository = new AtomicReference<>();
    repository.register(new Object() {

      @SuppressWarnings("GroovyUnusedDeclaration")
      @Subscribe
      public void listen(NewBuildEnvironmentUpdateEvent event) {
        publishedEvent.set(event)
        modelInRepository.set(repository.fetchBuildEnvironmentAndWait(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY))
      }
    })

    when:
    OmniBuildEnvironment buildEnvironment = repository.fetchBuildEnvironmentAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    buildEnvironment != null
    buildEnvironment.gradle != null
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

  def "fetchGradleBuildAndWait - send event after cache update"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new NewDefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

    AtomicReference<NewGradleBuildStructureUpdateEvent> publishedEvent = new AtomicReference<>();
    AtomicReference<OmniGradleBuildStructure> modelInRepository = new AtomicReference<>();
    repository.register(new Object() {

      @SuppressWarnings("GroovyUnusedDeclaration")
      @Subscribe
      public void listen(NewGradleBuildStructureUpdateEvent event) {
        publishedEvent.set(event)
        modelInRepository.set(repository.fetchGradleBuildAndWait(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY))
      }
    })

    when:
    OmniGradleBuildStructure gradleBuildStructure = repository.fetchGradleBuildAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    gradleBuildStructure != null
    gradleBuildStructure.rootProject != null
    gradleBuildStructure.rootProject.name == 'my root project'
    gradleBuildStructure.rootProject.path == ':'
    gradleBuildStructure.rootProject.projectDirectory?.absolutePath == (higherOrEqual("1.8", distribution) ? directoryProvider.testDirectory.absolutePath : null)
    gradleBuildStructure.rootProject.parent == null
    gradleBuildStructure.rootProject.children.size() == 2
    gradleBuildStructure.rootProject.children*.name == ['sub1', 'sub2']
    gradleBuildStructure.rootProject.children*.path == [':sub1', ':sub2']
    gradleBuildStructure.rootProject.children*.projectDirectory.collect {
      it?.absolutePath
    } == (higherOrEqual("1.8", distribution) ? ['sub1', 'sub2'].collect { new File(directoryProvider.testDirectory, it).absolutePath } : [null, null])
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

  def "fetchGradleBuildAndWait - when exception is thrown"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildStructure.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new NewDefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

    AtomicReference<NewGradleBuildStructureUpdateEvent> publishedEvent = new AtomicReference<>();
    repository.register(new Object() {

      @SuppressWarnings("GroovyUnusedDeclaration")
      @Subscribe
      public void listen(NewGradleBuildStructureUpdateEvent event) {
        publishedEvent.set(event)
      }
    })

    when:
    repository.fetchGradleBuildAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    thrown(GradleConnectionException)

    publishedEvent.get() == null

    where:
    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  def "fetchGradleProjectAndWait - send event after cache update"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new NewDefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

    AtomicReference<NewGradleBuildUpdateEvent> publishedEvent = new AtomicReference<>();
    AtomicReference<OmniGradleBuild> modelInRepository = new AtomicReference<>();
    repository.register(new Object() {

      @SuppressWarnings("GroovyUnusedDeclaration")
      @Subscribe
      public void listen(NewGradleBuildUpdateEvent event) {
        publishedEvent.set(event)
        modelInRepository.set(repository.fetchGradleProjectAndWait(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY))
      }
    })

    when:
    OmniGradleBuild gradleBuild = repository.fetchGradleProjectAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    gradleBuild != null
    gradleBuild.rootProject != null
    gradleBuild.rootProject.name == 'my root project'
    gradleBuild.rootProject.description == 'a sample root project'
    gradleBuild.rootProject.path == ':'
    gradleBuild.rootProject.projectDirectory?.absolutePath == (higherOrEqual("2.4", distribution) ? directoryProvider.testDirectory.absolutePath : null)
    gradleBuild.rootProject.buildDirectory?.absolutePath == (higherOrEqual("2.0", distribution) ? directoryProvider.file('build').absolutePath : null)
    gradleBuild.rootProject.buildScript.sourceFile?.absolutePath == (higherOrEqual("1.8", distribution) ? directoryProvider.file('build.gradle').absolutePath : null)
    gradleBuild.rootProject.projectTasks.size() == getImplicitlyAddedGradleProjectTasksCount(distribution) + 1
    gradleBuild.rootProject.parent == null
    gradleBuild.rootProject.children.size() == 2
    gradleBuild.rootProject.children*.name == ['sub1', 'sub2']
    gradleBuild.rootProject.children*.description == ['sub project 1', 'sub project 2']
    gradleBuild.rootProject.children*.path == [':sub1', ':sub2']
    gradleBuild.rootProject.children*.parent == [gradleBuild.rootProject, gradleBuild.rootProject]
    gradleBuild.rootProject.all.size() == 4
    gradleBuild.rootProject.all*.name == ['my root project', 'sub1', 'sub2', 'subSub1']

    def projectSub1 = gradleBuild.rootProject.tryFind({ OmniGradleProject input ->
      return input.getPath().equals(':sub1')
    } as Predicate).get()
    projectSub1.projectTasks.size() == 2

    def myFirstTaskOfSub1 = projectSub1.projectTasks[0]
    myFirstTaskOfSub1.name == 'myFirstTaskOfSub1'
    myFirstTaskOfSub1.description == '1st task of sub1'
    myFirstTaskOfSub1.path == ':sub1:myFirstTaskOfSub1'
    myFirstTaskOfSub1.isPublic()

    def mySecondTaskOfSub1 = projectSub1.projectTasks[1]
    mySecondTaskOfSub1.name == 'mySecondTaskOfSub1'
    mySecondTaskOfSub1.description == '2nd task of sub1'
    mySecondTaskOfSub1.path == ':sub1:mySecondTaskOfSub1'
    mySecondTaskOfSub1.isPublic() == !higherOrEqual("2.3", distribution) // all versions < 2.3 are corrected to or default to 'true'

    def projectSub2 = gradleBuild.rootProject.tryFind({ OmniGradleProject input ->
      return input.getPath().equals(':sub2')
    } as Predicate).get()
    projectSub2.taskSelectors.size() == 5

    def myTaskSelector = projectSub2.taskSelectors.find { it.name == 'myTask' }
    myTaskSelector.name == 'myTask'
    myTaskSelector.description == 'another task of sub2'
    myTaskSelector.isPublic()
    myTaskSelector.selectedTaskPaths as List == [':sub2:myTask', ':sub2:subSub1:myTask']

    def event = publishedEvent.get()
    event != null
    event.gradleBuild == gradleBuild

    def model = modelInRepository.get()
    model == gradleBuild

    where:
    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  def "fetchGradleProjectAndWait - when exception is thrown"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildFile.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new NewDefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

    AtomicReference<NewGradleBuildUpdateEvent> publishedEvent = new AtomicReference<>();
    repository.register(new Object() {

      @SuppressWarnings("GroovyUnusedDeclaration")
      @Subscribe
      public void listen(NewGradleBuildUpdateEvent event) {
        publishedEvent.set(event)
      }
    })

    when:
    repository.fetchGradleProjectAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    thrown(GradleConnectionException)

    publishedEvent.get() == null

    where:
    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  def "registerUnregister - no more events are sent to receiver once he is unregistered"() {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, GradleDistribution.fromBuild(), null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new NewDefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

    AtomicReference<NewBuildEnvironmentUpdateEvent> publishedEvent = new AtomicReference<>();
    def listener = new Object() {

      @SuppressWarnings("GroovyUnusedDeclaration")
      @Subscribe
      public void listen(NewBuildEnvironmentUpdateEvent event) {
        publishedEvent.set(event)
      }
    }

    when:
    publishedEvent.set(null)
    repository.register(listener)
    repository.fetchBuildEnvironmentAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

    then:
    publishedEvent.get() != null

    when:
    publishedEvent.set(null)
    repository.unregister(listener)
    repository.fetchBuildEnvironmentAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

    then:
    publishedEvent.get() == null
  }

  private static int getImplicitlyAddedGradleProjectTasksCount(GradleDistribution distribution) {
    // tasks implicitly provided by GradleProject#getTasks(): setupBuild
    def version = GradleVersion.version(extractVersion(distribution))
    version.compareTo(GradleVersion.version("1.6")) == 0 ? 1 : 0
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

  /**
   * Custom formatting for the {@link GradleDistribution} data value used in Spock test parameterization. The main purpose is to have shorter string representations than what is
   * returned by {@link GradleDistribution#toString()}. The main reason being that TeamCity truncates long test names which leads to tests with identical names and a false test
   * count is displayed as a consequence.
   */
  public static final class GradleDistributionFormatter implements DataValueFormatter {

    @SuppressWarnings("GroovyAccessibility")
    @Override
    public String format(Object input) {
      if (input instanceof GradleDistribution) {
        if (input.localInstallationDir != null) {
          File dir = input.localInstallationDir
          dir.absolutePath.substring(dir.absolutePath.lastIndexOf(File.separatorChar) + 1) // last path segment
        } else if (input.remoteDistributionUri != null) {
          GradleVersionExtractor.getVersion(input.remoteDistributionUri).get()
        } else if (input.version != null) {
          input.version
        } else {
          String.valueOf(input);
        }
      } else {
        String.valueOf(input);
      }
    }

  }

}
