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
import com.gradleware.tooling.toolingmodel.OmniEclipseWorkspace;
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
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes
import org.gradle.api.specs.Spec
import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProgressListener
import org.gradle.util.GradleVersion
import org.junit.Rule

import java.util.concurrent.atomic.AtomicReference

@VerboseUnroll(formatter = GradleDistributionFormatter.class)
class SingleRootProjectCompositeModelRepositoryTest extends ToolingModelToolingClientSpecification {

    @Rule
    TestDirectoryProvider projectA = new TestDirectoryProvider("projectA");
    @Rule
    TestDirectoryProvider projectB = new TestDirectoryProvider("projectB");

    @Rule
    TestDirectoryProvider projectWithErronousBuildFile = new TestDirectoryProvider("erroneous-build-file");


    def setup() {
        projectA.createFile('settings.gradle') << '''
       rootProject.name = 'my root project'
       include 'sub1'
       include 'sub2'
       include 'sub2:subSub1'
    '''
        projectA.createFile('build.gradle') << '''
       description = 'a sample root project'
       task myTask {}
    '''

        projectA.createDir('sub1')
        projectA.createFile('sub1', 'build.gradle') << '''
       description = 'sub project 1'
       task myFirstTaskOfSub1 {
         description = '1st task of sub1'
         group = 'build'
       }
       task mySecondTaskOfSub1 {
         description = '2nd task of sub1'
       }
    '''

        projectA.createDir('sub2')
        projectA.createFile('sub2', 'build.gradle') << '''
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

        projectA.createDir('sub2', 'subSub1')
        projectA.createFile('sub2', 'subSub1', 'build.gradle') << '''
       description = 'subSub project 1 of sub project 2'
       task myFirstTaskOfSub2subSub1{
         description = '1st task of sub2:subSub1'
       }
       task mySecondTaskOfSub2subSub1{
         description = '2nd task of sub2:subSub1'
       }
       task myTask {}
    '''

        projectB.createFile('settings.gradle') << '''
      rootProject.name = 'root project of multi-project build'
      include 'api'
      include 'impl'
    '''

        projectB.createDir('api')
        projectB.createFile('api', 'build.gradle') << '''
        apply plugin: 'java'
        repositories {
            mavenCentral()
            dependencies {
                compile 'com.google.guava:guava:18.0'
            }
        }
    '''

        projectB.createDir('impl')
        projectB.createFile('impl', 'build.gradle') << '''
        apply plugin: 'java'
        dependencies {
            compile project(':api')
        }
    '''

        projectWithErronousBuildFile.createFile('settings.gradle')
        projectWithErronousBuildFile.createFile('build.gradle') << 'task myTask {'
    }

    def "a cache update is sent after fetching the workspace"(GradleDistribution distribution, Environment environment) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(projectA.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        def DefaultCompositeModelRepository repository = new DefaultCompositeModelRepository([fixedRequestAttributes], toolingClient, new EventBus())

        AtomicReference<EclipseWorkspaceUpdateEvent> publishedEvent = new AtomicReference<>();
        AtomicReference<OmniEclipseWorkspace> modelInRepository = new AtomicReference<>();
        repository.register(new Object() {

                    @SuppressWarnings("GroovyUnusedDeclaration")
                    @Subscribe
                    public void listen(EclipseWorkspaceUpdateEvent event) {
                        publishedEvent.set(event)
                        modelInRepository.set(repository.fetchEclipseWorkspace(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY))
                    }
                })

        when:
        OmniEclipseWorkspace eclipseWorkspace = repository.fetchEclipseWorkspace(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

        then:
        eclipseWorkspace != null
        eclipseWorkspace.openEclipseProjects.size() == 4
        def OmniEclipseProject rootProject = eclipseWorkspace.tryFind {project -> project.parent == null}.get()

        rootProject.name == 'my root project'
        rootProject.description == 'a sample root project'
        rootProject.path == Path.from(':')
        rootProject.projectDirectory.absolutePath == projectA.testDirectory.absolutePath
        rootProject.parent == null
        rootProject.children.size() == 2
        rootProject.children*.name == ['sub1', 'sub2']
        rootProject.children*.description == [
            'sub project 1',
            'sub project 2'
        ]
        rootProject.children*.path.path == [':sub1', ':sub2']
        rootProject.children*.parent == [rootProject, rootProject]
        rootProject.all.size() == 4
        rootProject.all*.name == [
            'my root project',
            'sub1',
            'sub2',
            'subSub1'
        ]
        //TODO workspace project order should be stable
        //rootProject.all*.name == eclipseWorkspace.openEclipseProjects*.name

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
        myTaskSelector.selectedTaskPaths*.path as List == [
            ':sub2:myTask',
            ':sub2:subSub1:myTask'
        ]

        def event = publishedEvent.get()
        event != null
        event.eclipseWorkspace == eclipseWorkspace

        def model = modelInRepository.get()
        model == eclipseWorkspace

        where:
        [distribution, environment]<< runInAllEnvironmentsForGradleTargetVersions(">=2.3")
    }

    @SuppressWarnings("GroovyTrivialConditional")
    def "projects in the workspace have correct sources and project/external dependencies "(GradleDistribution distribution, Environment environment) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(projectB.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        def repository = new DefaultCompositeModelRepository([fixedRequestAttributes], toolingClient, new EventBus(), environment)

        def apiProjectDir = new TestFile(this.projectB.testDirectory, 'api')
        apiProjectDir.create {
            src {
                main {
                    java {}
                    resources {}
                }
                test { java {} }
            }
        }

        when:
        OmniEclipseWorkspace eclipseWorkspace = repository.fetchEclipseWorkspace(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

        then:
        eclipseWorkspace != null
        def OmniEclipseProject rootProject = eclipseWorkspace.tryFind {project -> project.parent == null}.get()
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
        apiProjectDependency.targetProjectPath.path == ':api'
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
            !guavaDependency.gradleModuleVersion.isPresent()
        }
        rootProject.tryFind({ it.name == 'impl' } as Spec).get().externalDependencies == []

        where:
        [distribution, environment]<< runInAllEnvironmentsForGradleTargetVersions(">=1.0")
    }

    def "projects in the workspace have correct build commands and natures"(GradleDistribution distribution, Environment environment) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(projectB.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        def repository = new DefaultCompositeModelRepository([fixedRequestAttributes], toolingClient, new EventBus(), environment)

        new TestFile(this.projectB.testDirectory, 'api/build.gradle') << """
          apply plugin: 'eclipse'
          eclipse {
            project {
              natures = ['customNature']
              buildCommands = [new org.gradle.plugins.ide.eclipse.model.BuildCommand('buildCommand', [argKey: 'argValue'])]
            }
          }
    """

        when:
        OmniEclipseWorkspace eclipseWorkspace = repository.fetchEclipseWorkspace(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

        then:
        def OmniEclipseProject rootProject = eclipseWorkspace.tryFind {project -> project.parent == null}.get()
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
        [distribution, environment]<< runInAllEnvironmentsForGradleTargetVersions(">=1.0")
    }

    def "projects in the workspace have no source version settings for non-JVM projects"(GradleDistribution distribution, Environment environment) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(projectA.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        def repository = new DefaultCompositeModelRepository([fixedRequestAttributes], toolingClient, new EventBus(), environment)

        when:
        OmniEclipseWorkspace eclipseWorkspace = repository.fetchEclipseWorkspace(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

        then:
        def OmniEclipseProject rootProject = eclipseWorkspace.tryFind {project -> project.parent == null}.get()

        def eclipseProject = rootProject.tryFind({ it.name == 'sub1' } as Spec).get()
        def sourceCompatibility = eclipseProject.javaSourceSettings
        if (higherOrEqual('2.10', distribution)) {
            assert sourceCompatibility.isPresent()
            assert sourceCompatibility.get() == null
        } else {
            assert !sourceCompatibility.isPresent()
        }

        where:
        [distribution, environment]<< runInAllEnvironmentsForGradleTargetVersions(">=1.0")
    }

    def "projects in the workspace have correct source version settings for JVM projects"(GradleDistribution distribution, Environment environment) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(projectA.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        def repository = new DefaultCompositeModelRepository([fixedRequestAttributes], toolingClient, new EventBus(), environment)
        new TestFile(this.projectA.testDirectory, 'sub1/build.gradle') << """
          apply plugin: 'java'
          apply plugin: 'eclipse'
          eclipse {
            project {
              sourceCompatibility = 1.2
            }
          }
        """

        when:
        OmniEclipseWorkspace eclipseWorkspace = repository.fetchEclipseWorkspace(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

        then:
        def OmniEclipseProject rootProject = eclipseWorkspace.tryFind {project -> project.parent == null}.get()
        def eclipseProject = rootProject.tryFind({ it.name == 'sub1' } as Spec).get()
        def sourceCompatibility = eclipseProject.javaSourceSettings
        if (higherOrEqual('2.10', distribution)) {
            assert sourceCompatibility.isPresent()
            assert sourceCompatibility.get().sourceLanguageLevel.name == '1.2'
        } else {
            assert !sourceCompatibility.isPresent()
        }

        where:
        [distribution, environment]<< runInAllEnvironmentsForGradleTargetVersions(">=1.0")
    }

    def "when fetching the workspace fails, there is no update event"(GradleDistribution distribution, Environment environment) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(projectWithErronousBuildFile.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        def repository = new DefaultCompositeModelRepository([fixedRequestAttributes], toolingClient, new EventBus(), environment)

        AtomicReference<EclipseWorkspaceUpdateEvent> publishedEvent = new AtomicReference<>();
        repository.register(new Object() {

                    @SuppressWarnings("GroovyUnusedDeclaration")
                    @Subscribe
                    public void listen(EclipseWorkspaceUpdateEvent event) {
                        publishedEvent.set(event)
                    }
                })

        when:
        repository.fetchEclipseWorkspace(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

        then:
        thrown(GradleConnectionException)

        publishedEvent.get() == null

        where:
        [distribution, environment]<< runInAllEnvironmentsForGradleTargetVersions(">=1.0")
    }

    def "no more events are sent to receiver once he is unregistered"() {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(projectA.testDirectory, null, GradleDistribution.fromBuild(), null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        def repository = new DefaultCompositeModelRepository([fixedRequestAttributes], toolingClient, new EventBus())

        AtomicReference<EclipseWorkspaceUpdateEvent> publishedEvent = new AtomicReference<>();
        def listener = new Object() {

                    @SuppressWarnings("GroovyUnusedDeclaration")
                    @Subscribe
                    public void listen(EclipseWorkspaceUpdateEvent event) {
                        publishedEvent.set(event)
                    }
                }

        when:
        publishedEvent.set(null)
        repository.register(listener)
        repository.fetchEclipseWorkspace(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

        then:
        publishedEvent.get() != null

        when:
        publishedEvent.set(null)
        repository.unregister(listener)
        repository.fetchEclipseWorkspace(transientRequestAttributes, FetchStrategy.FORCE_RELOAD)

        then:
        publishedEvent.get() == null
    }

    private static boolean higherOrEqual(String minVersion, GradleDistribution distribution) {
        def gradleVersion = GradleVersion.version(extractVersion(distribution))
        gradleVersion.baseVersion.compareTo(GradleVersion.version(minVersion)) >= 0
    }

    @SuppressWarnings([
        "GroovyAssignabilityCheck",
        "GroovyAccessibility"
    ])
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

}
