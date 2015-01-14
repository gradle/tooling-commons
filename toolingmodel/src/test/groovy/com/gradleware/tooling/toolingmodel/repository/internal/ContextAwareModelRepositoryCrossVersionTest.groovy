package com.gradleware.tooling.toolingmodel.repository.internal

import com.google.common.collect.ImmutableList
import com.google.common.eventbus.EventBus
import com.gradleware.tooling.toolingmodel.repository.BuildInvocationsContainer
import com.gradleware.tooling.toolingmodel.repository.Environment
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes
import com.gradleware.tooling.toolingmodel.util.Pair
import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.DataValueFormatter
import com.gradleware.tooling.spock.DomainToolingClientSpecification
import com.gradleware.tooling.spock.VerboseUnroll
import com.gradleware.tooling.testing.GradleVersionExtractor
import com.gradleware.tooling.testing.GradleVersionParameterization
import com.gradleware.tooling.toolingclient.GradleDistribution
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProgressListener
import org.gradle.tooling.model.GradleProject
import org.gradle.util.GradleVersion
import org.junit.Rule

@VerboseUnroll(formatter = GradleDistributionFormatter.class)
class ContextAwareModelRepositoryCrossVersionTest extends DomainToolingClientSpecification {

  @Rule
  public TestDirectoryProvider directoryProvider = new TestDirectoryProvider();

  def setup() {
    directoryProvider.createFile('settings.gradle') << '''
rootProject.name = 'TestProject'
'''
    directoryProvider.createFile('build.gradle') << '''
       description = 'my project'
       task myTask {}
    '''
  }

