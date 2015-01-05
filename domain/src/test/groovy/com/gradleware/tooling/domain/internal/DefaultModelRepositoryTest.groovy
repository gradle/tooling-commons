package com.gradleware.tooling.domain.internal

import com.google.common.collect.ImmutableList
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.gradleware.tooling.domain.BuildEnvironmentUpdateEvent
import com.gradleware.tooling.domain.BuildInvocationsUpdateEvent
import com.gradleware.tooling.domain.EclipseProjectUpdateEvent
import com.gradleware.tooling.domain.FetchStrategy
import com.gradleware.tooling.domain.FixedRequestAttributes
import com.gradleware.tooling.domain.GradleBuildUpdateEvent
import com.gradleware.tooling.domain.GradleProjectUpdateEvent
import com.gradleware.tooling.domain.TransientRequestAttributes
import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.ToolingClientSpecification
import com.gradleware.tooling.toolingapi.GradleDistribution
import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProgressListener
import org.junit.Rule

import java.util.concurrent.atomic.AtomicReference

class DefaultModelRepositoryTest extends ToolingClientSpecification {

  @Rule
  TestDirectoryProvider directoryProvider = new TestDirectoryProvider();

  @Rule
  TestDirectoryProvider directoryProviderErroneousBuildStructure = new TestDirectoryProvider();

  @Rule
  TestDirectoryProvider directoryProviderErroneousBuildFile = new TestDirectoryProvider();

  FixedRequestAttributes fixedRequestAttributes
  TransientRequestAttributes transientRequestAttributes
  DefaultModelRepository repository

