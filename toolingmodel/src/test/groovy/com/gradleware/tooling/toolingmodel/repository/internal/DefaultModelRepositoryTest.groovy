package com.gradleware.tooling.toolingmodel.repository.internal

import com.google.common.base.Predicate
import com.google.common.collect.ImmutableList
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.DataValueFormatter
import com.gradleware.tooling.spock.ToolingModelToolingClientSpecification
import com.gradleware.tooling.spock.VerboseUnroll
import com.gradleware.tooling.testing.GradleVersionExtractor
import com.gradleware.tooling.testing.GradleVersionParameterization
import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment
import com.gradleware.tooling.toolingmodel.OmniBuildInvocationsContainer
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild
import com.gradleware.tooling.toolingmodel.OmniGradleBuild
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure
import com.gradleware.tooling.toolingmodel.OmniGradleProject
import com.gradleware.tooling.toolingmodel.repository.BuildEnvironmentUpdateEvent
import com.gradleware.tooling.toolingmodel.repository.BuildInvocationsUpdateEvent
import com.gradleware.tooling.toolingmodel.repository.EclipseGradleBuildUpdateEvent
import com.gradleware.tooling.toolingmodel.repository.Environment
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes
import com.gradleware.tooling.toolingmodel.repository.GradleBuildStructureUpdateEvent
import com.gradleware.tooling.toolingmodel.repository.GradleBuildUpdateEvent
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes
import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProgressListener
import org.gradle.util.GradleVersion
import org.junit.Rule

import java.util.concurrent.atomic.AtomicReference

@VerboseUnroll(formatter = GradleDistributionFormatter.class)
class DefaultModelRepositoryTest extends ToolingModelToolingClientSpecification {

  private static final List<String> IMPLICIT_TASKS = ['init', 'wrapper', 'help', 'projects', 'tasks', 'properties', 'components', 'dependencies', 'dependencyInsight']

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

  def "fetchBuildEnvironment - send event after cache update"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

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
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

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

  def "fetchGradleBuildStructure - when exception is thrown"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildStructure.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

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
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

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

  def "fetchGradleBuild - when exception is thrown"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildFile.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

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

  def "fetchEclipseGradleBuild - send event after cache update"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

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
    eclipseGradleBuild != null
    eclipseGradleBuild.rootEclipseProject != null
    eclipseGradleBuild.rootEclipseProject.name == 'my root project'
    eclipseGradleBuild.rootEclipseProject.description == 'a sample root project'
    eclipseGradleBuild.rootEclipseProject.path == ':'
    eclipseGradleBuild.rootEclipseProject.projectDirectory.absolutePath == directoryProvider.testDirectory.absolutePath
    eclipseGradleBuild.rootEclipseProject.parent == null
    eclipseGradleBuild.rootEclipseProject.children.size() == 2
    eclipseGradleBuild.rootEclipseProject.children*.name == ['sub1', 'sub2']
    eclipseGradleBuild.rootEclipseProject.children*.description == ['sub project 1', 'sub project 2']
    eclipseGradleBuild.rootEclipseProject.children*.path == [':sub1', ':sub2']
    eclipseGradleBuild.rootEclipseProject.children*.parent == [eclipseGradleBuild.rootEclipseProject, eclipseGradleBuild.rootEclipseProject]
    eclipseGradleBuild.rootEclipseProject.all.size() == 4
    eclipseGradleBuild.rootEclipseProject.all*.name == ['my root project', 'sub1', 'sub2', 'subSub1']

