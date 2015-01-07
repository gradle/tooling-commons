package com.gradleware.tooling.domain.internal

import com.google.common.collect.ImmutableList
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.gradleware.tooling.domain.BuildEnvironmentUpdateEvent
import com.gradleware.tooling.domain.Environment
import com.gradleware.tooling.domain.FetchStrategy
import com.gradleware.tooling.domain.FixedRequestAttributes
import com.gradleware.tooling.domain.TransientRequestAttributes
import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.DataValueFormatter
import com.gradleware.tooling.spock.DomainToolingClientSpecification
import com.gradleware.tooling.spock.VerboseUnroll
import com.gradleware.tooling.testing.GradleVersionExtractor
import com.gradleware.tooling.testing.GradleVersionParameterization
import com.gradleware.tooling.toolingapi.GradleDistribution
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProgressListener
import org.gradle.tooling.model.build.BuildEnvironment
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
    // Gradle projects for testing
    directoryProvider.createFile('settings.gradle')
    directoryProvider.createFile('build.gradle') << 'task myTask {}'

    directoryProviderErroneousBuildStructure.createFile('settings.gradle') << 'include foo'

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
    def buildEnvironment = repository.fetchBuildEnvironmentAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

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

//  def "fetchGradleBuildAndWait"() {
//    given:
//    AtomicReference<GradleBuildUpdateEvent> publishedEvent = new AtomicReference<>();
//    repository.register(new Object() {
//
//      @Subscribe
//      public void listen(GradleBuildUpdateEvent event) {
//        publishedEvent.set(event)
//      }
//    })
//
//    when:
//    repository.fetchGradleBuildAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//
//    then:
//    def event = publishedEvent.get()
//    event != null
//    publishedEvent.get().gradleBuild != null
//    publishedEvent.get().gradleBuild.rootProject != null
//  }
//
//  def "fetchGradleBuildAndWaitWhenExceptionIsThrown"() {
//    given:
//    def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildStructure.testDirectory, null, GradleDistribution.fromBuild(), null, ImmutableList.of(), ImmutableList.of())
//    transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
//    def repository = new NewDefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())
//
//    AtomicReference<BuildInvocationsUpdateEvent> publishedEvent = new AtomicReference<>();
//    repository.register(new Object() {
//
//      @Subscribe
//      public void listen(BuildInvocationsUpdateEvent event) {
//        publishedEvent.set(event)
//      }
//    })
//
//    when:
//    repository.fetchGradleBuildAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//
//    then:
//    thrown(GradleConnectionException)
//
//    publishedEvent.get() == null
//  }

  def "registerUnregister"() {
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
