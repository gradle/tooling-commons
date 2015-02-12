package com.gradleware.tooling.toolingmodel.repository.internal

import com.google.common.collect.ImmutableList
import com.google.common.eventbus.EventBus
import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.ToolingModelToolingClientSpecification
import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.Path
import com.gradleware.tooling.toolingmodel.repository.Environment
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProgressListener
import org.junit.Rule

class DefaultModelRepositoryCacheTest extends ToolingModelToolingClientSpecification {

  @Rule
  TestDirectoryProvider directoryProvider = new TestDirectoryProvider();

  FixedRequestAttributes fixedRequestAttributes
  TransientRequestAttributes transientRequestAttributes
  DefaultModelRepository repository

  def setup() {
    // Gradle projects for testing
    directoryProvider.createFile('settings.gradle')
    directoryProvider.createFile('build.gradle') << 'task myTask {}'

    // request attributes and model repository for testing
    fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, GradleDistribution.fromBuild(), null, ImmutableList.of(), ImmutableList.of())
    transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())
  }

  def "fetchBuildEnvironment"() {
    when:
    def lookUp = repository.fetchBuildEnvironment(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY)

    then:
    lookUp == null

    when:
    def firstLookUp = repository.fetchBuildEnvironment(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
    def secondLookUp = repository.fetchBuildEnvironment(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    firstLookUp != null
    firstLookUp.is(secondLookUp)

    when:
    def thirdLookUp = repository.fetchBuildEnvironment(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)
    def fourthLookUp = repository.fetchBuildEnvironment(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

    then:
    thirdLookUp != null
    !thirdLookUp.is(fourthLookUp)
    thirdLookUp.gradle.gradleUserHome.get() == fourthLookUp.gradle.gradleUserHome.get()
    thirdLookUp.gradle.gradleVersion == fourthLookUp.gradle.gradleVersion
    thirdLookUp.java.javaHome == fourthLookUp.java.javaHome
    thirdLookUp.java.jvmArguments == fourthLookUp.java.jvmArguments
  }

  def "fetchGradleBuildStructure"() {
    when:
    def lookUp = repository.fetchGradleBuildStructure(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY)

    then:
    lookUp == null

    when:
    def firstLookUp = repository.fetchGradleBuildStructure(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
    def secondLookUp = repository.fetchGradleBuildStructure(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    firstLookUp != null
    firstLookUp.is(secondLookUp)

    when:
    def thirdLookUp = repository.fetchGradleBuildStructure(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)
    def fourthLookUp = repository.fetchGradleBuildStructure(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

    then:
    thirdLookUp != null
    !thirdLookUp.is(fourthLookUp)
    thirdLookUp.rootProject.path == fourthLookUp.rootProject.path
    thirdLookUp.rootProject.all.size() == fourthLookUp.rootProject.all.size()
  }

  def "fetchGradleBuild"() {
    when:
    def lookUp = repository.fetchGradleBuild(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY)

    then:
    lookUp == null

    when:
    def firstLookUp = repository.fetchGradleBuild(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
    def secondLookUp = repository.fetchGradleBuild(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    firstLookUp != null
    firstLookUp.is(secondLookUp)

    when:
    def thirdLookUp = repository.fetchGradleBuild(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)
    def fourthLookUp = repository.fetchGradleBuild(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

    then:
    thirdLookUp != null
    !thirdLookUp.is(fourthLookUp)
    thirdLookUp.rootProject.path == fourthLookUp.rootProject.path
    thirdLookUp.rootProject.all.size() == fourthLookUp.rootProject.all.size()
  }

  def "fetchEclipseGradleBuild"() {
    when:
    def lookUp = repository.fetchEclipseGradleBuild(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY)

    then:
    lookUp == null

    when:
    def firstLookUp = repository.fetchEclipseGradleBuild(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
    def secondLookUp = repository.fetchEclipseGradleBuild(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    firstLookUp != null
    firstLookUp.is(secondLookUp)

    when:
    def thirdLookUp = repository.fetchEclipseGradleBuild(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)
    def fourthLookUp = repository.fetchEclipseGradleBuild(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

    then:
    thirdLookUp != null
    !thirdLookUp.is(fourthLookUp)
    thirdLookUp.rootProject.path == fourthLookUp.rootProject.path
    thirdLookUp.rootProject.all.size() == fourthLookUp.rootProject.all.size()
  }

  def "fetchBuildInvocations"() {
    when:
    def lookUp = repository.fetchBuildInvocations(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY)

    then:
    lookUp == null

    when:
    def firstLookUp = repository.fetchBuildInvocations(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
    def secondLookUp = repository.fetchBuildInvocations(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    firstLookUp != null
    firstLookUp.is(secondLookUp)

    when:
    def thirdLookUp = repository.fetchBuildInvocations(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)
    def fourthLookUp = repository.fetchBuildInvocations(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

    then:
    thirdLookUp != null
    !thirdLookUp.is(fourthLookUp)
    thirdLookUp.asMap()[Path.from(':')].projectTasks.size() == fourthLookUp.asMap()[Path.from(':')].projectTasks.size()
    thirdLookUp.asMap()[Path.from(':')].taskSelectors.size() == fourthLookUp.asMap()[Path.from(':')].taskSelectors.size()
  }

  def "fetchBuildInvocations - fallback scenarios"() {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, GradleDistribution.forVersion('2.2'), null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus(), Environment.ECLIPSE)

    when:
    def lookUp = repository.fetchBuildInvocations(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY)

    then:
    lookUp == null
    repository.fetchGradleBuild(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY) == null

    when:
    def firstLookUp = repository.fetchBuildInvocations(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
    def secondLookUp = repository.fetchBuildInvocations(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    firstLookUp != null
    firstLookUp.is(secondLookUp)
    repository.fetchGradleBuild(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY) != null

    when:
    def thirdLookUp = repository.fetchBuildInvocations(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)
    def fourthLookUp = repository.fetchBuildInvocations(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

    then:
    thirdLookUp != null
    !thirdLookUp.is(fourthLookUp)
    thirdLookUp.asMap()[Path.from(':')].projectTasks.size() == fourthLookUp.asMap()[Path.from(':')].projectTasks.size()
    thirdLookUp.asMap()[Path.from(':')].taskSelectors.size() == fourthLookUp.asMap()[Path.from(':')].taskSelectors.size()
    repository.fetchGradleBuild(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY) != null
  }

}
