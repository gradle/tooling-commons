package com.gradleware.tooling.toolingmodel.repository.internal

import com.google.common.collect.ImmutableList
import com.google.common.eventbus.EventBus
import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.ToolingModelToolingClientSpecification
import com.gradleware.tooling.toolingclient.GradleDistribution
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
    assert firstLookUp != null
    assert firstLookUp.is(secondLookUp)

    when:
    def thirdLookUp = repository.fetchBuildInvocations(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)
    def fourthLookUp = repository.fetchBuildInvocations(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

    then:
    thirdLookUp != null
    !thirdLookUp.is(fourthLookUp)
    thirdLookUp.asMap()[':'].projectTasks.size() == fourthLookUp.asMap()[':'].projectTasks.size()
    thirdLookUp.asMap()[':'].taskSelectors.size() == fourthLookUp.asMap()[':'].taskSelectors.size()
  }

//  def "fetchGradleProjectWithBuildInvocationsAndWait_FromCacheOnly_CacheNotPopulated"() {
//    when:
//    def lookUp = repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY)
//
//    then:
//    lookUp != null
//    lookUp.first == null
//    lookUp.second == null
//  }

//  def "fetchGradleProjectWithBuildInvocationsAndWait_FromCacheOnly_CachePopulated"() {
//    when:
//    def gradleProjectLookUp = repository.fetchGradleProjectAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//    def buildInvocationsLookUp = repository.fetchBuildInvocations(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//
//    def compositeLookup = repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY)
//
//    then:
//    compositeLookup != null
//    compositeLookup.first.is(gradleProjectLookUp)
//    compositeLookup.second.is(buildInvocationsLookUp)
//  }

//  def "fetchGradleProjectWithBuildInvocationsAndWait_LoadIfNotCached_CacheNotPopulated"() {
//    when:
//    def compositeLookup = repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//
//    def gradleProjectLookUp = repository.fetchGradleProjectAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//    def buildInvocationsLookUp = repository.fetchBuildInvocations(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//
//    then:
//    compositeLookup != null
//    compositeLookup.first.is(gradleProjectLookUp)
//    compositeLookup.second.is(buildInvocationsLookUp)
//  }

//  def "fetchGradleProjectWithBuildInvocationsAndWait_LoadIfNotCached_CachePopulated"() {
//    when:
//    def gradleProjectLookUp = repository.fetchGradleProjectAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//    def buildInvocationsLookUp = repository.fetchBuildInvocations(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//
//    def compositeLookup = repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//
//    then:
//    compositeLookup != null
//    compositeLookup.first.is(gradleProjectLookUp)
//    compositeLookup.second.is(buildInvocationsLookUp)
//  }

//  def "fetchGradleProjectWithBuildInvocationsAndWait_LoadIfNotCached_RepeatedRead"() {
//    when:
//    def firstLookUp = repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//    def secondLookUp = repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//
//    then:
//    firstLookUp != null
//    !firstLookUp.is(secondLookUp)
//    firstLookUp.first.is(secondLookUp.first)
//    firstLookUp.second.is(secondLookUp.second)
//  }

//  def "fetchGradleProjectWithBuildInvocationsAndWait_ForceReload_CacheNotPopulated"() {
//    when:
//    def compositeLookup = repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)
//
//    def gradleProjectLookUp = repository.fetchGradleProjectAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//    def buildInvocationsLookUp = repository.fetchBuildInvocations(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//
//    then:
//    compositeLookup != null
//    compositeLookup.first.is(gradleProjectLookUp)
//    compositeLookup.second.is(buildInvocationsLookUp)
//  }

//  def "fetchGradleProjectWithBuildInvocationsAndWait_ForceReload_CachePopulated"() {
//    when:
//    def gradleProjectLookUp = repository.fetchGradleProjectAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//    def buildInvocationsLookUp = repository.fetchBuildInvocations(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//
//    def compositeLookup = repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)
//
//    then:
//    compositeLookup != null
//    !compositeLookup.first.is(gradleProjectLookUp)
//    !compositeLookup.second.is(buildInvocationsLookUp)
//  }

//  def "fetchGradleProjectWithBuildInvocationsAndWait_ForceReload_RepeatedRead"() {
//    when:
//    def firstLookUp = repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)
//    def secondLookUp = repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)
//
//    then:
//    firstLookUp != null
//    !firstLookUp.is(secondLookUp)
//    !firstLookUp.first.is(secondLookUp.first)
//    !firstLookUp.second.is(secondLookUp.second)
//  }

}
