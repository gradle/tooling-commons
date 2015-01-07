package com.gradleware.tooling.domain.internal

import com.google.common.collect.ImmutableList
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.gradleware.tooling.domain.BuildEnvironmentUpdateEvent
import com.gradleware.tooling.domain.BuildInvocationsUpdateEvent
import com.gradleware.tooling.domain.Environment
import com.gradleware.tooling.domain.FetchStrategy
import com.gradleware.tooling.domain.FixedRequestAttributes
import com.gradleware.tooling.domain.GradleBuildUpdateEvent
import com.gradleware.tooling.domain.GradleProjectUpdateEvent
import com.gradleware.tooling.domain.TransientRequestAttributes
import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.DataValueFormatter
import com.gradleware.tooling.spock.DomainToolingClientSpecification
import com.gradleware.tooling.spock.VerboseUnroll
import com.gradleware.tooling.testing.GradleVersionExtractor
import com.gradleware.tooling.testing.GradleVersionParameterization
import com.gradleware.tooling.toolingapi.GradleDistribution
import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProgressListener
import org.gradle.tooling.model.GradleProject
import org.gradle.tooling.model.build.BuildEnvironment
import org.gradle.tooling.model.gradle.GradleBuild
import org.gradle.util.GradleVersion
import org.junit.Rule
import org.spockframework.util.Assert

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

    AtomicReference<BuildEnvironmentUpdateEvent> publishedEvent = new AtomicReference<>();
    AtomicReference<BuildEnvironment> modelInRepository = new AtomicReference<>();
    repository.register(new Object() {

      @SuppressWarnings("GroovyUnusedDeclaration")
      @Subscribe
      public void listen(BuildEnvironmentUpdateEvent event) {
        publishedEvent.set(event)
        modelInRepository.set(repository.fetchBuildEnvironmentAndWait(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY))
      }
    })

    when:
    BuildEnvironment buildEnvironment = repository.fetchBuildEnvironmentAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

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

    AtomicReference<GradleBuildUpdateEvent> publishedEvent = new AtomicReference<>();
    AtomicReference<GradleBuild> modelInRepository = new AtomicReference<>();
    repository.register(new Object() {

      @SuppressWarnings("GroovyUnusedDeclaration")
      @Subscribe
      public void listen(GradleBuildUpdateEvent event) {
        publishedEvent.set(event)
        modelInRepository.set(repository.fetchGradleBuildAndWait(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY))
      }
    })

    when:
    GradleBuild gradleBuild = repository.fetchGradleBuildAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    gradleBuild != null
    gradleBuild.rootProject != null
    gradleBuild.rootProject.name == 'my root project'
    gradleBuild.rootProject.path == ':'
    gradleBuild.rootProject.parent == null
    gradleBuild.rootProject.children.size() == 2
    gradleBuild.rootProject.children*.name as Set == ['sub1', 'sub2'] as Set
    gradleBuild.rootProject.children*.path as Set == [':sub1', ':sub2'] as Set
    gradleBuild.rootProject.children*.parent as Set == [gradleBuild.rootProject] as Set
    gradleBuild.projects.size() == 4
    gradleBuild.projects*.name as Set == ['my root project', 'sub1', 'sub2', 'subSub1'] as Set

    if (higherOrEqual("1.8", distribution)) {
      gradleBuild.rootProject.projectDirectory.absolutePath == directoryProvider.testDirectory.absolutePath
    } else {
      try {
        gradleBuild.rootProject.projectDirectory
        Assert.fail("BasicGradleProject#projectDirectory should not be supported", distribution)
      } catch (Exception ignored) {
        // expected
      }
    }

    def event = publishedEvent.get()
    event != null
    event.gradleBuild == gradleBuild

    def model = modelInRepository.get()
    model == gradleBuild

    where:
    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  def "fetchGradleBuildAndWait - when exception is thrown"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildStructure.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new NewDefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

    AtomicReference<GradleBuildUpdateEvent> publishedEvent = new AtomicReference<>();
    repository.register(new Object() {

      @SuppressWarnings("GroovyUnusedDeclaration")
      @Subscribe
      public void listen(GradleBuildUpdateEvent event) {
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

    AtomicReference<GradleProjectUpdateEvent> publishedEvent = new AtomicReference<>();
    AtomicReference<GradleProject> modelInRepository = new AtomicReference<>();
    repository.register(new Object() {

      @SuppressWarnings("GroovyUnusedDeclaration")
      @Subscribe
      public void listen(GradleProjectUpdateEvent event) {
        publishedEvent.set(event)
        modelInRepository.set(repository.fetchGradleProjectAndWait(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY))
      }
    })

    when:
    GradleProject gradleProject = repository.fetchGradleProjectAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    gradleProject != null
    gradleProject.name == 'my root project'
    gradleProject.description == 'a sample root project'
    gradleProject.path == ':'
    gradleProject.tasks.size() == getImplicitlyAddedGradleProjectTasksCount(distribution) + 1
    gradleProject.parent == null
    gradleProject.children.size() == 2
    gradleProject.children*.name as Set == ['sub1', 'sub2'] as Set
    gradleProject.children*.description as Set == ['sub project 1', 'sub project 2'] as Set
    gradleProject.children*.path as Set == [':sub1', ':sub2'] as Set
    gradleProject.children*.parent as Set == [gradleProject] as Set

    if (higherOrEqual("1.8", distribution)) {
      gradleProject.buildScript.sourceFile.absolutePath == directoryProvider.file('build.gradle').absolutePath
    } else {
      try {
        gradleProject.buildScript
        Assert.fail("GradleProject#buildScript should not be supported", distribution)
      } catch (Exception ignored) {
        // expected
      }
    }

    if (higherOrEqual("2.0", distribution)) {
      gradleProject.buildDirectory.absolutePath == new File(directoryProvider.testDirectory, 'build').absolutePath
    } else {
      try {
        gradleProject.buildDirectory
        Assert.fail("GradleProject#buildDirectory should not be supported", distribution)
      } catch (Exception ignored) {
        // expected
      }
    }

    gradleProject.findByPath(':sub1').tasks.size() == 2
    def myFirstTaskOfSub1 = gradleProject.findByPath(':sub1').tasks.find { it.name == 'myFirstTaskOfSub1' }
    myFirstTaskOfSub1.description == '1st task of sub1'
    myFirstTaskOfSub1.displayName == "task ':sub1:myFirstTaskOfSub1'"

    if (GradleVersion.version(extractVersion(distribution)).version.startsWith("2.3")) {
      assert myFirstTaskOfSub1.public
    } else if (higherOrEqual("2.1", distribution)) {
      assert !myFirstTaskOfSub1.public
    } else {
      try {
        myFirstTaskOfSub1.public
        Assert.fail("GradleTask#public should not be supported", distribution)
      } catch (Exception ignored) {
        // expected
      }
    }

    def event = publishedEvent.get()
    event != null
    event.gradleProject == gradleProject

    def model = modelInRepository.get()
    model == gradleProject

    where:
    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  def "fetchGradleProjectAndWait - when exception is thrown"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildFile.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new NewDefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

    AtomicReference<BuildInvocationsUpdateEvent> publishedEvent = new AtomicReference<>();
    repository.register(new Object() {

      @SuppressWarnings("GroovyUnusedDeclaration")
      @Subscribe
      public void listen(BuildInvocationsUpdateEvent event) {
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

  private static boolean higherOrEqual(String referenceVersion, GradleDistribution distribution) {
    def gradleVersion = GradleVersion.version(extractVersion(distribution))
    gradleVersion.compareTo(GradleVersion.version(referenceVersion)) >= 0
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