    def projectSub1 = eclipseGradleBuild.rootProject.tryFind({ OmniGradleProject input ->
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

    def projectSub2 = eclipseGradleBuild.rootProject.tryFind({ OmniGradleProject input ->
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
    event.eclipseGradleBuild == eclipseGradleBuild

    def model = modelInRepository.get()
    model == eclipseGradleBuild

    where:
    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  def "fetchEclipseGradleBuild - when exception is thrown"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildFile.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

    AtomicReference<EclipseGradleBuildUpdateEvent> publishedEvent = new AtomicReference<>();
    repository.register(new Object() {

      @SuppressWarnings("GroovyUnusedDeclaration")
      @Subscribe
      public void listen(EclipseGradleBuildUpdateEvent event) {
        publishedEvent.set(event)
      }
    })

    when:
    repository.fetchEclipseGradleBuild(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

    then:
    thrown(GradleConnectionException)

    publishedEvent.get() == null

    where:
    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.0")
  }

  def "fetchBuildInvocations - send event after cache update"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

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
    def rootProjectExplicitTasks = buildInvocations.get(':').get().projectTasks.findAll { !IMPLICIT_TASKS.contains(it.name) }
    rootProjectExplicitTasks.size() == 1

    def projectSub1 = buildInvocations.get(':sub1').get()
    def sub1ExplicitTasks = projectSub1.projectTasks.findAll { !IMPLICIT_TASKS.contains(it.name) }
    sub1ExplicitTasks.size() == 2

    def myFirstTaskOfSub1 = sub1ExplicitTasks[0]
    myFirstTaskOfSub1.name == 'myFirstTaskOfSub1'
    myFirstTaskOfSub1.description == '1st task of sub1'
    myFirstTaskOfSub1.path == ':sub1:myFirstTaskOfSub1'
    myFirstTaskOfSub1.isPublic()

    def mySecondTaskOfSub1 = sub1ExplicitTasks[1]
    mySecondTaskOfSub1.name == 'mySecondTaskOfSub1'
    mySecondTaskOfSub1.description == '2nd task of sub1'
    mySecondTaskOfSub1.path == ':sub1:mySecondTaskOfSub1'
    mySecondTaskOfSub1.isPublic() == !higherOrEqual("2.1", distribution) // all versions < 2.1 default to 'true'

    def projectSub2 = buildInvocations.get(':sub2').get()
    def sub2ExplicitTaskSelectors = projectSub2.taskSelectors.findAll { !IMPLICIT_TASKS.contains(it.name) }
    sub2ExplicitTaskSelectors.size() == 5

    def myTaskSelector = sub2ExplicitTaskSelectors.find { it.name == 'myTask' }
    myTaskSelector.name == 'myTask'
    myTaskSelector.description == higherOrEqual("2.3", distribution)? 'another task of sub2' : 'sub2:myTask task selector'
    myTaskSelector.isPublic()
//    myTaskSelector.selectedTaskPaths as List == [':sub2:myTask', ':sub2:subSub1:myTask']

    def event = publishedEvent.get()
    event != null
    event.buildInvocations == buildInvocations

    def model = modelInRepository.get()
    model == buildInvocations

    where:
    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.12") // todo support older versions than 1.12
  }

  def "fetchBuildInvocations - when exception is thrown"(GradleDistribution distribution, Environment environment) {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildFile.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

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

//  def "fetchGradleProjectWithBuildInvocationsAndWait"() {
//    given:
//    AtomicReference<GradleProjectUpdateEvent> publishedGradleProjectEvent = new AtomicReference<>();
//    AtomicReference<BuildInvocationsUpdateEvent> publishedBuildInvocationsEvent = new AtomicReference<>();
//    repository.register(new Object() {
//
//      @Subscribe
//      public void listen1(GradleProjectUpdateEvent event) {
//        publishedGradleProjectEvent.set(event)
//      }
//
//      @Subscribe
//      public void listen2(BuildInvocationsUpdateEvent event) {
//        publishedBuildInvocationsEvent.set(event)
//      }
//    })
//
//    when:
//    repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//
//    then:
//    def gradleProjectEvent = publishedGradleProjectEvent.get()
//    gradleProjectEvent != null
//    publishedGradleProjectEvent.get().gradleProject != null
//    publishedGradleProjectEvent.get().gradleProject.path == ":"
//
//    def buildInvocationsEvent = publishedBuildInvocationsEvent.get()
//    buildInvocationsEvent != null
//    publishedBuildInvocationsEvent.get().buildInvocations.asMap()[':'].tasks.size() > 0
//    publishedBuildInvocationsEvent.get().buildInvocations.asMap()[':'].taskSelectors.size() > 0
//  }

//  def "fetchGradleProjectWithBuildInvocationsAndWaitWhenExceptionIsThrown"() {
//    given:
//    def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildFile.testDirectory, null, GradleDistribution.fromBuild(), null, ImmutableList.of(), ImmutableList.of())
//    transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
//    def repository = new DefaultModelRepository(fixedRequestAttributes, new EventBus(), toolingClient)
//
//    AtomicReference<GradleProjectUpdateEvent> publishedGradleProjectEvent = new AtomicReference<>();
//    AtomicReference<BuildInvocationsUpdateEvent> publishedBuildInvocationsEvent = new AtomicReference<>();
//    repository.register(new Object() {
//
//      @Subscribe
//      public void listen1(GradleProjectUpdateEvent event) {
//        publishedGradleProjectEvent.set(event)
//      }
//
//      @Subscribe
//      public void listen2(BuildInvocationsUpdateEvent event) {
//        publishedBuildInvocationsEvent.set(event)
//      }
//    })
//
//    when:
//    repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
//
//    then:
//    thrown(GradleConnectionException)
//
//    publishedGradleProjectEvent.get() == null
//    publishedBuildInvocationsEvent.get() == null
//  }

//  def "fetchBuildInvocations"(GradleDistribution distribution, Environment environment) {
//    given:
//    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
//    def targetRepository = new DefaultModelRepository(fixedRequestAttributes, new EventBus(), toolingClient)
//    def repository = new ContextAwareModelRepository(targetRepository, environment)
//
//    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
//
//    when:
//    def buildInvocations = repository.fetchBuildInvocations(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)
//
//    then:
//    buildInvocations != null
//    buildInvocations.asMap() != null
//    buildInvocations.asMap().get(':').tasks.size() == getImplicitlyAddedBuildInvocationsTasksCount(distribution, environment) + getImplicitlyAddedGradleProjectTasksCount(distribution) + 1
//    buildInvocations.asMap().get(':').taskSelectors.size() == getImplicitlyAddedBuildInvocationsTaskSelectorsCount(distribution, environment) + getImplicitlyAddedGradleProjectTaskSelectorsCount(distribution) + 1
//
//    where:
//    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.0")
//  }

//  def "fetchGradleProjectWithBuildInvocations"(GradleDistribution distribution, Environment environment) {
//    given:
//    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
//    def targetRepository = new DefaultModelRepository(fixedRequestAttributes, new EventBus(), toolingClient)
//    def repository = new ContextAwareModelRepository(targetRepository, environment)
//
//    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
//
//    when:
//    Pair<GradleProject, BuildInvocationsContainer> gradleProjectAndBuildInvocations = repository.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)
//
//    then:
//    gradleProjectAndBuildInvocations != null
//    gradleProjectAndBuildInvocations.first != null
//    gradleProjectAndBuildInvocations.first.name == 'TestProject'
//    gradleProjectAndBuildInvocations.first.description == 'my project'
//    gradleProjectAndBuildInvocations.first.path == ':'
//    gradleProjectAndBuildInvocations.first.tasks.size() == getImplicitlyAddedGradleProjectTasksCount(distribution) + 1
//    gradleProjectAndBuildInvocations.first.parent == null
//    gradleProjectAndBuildInvocations.first.children.size() == 0
//    gradleProjectAndBuildInvocations.second.asMap() != null
//    gradleProjectAndBuildInvocations.second.asMap().get(':').tasks.size() == getImplicitlyAddedBuildInvocationsTasksCount(distribution, environment) + getImplicitlyAddedGradleProjectTasksCount(distribution) + 1
//    gradleProjectAndBuildInvocations.second.asMap().get(':').taskSelectors.size() == getImplicitlyAddedBuildInvocationsTaskSelectorsCount(distribution, environment) + getImplicitlyAddedGradleProjectTaskSelectorsCount(distribution) + 1
//
//    where:
//    [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.0")
//  }

  def "registerUnregister - no more events are sent to receiver once he is unregistered"() {
    given:
    def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, GradleDistribution.fromBuild(), null, ImmutableList.of(), ImmutableList.of())
    def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    def repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

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

  private static int getImplicitlyAddedGradleProjectTasksCount(GradleDistribution distribution) {
    // tasks implicitly provided by GradleProject#getTasks(): setupBuild
    def version = GradleVersion.version(extractVersion(distribution))
    version.compareTo(GradleVersion.version("1.6")) == 0 ? 1 : 0
  }

//  private static int getImplicitlyAddedBuildInvocationsTasksCount(GradleDistribution distribution, Environment environment) {
//    // tasks implicitly provided by BuildInvocations#getTasks(): init, wrapper, help, properties, projects, tasks, dependencies, dependencyInsight, components
//    def version = GradleVersion.version(extractVersion(distribution))
//    version.baseVersion.compareTo(GradleVersion.version("2.3")) >= 0 || version.baseVersion.compareTo(GradleVersion.version("2.1")) >= 0 && environment != Environment.ECLIPSE ? 9 : version.baseVersion.compareTo(GradleVersion.version("2.0")) >= 0 && environment != Environment.ECLIPSE ? 8 : 0
//  }
//
//  private static int getImplicitlyAddedBuildInvocationsTaskSelectorsCount(GradleDistribution distribution, Environment environment) {
//    // tasks implicitly provided by BuildInvocations#getTaskSelectors():
//    def version = GradleVersion.version(extractVersion(distribution));
//    version.baseVersion.compareTo(GradleVersion.version("2.3")) >= 0 || version.baseVersion.compareTo(GradleVersion.version("2.1")) >= 0 && environment != Environment.ECLIPSE ? 9 : 0
//  }
//  private static int getImplicitlyAddedGradleProjectTaskSelectorsCount(GradleDistribution distribution) {
//    // tasks implicitly provided by GradleProject#getTasks(): setupBuild
//    def version = GradleVersion.version(extractVersion(distribution))
//    version.compareTo(GradleVersion.version("1.6")) == 0 ? 1 : 0
//  }

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