  def "fetchBuildEnvironmentAndWait_everything"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def targetRepository = new DefaultModelRepository(fixedRequestAttributes, new EventBus(), toolingClient)
    def repository = new ContextAwareModelRepository(targetRepository, environment)

    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())

    when:
    def buildEnvironment = repository.fetchBuildEnvironmentAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

    then:
    buildEnvironment != null
    buildEnvironment.gradle != null
    buildEnvironment.gradle.gradleVersion == extractVersion(distribution)
    buildEnvironment.java != null
    buildEnvironment.java.javaHome != null
    buildEnvironment.java.jvmArguments != null

    where:
    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  def "fetchGradleBuild_withoutProjectDirectory"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def targetRepository = new DefaultModelRepository(fixedRequestAttributes, new EventBus(), toolingClient)
    def repository = new ContextAwareModelRepository(targetRepository, environment)

    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())

    when:
    def gradleBuild = repository.fetchGradleBuildAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

    then:
    gradleBuild != null
    gradleBuild.rootProject != null
    gradleBuild.rootProject.name == 'TestProject'
    gradleBuild.rootProject.path == ":"
    gradleBuild.rootProject.parent == null
    gradleBuild.rootProject.children.size() == 0

    where:
    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  def "fetchGradleBuild_withProjectDirectory"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def targetRepository = new DefaultModelRepository(fixedRequestAttributes, new EventBus(), toolingClient)
    def repository = new ContextAwareModelRepository(targetRepository, environment)

    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())

    when:
    def gradleBuild = repository.fetchGradleBuildAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

    then:
    gradleBuild != null
    gradleBuild.rootProject != null
    gradleBuild.rootProject.projectDirectory.absolutePath == directoryProvider.testDirectory.absolutePath

    where:
    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.8")
  }

  def "fetchGradleProject_withoutBuildDirectory_withoutBuildScript"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def targetRepository = new DefaultModelRepository(fixedRequestAttributes, new EventBus(), toolingClient)
    def repository = new ContextAwareModelRepository(targetRepository, environment)

    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())

    when:
    def gradleProject = repository.fetchGradleProjectAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

    then:
    gradleProject != null
    gradleProject.name == 'TestProject'
    gradleProject.description == 'my project'
    gradleProject.path == ':'
    gradleProject.tasks.size() == getImplicitlyAddedGradleProjectTasksCount(distribution) + 1
    gradleProject.parent == null
    gradleProject.children.size() == 0

    where:
    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  def "fetchGradleProject_withBuildScript"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def targetRepository = new DefaultModelRepository(fixedRequestAttributes, new EventBus(), toolingClient)
    def repository = new ContextAwareModelRepository(targetRepository, environment)

    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())

    when:
    def gradleProject = repository.fetchGradleProjectAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

    then:
    gradleProject != null
    gradleProject.buildScript.sourceFile.absolutePath == directoryProvider.file('build.gradle').absolutePath

    where:
    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.8")
  }

  def "fetchGradleProject_withBuildDirectory"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def targetRepository = new DefaultModelRepository(fixedRequestAttributes, new EventBus(), toolingClient)
    def repository = new ContextAwareModelRepository(targetRepository, environment)

    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())

    when:
    def gradleProject = repository.fetchGradleProjectAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

    then:
    gradleProject != null
    gradleProject.buildDirectory.absolutePath == new File(directoryProvider.testDirectory, 'build').absolutePath

    where:
    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=2.0")
  }

  def "fetchBuildInvocations"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def targetRepository = new DefaultModelRepository(fixedRequestAttributes, new EventBus(), toolingClient)
    def repository = new ContextAwareModelRepository(targetRepository, environment)

    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())

    when:
    def buildInvocations = repository.fetchBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

    then:
    buildInvocations != null
    buildInvocations.asMap() != null
    buildInvocations.asMap().get(':').tasks.size() == getImplicitlyAddedBuildInvocationsTasksCount(distribution, environment) + getImplicitlyAddedGradleProjectTasksCount(distribution) + 1
    buildInvocations.asMap().get(':').taskSelectors.size() == getImplicitlyAddedBuildInvocationsTaskSelectorsCount(distribution, environment) + getImplicitlyAddedGradleProjectTaskSelectorsCount(distribution) + 1

    where:
    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  def "fetchGradleProjectWithBuildInvocations"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def targetRepository = new DefaultModelRepository(fixedRequestAttributes, new EventBus(), toolingClient)
    def repository = new ContextAwareModelRepository(targetRepository, environment)

    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())

    when:
    Pair<GradleProject, BuildInvocationsContainer> gradleProjectAndBuildInvocations = repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

    then:
    gradleProjectAndBuildInvocations != null
    gradleProjectAndBuildInvocations.first != null
    gradleProjectAndBuildInvocations.first.name == 'TestProject'
    gradleProjectAndBuildInvocations.first.description == 'my project'
    gradleProjectAndBuildInvocations.first.path == ':'
    gradleProjectAndBuildInvocations.first.tasks.size() == getImplicitlyAddedGradleProjectTasksCount(distribution) + 1
    gradleProjectAndBuildInvocations.first.parent == null
    gradleProjectAndBuildInvocations.first.children.size() == 0
    gradleProjectAndBuildInvocations.second.asMap() != null
    gradleProjectAndBuildInvocations.second.asMap().get(':').tasks.size() == getImplicitlyAddedBuildInvocationsTasksCount(distribution, environment) + getImplicitlyAddedGradleProjectTasksCount(distribution) + 1
    gradleProjectAndBuildInvocations.second.asMap().get(':').taskSelectors.size() == getImplicitlyAddedBuildInvocationsTaskSelectorsCount(distribution, environment) + getImplicitlyAddedGradleProjectTaskSelectorsCount(distribution) + 1

    where:
    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  private static int getImplicitlyAddedBuildInvocationsTasksCount(GradleDistribution distribution, Environment environment) {
    // tasks implicitly provided by BuildInvocations#getTasks(): init, wrapper, help, properties, projects, tasks, dependencies, dependencyInsight, components
    def version = GradleVersion.version(extractVersion(distribution))
    version.baseVersion.compareTo(GradleVersion.version("2.3")) >= 0 || version.baseVersion.compareTo(GradleVersion.version("2.1")) >= 0 && environment != Environment.ECLIPSE ? 9 : version.baseVersion.compareTo(GradleVersion.version("2.0")) >= 0 && environment != Environment.ECLIPSE ? 8 : 0 }

  private static int getImplicitlyAddedBuildInvocationsTaskSelectorsCount(GradleDistribution distribution, Environment environment) {
    // tasks implicitly provided by BuildInvocations#getTaskSelectors():
    def version = GradleVersion.version(extractVersion(distribution));
    version.baseVersion.compareTo(GradleVersion.version("2.3")) >= 0 || version.baseVersion.compareTo(GradleVersion.version("2.1")) >= 0 && environment != Environment.ECLIPSE ? 9 : 0
  }

  private static int getImplicitlyAddedGradleProjectTasksCount(GradleDistribution distribution) {
    // tasks implicitly provided by GradleProject#getTasks(): setupBuild
    def version = GradleVersion.version(extractVersion(distribution))
    version.compareTo(GradleVersion.version("1.6")) == 0 ? 1 : 0
  }

  private static int getImplicitlyAddedGradleProjectTaskSelectorsCount(GradleDistribution distribution) {
    // tasks implicitly provided by GradleProject#getTasks(): setupBuild
    def version = GradleVersion.version(extractVersion(distribution))
    version.compareTo(GradleVersion.version("1.6")) == 0 ? 1 : 0
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
  public static class GradleDistributionFormatter implements DataValueFormatter {

    @SuppressWarnings("GroovyAccessibility")
    @Override
    public String format(Object input) {
      if (input instanceof GradleDistribution) {
        if (input.localInstallationDir != null) {
          // extract last path segment from path
          File dir = input.localInstallationDir
          return dir.absolutePath.substring(dir.absolutePath.lastIndexOf('/' + 1))
        } else if (input.remoteDistributionUri != null) {
          // convert https://services.gradle.org/distributions-snapshots/gradle-2.3-20141212203103+0000-bin.zip to 2.3-20141212203103+0000
          URI uri = input.remoteDistributionUri
          def lastPathSegment = uri.path.substring(uri.path.lastIndexOf('/') + 1)
          return lastPathSegment.substring("gradle-".length(), lastPathSegment.lastIndexOf('-'))
        } else if (input.version != null) {
          // no conversion necessary
          return input.version
        }
      }
      String.valueOf(input);
    }

  }

}