  def setup() {
    // Gradle projects for testing
    directoryProvider.createFile('settings.gradle')
    directoryProvider.createFile('build.gradle') << 'task myTask {}'

    directoryProviderErroneousBuildStructure.createFile('settings.gradle') << 'include foo'

    directoryProviderErroneousBuildFile.createFile('settings.gradle')
    directoryProviderErroneousBuildFile.createFile('build.gradle') << 'task myTask {'

    // request attributes and model repository for testing
    fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, GradleDistribution.fromBuild(), null, ImmutableList.of(), ImmutableList.of())
    transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    repository = new DefaultModelRepository(fixedRequestAttributes, new EventBus(), toolingClient)
  }

  def "fetchBuildEnvironmentAndWait"() {
    given:
    AtomicReference<BuildEnvironmentUpdateEvent> publishedEvent = new AtomicReference<>();
    repository.register(new Object() {

      @Subscribe
      public void listen(BuildEnvironmentUpdateEvent event) {
        publishedEvent.set(event)
      }
    })

    when:
    repository.fetchBuildEnvironmentAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    def event = publishedEvent.get()
    event != null
    publishedEvent.get().buildEnvironment.gradle != null
    publishedEvent.get().buildEnvironment.java != null
  }

  def "fetchGradleBuildAndWait"() {
    given:
    AtomicReference<GradleBuildUpdateEvent> publishedEvent = new AtomicReference<>();
    repository.register(new Object() {

      @Subscribe
      public void listen(GradleBuildUpdateEvent event) {
        publishedEvent.set(event)
      }
    })

    when:
    repository.fetchGradleBuildAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    def event = publishedEvent.get()
    event != null
    publishedEvent.get().gradleBuild != null
    publishedEvent.get().gradleBuild.rootProject != null
  }

  def "fetchGradleBuildAndWaitWhenExceptionIsThrown"() {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildStructure.testDirectory, null, GradleDistribution.fromBuild(), null, ImmutableList.of(), ImmutableList.of())
    transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultModelRepository(fixedRequestAttributes, new EventBus(), toolingClient)

    AtomicReference<BuildInvocationsUpdateEvent> publishedEvent = new AtomicReference<>();
    repository.register(new Object() {

      @Subscribe
      public void listen(BuildInvocationsUpdateEvent event) {
        publishedEvent.set(event)
      }
    })

    when:
    repository.fetchGradleBuildAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    thrown(GradleConnectionException)

    publishedEvent.get() == null
  }

  def "fetchEclipseProjectAndWait"() {
    given:
    AtomicReference<EclipseProjectUpdateEvent> publishedEvent = new AtomicReference<>();
    repository.register(new Object() {

      @Subscribe
      public void listen(EclipseProjectUpdateEvent event) {
        publishedEvent.set(event)
      }
    })

    when:
    repository.fetchEclipseProjectAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    def event = publishedEvent.get()
    event != null
    publishedEvent.get().eclipseProject != null
    publishedEvent.get().eclipseProject.gradleProject.path == ":"
  }

  def "fetchEclipseProjectAndWaitWhenExceptionIsThrown"() {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildFile.testDirectory, null, GradleDistribution.fromBuild(), null, ImmutableList.of(), ImmutableList.of())
    transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultModelRepository(fixedRequestAttributes, new EventBus(), toolingClient)

    AtomicReference<EclipseProjectUpdateEvent> publishedEvent = new AtomicReference<>();
    repository.register(new Object() {

      @Subscribe
      public void listen(EclipseProjectUpdateEvent event) {
        publishedEvent.set(event)
      }
    })

    when:
    repository.fetchEclipseProjectAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    thrown(GradleConnectionException)

    publishedEvent.get() == null
  }

  def "fetchGradleProjectAndWait"() {
    given:
    AtomicReference<GradleProjectUpdateEvent> publishedEvent = new AtomicReference<>();
    repository.register(new Object() {

      @Subscribe
      public void listen(GradleProjectUpdateEvent event) {
        publishedEvent.set(event)
      }
    })

    when:
    repository.fetchGradleProjectAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    def event = publishedEvent.get()
    event != null
    publishedEvent.get().gradleProject != null
    publishedEvent.get().gradleProject.path == ":"
  }

  def "fetchGradleProjectAndWaitWhenExceptionIsThrown"() {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildFile.testDirectory, null, GradleDistribution.fromBuild(), null, ImmutableList.of(), ImmutableList.of())
    transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultModelRepository(fixedRequestAttributes, new EventBus(), toolingClient)

    AtomicReference<BuildInvocationsUpdateEvent> publishedEvent = new AtomicReference<>();
    repository.register(new Object() {

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
  }

  def "fetchBuildInvocationsAndWait"() {
    given:
    AtomicReference<BuildInvocationsUpdateEvent> publishedEvent = new AtomicReference<>();
    repository.register(new Object() {

      @Subscribe
      public void listen(BuildInvocationsUpdateEvent event) {
        publishedEvent.set(event)
      }
    })

    when:
    repository.fetchBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    def event = publishedEvent.get()
    event != null
    publishedEvent.get().buildInvocations.asMap()[':'].tasks.size() > 0
    publishedEvent.get().buildInvocations.asMap()[':'].taskSelectors.size() > 0
  }

  def "fetchBuildInvocationsAndWaitWhenExceptionIsThrown"() {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildFile.testDirectory, null, GradleDistribution.fromBuild(), null, ImmutableList.of(), ImmutableList.of())
    transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultModelRepository(fixedRequestAttributes, new EventBus(), toolingClient)

    AtomicReference<BuildInvocationsUpdateEvent> publishedEvent = new AtomicReference<>();
    repository.register(new Object() {

      @Subscribe
      public void listen(BuildInvocationsUpdateEvent event) {
        publishedEvent.set(event)
      }
    })

    when:
    repository.fetchBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    thrown(GradleConnectionException)

    publishedEvent.get() == null
  }

  def "fetchGradleProjectWithBuildInvocationsAndWait"() {
    given:
    AtomicReference<GradleProjectUpdateEvent> publishedGradleProjectEvent = new AtomicReference<>();
    AtomicReference<BuildInvocationsUpdateEvent> publishedBuildInvocationsEvent = new AtomicReference<>();
    repository.register(new Object() {

      @Subscribe
      public void listen1(GradleProjectUpdateEvent event) {
        publishedGradleProjectEvent.set(event)
      }

      @Subscribe
      public void listen2(BuildInvocationsUpdateEvent event) {
        publishedBuildInvocationsEvent.set(event)
      }
    })

    when:
    repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    def gradleProjectEvent = publishedGradleProjectEvent.get()
    gradleProjectEvent != null
    publishedGradleProjectEvent.get().gradleProject != null
    publishedGradleProjectEvent.get().gradleProject.path == ":"

    def buildInvocationsEvent = publishedBuildInvocationsEvent.get()
    buildInvocationsEvent != null
    publishedBuildInvocationsEvent.get().buildInvocations.asMap()[':'].tasks.size() > 0
    publishedBuildInvocationsEvent.get().buildInvocations.asMap()[':'].taskSelectors.size() > 0
  }

  def "fetchGradleProjectWithBuildInvocationsAndWaitWhenExceptionIsThrown"() {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildFile.testDirectory, null, GradleDistribution.fromBuild(), null, ImmutableList.of(), ImmutableList.of())
    transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultModelRepository(fixedRequestAttributes, new EventBus(), toolingClient)

    AtomicReference<GradleProjectUpdateEvent> publishedGradleProjectEvent = new AtomicReference<>();
    AtomicReference<BuildInvocationsUpdateEvent> publishedBuildInvocationsEvent = new AtomicReference<>();
    repository.register(new Object() {

      @Subscribe
      public void listen1(GradleProjectUpdateEvent event) {
        publishedGradleProjectEvent.set(event)
      }

      @Subscribe
      public void listen2(BuildInvocationsUpdateEvent event) {
        publishedBuildInvocationsEvent.set(event)
      }
    })

    when:
    repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    thrown(GradleConnectionException)

    publishedGradleProjectEvent.get() == null
    publishedBuildInvocationsEvent.get() == null
  }

  def "registerUnregister"() {
    given:
    AtomicReference<BuildInvocationsUpdateEvent> publishedEvent = new AtomicReference<>();
    def listener = new Object() {

      @Subscribe
      public void listen(BuildInvocationsUpdateEvent event) {
        publishedEvent.set(event)
      }
    }

    repository.register(listener)
    repository.unregister(listener)

    when:
    repository.fetchBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    publishedEvent.get() == null
  }

}
