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
import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.ToolingModelToolingClientSpecification
import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes
import com.gradleware.tooling.toolingmodel.repository.ModelRepository
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProgressListener
import org.junit.Rule

class DefaultModelRepositoryCacheTest extends ToolingModelToolingClientSpecification {

  @Rule
  TestDirectoryProvider directoryProvider = new TestDirectoryProvider();

  FixedRequestAttributes fixedRequestAttributes
  TransientRequestAttributes transientRequestAttributes
  ModelRepository repository

  def setup() {
    // Gradle projects for testing
    directoryProvider.createFile('settings.gradle')
    directoryProvider.createFile('build.gradle') << 'task myTask {}'

    // request attributes and model repository for testing
    fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, GradleDistribution.fromBuild(), null, ImmutableList.of(), ImmutableList.of())
    transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
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

  def "fetchGradleBuild"() {
    when:
    def lookUp = repository.fetchGradleProjects(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY)

    then:
    lookUp == null

    when:
    def firstLookUp = repository.fetchGradleProjects(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
    def secondLookUp = repository.fetchGradleProjects(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    firstLookUp != null
    firstLookUp.is(secondLookUp)

    when:
    def thirdLookUp = repository.fetchGradleProjects(transientRequestAttributes, FetchStrategy.FORCE_RELOAD) as List
    def fourthLookUp = repository.fetchGradleProjects(transientRequestAttributes, FetchStrategy.FORCE_RELOAD) as List

    then:
    thirdLookUp != null
    !thirdLookUp.is(fourthLookUp)
    thirdLookUp[0].path == fourthLookUp[0].path
    thirdLookUp[0].all.size() == fourthLookUp[0].all.size()
  }

  def "fetchEclipseGradleBuild"() {
    when:
    def lookUp = repository.fetchEclipseGradleProjects(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY)

    then:
    lookUp == null

    when:
    def firstLookUp = repository.fetchEclipseGradleProjects(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
    def secondLookUp = repository.fetchEclipseGradleProjects(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    firstLookUp != null
    firstLookUp.is(secondLookUp)

    when:
    def thirdLookUp = repository.fetchEclipseGradleProjects(transientRequestAttributes, FetchStrategy.FORCE_RELOAD) as List
    def fourthLookUp = repository.fetchEclipseGradleProjects(transientRequestAttributes, FetchStrategy.FORCE_RELOAD) as List

    then:
    thirdLookUp != null
    !thirdLookUp.is(fourthLookUp)
    thirdLookUp[0].gradleProject.path == fourthLookUp[0].gradleProject.path
    thirdLookUp[0].gradleProject.all.size() == fourthLookUp[0].gradleProject.all.size()
  }

}
