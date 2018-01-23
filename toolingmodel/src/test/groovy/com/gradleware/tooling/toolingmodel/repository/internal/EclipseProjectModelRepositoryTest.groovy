/*
 * Copyright 2016 the original author or authors.
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
import com.gradleware.tooling.junit.TestFile
import com.gradleware.tooling.spock.VerboseUnroll
import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.OmniEclipseProject
import com.gradleware.tooling.toolingmodel.OmniGradleProject
import com.gradleware.tooling.toolingmodel.Path
import com.gradleware.tooling.toolingmodel.repository.*
import org.gradle.api.JavaVersion
import org.gradle.api.specs.Spec
import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProgressListener

import java.util.concurrent.atomic.AtomicReference

@VerboseUnroll(formatter = GradleDistributionFormatter.class)
class EclipseProjectModelRepositoryTest extends ModelRepositorySpec {

    def "projects have correct structure and tasks" (GradleDistribution distribution) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())

        when:
        def rootProject = fetchRootEclipseProject(fixedRequestAttributes, transientRequestAttributes)

        then:
        rootProject != null
        rootProject.name == 'my root project'
        rootProject.description == 'a sample root project'
        rootProject.path == Path.from(':')
        rootProject.projectDirectory.absolutePath == directoryProvider.testDirectory.absolutePath
        rootProject.parent == null
        rootProject.children.size() == 2
        rootProject.children*.name == ['sub1', 'sub2']
        rootProject.children*.description == ['sub project 1', 'sub project 2']
        rootProject.children*.path.path == [':sub1', ':sub2']
        rootProject.children*.parent == [rootProject, rootProject]
        rootProject.all.size() == 4
        rootProject.all*.name == ['my root project', 'sub1', 'sub2', 'subSub1']

        def projectSub1 = rootProject.gradleProject.tryFind({ OmniGradleProject input ->
            return input.path.path == ':sub1'
        } as Spec).get()
        projectSub1.projectTasks.findAll { !ImplicitTasks.ALL.contains(it.name) }.size() == 2

        def myFirstTaskOfSub1 = projectSub1.projectTasks.findAll { !ImplicitTasks.ALL.contains(it.name) }[0]
        myFirstTaskOfSub1.name == 'myFirstTaskOfSub1'
        myFirstTaskOfSub1.description == '1st task of sub1'
        myFirstTaskOfSub1.path.path == ':sub1:myFirstTaskOfSub1'
        myFirstTaskOfSub1.isPublic() == (!higherOrEqual('2.1', distribution) || higherOrEqual('2.3', distribution))
        if (higherOrEqual('2.5', distribution)) {
            assert myFirstTaskOfSub1.group.get() == 'build'
        } else {
            assert !myFirstTaskOfSub1.group.present
        }

        def mySecondTaskOfSub1 = projectSub1.projectTasks.findAll { !ImplicitTasks.ALL.contains(it.name) }[1]
        mySecondTaskOfSub1.name == 'mySecondTaskOfSub1'
        mySecondTaskOfSub1.description == '2nd task of sub1'
        mySecondTaskOfSub1.path.path == ':sub1:mySecondTaskOfSub1'
        mySecondTaskOfSub1.isPublic() == !higherOrEqual('2.1', distribution) // all versions < 2.1 default to 'true'
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
        myTaskSelector.isPublic() == (!higherOrEqual('2.1', distribution) || higherOrEqual('2.3', distribution))
        myTaskSelector.selectedTaskPaths*.path as List == [':sub2:myTask', ':sub2:subSub1:myTask']

        where:
        distribution << gradleDistributionRange(">=1.2")
    }

    def "send event after cache update"(GradleDistribution distribution) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        def repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

        AtomicReference<EclipseProjectUpdateEvent> publishedEvent = new AtomicReference<>();
        AtomicReference<Set<OmniEclipseProject>> modelInRepository = new AtomicReference<>();
        repository.register(new Object() {

            @SuppressWarnings("GroovyUnusedDeclaration")
            @Subscribe
            public void listen(EclipseProjectUpdateEvent event) {
                publishedEvent.set(event)
                modelInRepository.set(repository.fetchEclipseGradleProjects(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY))
            }
        })

        when:
        def eclipseGradleBuild = repository.fetchEclipseGradleProjects(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

        then:
        def model = modelInRepository.get()
        model == eclipseGradleBuild

        where:
        distribution << gradleDistributionRange(">=1.2")
    }

    @SuppressWarnings("GroovyTrivialConditional")
    def "sources and project/external dependencies "(GradleDistribution distribution) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderMultiProjectBuild.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())

        def apiProjectDir = new TestFile(this.directoryProviderMultiProjectBuild.testDirectory, 'api')
        apiProjectDir.create {
            src {
                main {
                    java {}
                    resources {}
                }
                test {
                    java {}
                }
            }
        }

        when:
        def rootProject = fetchRootEclipseProject(fixedRequestAttributes, transientRequestAttributes)

        then:
        rootProject != null
        rootProject.name == 'root project of multi-project build'
        rootProject.sourceDirectories == []
        rootProject.projectDependencies == []
        rootProject.externalDependencies == []

        // verify source directories
        def apiProject = rootProject.tryFind({ it.name == 'api' } as Spec).get()
        def apiSourceDirectories = apiProject.sourceDirectories
        apiSourceDirectories.size() == 3
        apiSourceDirectories[0].path == 'src/main/java'
        apiSourceDirectories[0].directory == apiProjectDir.file('src/main/java')
        apiSourceDirectories[1].path == 'src/main/resources'
        apiSourceDirectories[1].directory == apiProjectDir.file('src/main/resources')
        apiSourceDirectories[2].path == 'src/test/java'
        apiSourceDirectories[2].directory == apiProjectDir.file('src/test/java')
        rootProject.tryFind({ it.name == 'impl' } as Spec).get().sourceDirectories == []

        // verify project dependencies
        apiProject.projectDependencies == []
        def implProjectDependencies = rootProject.tryFind({ it.name == 'impl' } as Spec).get().projectDependencies
        def implExternalDependencies = rootProject.tryFind({ it.name == 'impl' } as Spec).get().externalDependencies
        implProjectDependencies.size() == 1
        def apiProjectDependency = implProjectDependencies[0]
        apiProjectDependency.exported == higherOrEqual('2.5', distribution) ? false : true

        // verify external dependencies
        def apiExternalDependencies = apiProject.externalDependencies
        apiExternalDependencies.size() == 1
        def guavaDependency = apiExternalDependencies[0]
        guavaDependency.file != null
        guavaDependency.source != null
        guavaDependency.javadoc == null
        guavaDependency.exported == higherOrEqual('2.5', distribution) ? false : true

        guavaDependency.gradleModuleVersion.get().group == 'com.google.guava'
        guavaDependency.gradleModuleVersion.get().name == 'guava'
        guavaDependency.gradleModuleVersion.get().version == '18.0'

        if (higherOrEqual('2.5', distribution)) {
            assert implExternalDependencies.size() == 2
            assert implExternalDependencies.find { it.file == guavaDependency.file }
            assert implExternalDependencies.find { it.file.absolutePath.contains('log4j') }
        } else {
            assert implExternalDependencies.size() == 1
            assert implExternalDependencies.find { it.file.absolutePath.contains('log4j') }
        }

        // verify classpath attributes
        if (higherOrEqual('4.4', distribution)) {
            assert apiProjectDependency.classpathAttributes.get() == []
            assert apiProjectDependency.accessRules.get() == []
            assert guavaDependency.classpathAttributes.get().size() == 1
            assert guavaDependency.accessRules.get() == []
        } else if (higherOrEqual('3.0', distribution)) {
            assert apiProjectDependency.classpathAttributes.get() == []
            assert apiProjectDependency.accessRules.get() == []
            assert guavaDependency.classpathAttributes.get() == []
            assert guavaDependency.accessRules.get() == []
        } else if (higherOrEqual('2.14', distribution)) {
            assert apiProjectDependency.classpathAttributes.get() == []
            assert !apiProjectDependency.accessRules.isPresent()
            assert guavaDependency.classpathAttributes.get() == []
            assert !guavaDependency.accessRules.isPresent()
        } else {
            assert !apiProjectDependency.classpathAttributes.isPresent()
            assert !guavaDependency.classpathAttributes.isPresent()
            assert !apiProjectDependency.accessRules.isPresent()
            assert !guavaDependency.accessRules.isPresent()
        }

        where:
        distribution << gradleDistributionRange(">=1.2")
    }

    def "build commands and natures"(GradleDistribution distribution) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderMultiProjectBuild.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())

        new TestFile(this.directoryProviderMultiProjectBuild.testDirectory, 'api/build.gradle') << """
            apply plugin: 'eclipse'
            eclipse {
                project {
                    natures = ['customNature']
                    buildCommands = [new org.gradle.plugins.ide.eclipse.model.BuildCommand('buildCommand', [argKey: 'argValue'])]
                }
            }

        """

        when:
        def rootProject = fetchRootEclipseProject(fixedRequestAttributes, transientRequestAttributes)

        then:
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
        distribution << gradleDistributionRange(">=1.2")
    }

    def "source version settings for non-JVM projects"(GradleDistribution distribution) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())

        when:
        def rootProject = fetchRootEclipseProject(fixedRequestAttributes, transientRequestAttributes)

        then:
        def eclipseProject = rootProject.tryFind({ it.name == 'sub1' } as Spec).get()
        !eclipseProject.javaSourceSettings.isPresent()

        where:
        distribution << gradleDistributionRange(">=1.2")
    }

    def "source version settings for JVM projects"(GradleDistribution distribution) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        new TestFile(this.directoryProvider.testDirectory, 'sub1/build.gradle') << """
            apply plugin: 'java'
            apply plugin: 'eclipse'
            eclipse {
                project {
                    sourceCompatibility = 1.2
                    targetCompatibility = 1.3
                }
            }
        """
        new TestFile(this.directoryProvider.testDirectory, 'sub1/src/main/java').mkdirs()

        when:
        def rootProject = fetchRootEclipseProject(fixedRequestAttributes, transientRequestAttributes)

        then:
        def eclipseProject = rootProject.tryFind({ it.name == 'sub1' } as Spec).get()
        def sourceCompatibility = eclipseProject.javaSourceSettings
        sourceCompatibility.isPresent()
        if (higherOrEqual('2.11', distribution)) {
            assert sourceCompatibility.get().sourceLanguageLevel.name == '1.2'
            assert sourceCompatibility.get().targetBytecodeLevel.name == '1.3'
            assert sourceCompatibility.get().targetRuntime.homeDirectory != null
            assert sourceCompatibility.get().targetRuntime.javaVersion != null
        } else if (higherOrEqual('2.10', distribution)) {
            assert sourceCompatibility.get().sourceLanguageLevel.name == '1.2'
            assert sourceCompatibility.get().targetBytecodeLevel.name == '1.2'
            assert sourceCompatibility.get().targetRuntime.homeDirectory == new File(System.getProperty("java.home"))
            assert sourceCompatibility.get().targetRuntime.javaVersion.name == JavaVersion.current().toString()
        } else {
            assert sourceCompatibility.get().sourceLanguageLevel.name == JavaVersion.current().toString()
            assert sourceCompatibility.get().targetBytecodeLevel.name == JavaVersion.current().toString()
            assert sourceCompatibility.get().targetRuntime.homeDirectory == new File(System.getProperty("java.home"))
            assert sourceCompatibility.get().targetRuntime.javaVersion.name == JavaVersion.current().toString()
        }

        where:
        distribution << gradleDistributionRange(">=1.2")
    }

    def "classpath containers"(GradleDistribution distribution) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        new TestFile(this.directoryProvider.testDirectory, 'sub1/build.gradle') << """
            apply plugin: 'java'
            apply plugin: 'eclipse'
            eclipse {
                classpath {
                    containers 'firstContainerPath', 'secondContainerPath'
                }
            }
        """

        when:
        def rootProject = fetchRootEclipseProject(fixedRequestAttributes, transientRequestAttributes)

        then:
        def eclipseProject = rootProject.tryFind({ it.name == 'sub1' } as Spec).get()
        def classpathContainers = eclipseProject.classpathContainers
        if (higherOrEqual('3.0', distribution)) {
            assert classpathContainers.get().find { it.path == 'firstContainerPath' }
            assert !classpathContainers.get().find { it.path == 'firstContainerPath' }.exported
            assert classpathContainers.get().find { it.path == 'secondContainerPath' }
            assert !classpathContainers.get().find { it.path == 'secondContainerPath' }.exported
        } else {
            assert !classpathContainers.isPresent()
        }

        where:
        distribution << gradleDistributionRange(">=1.2")
    }

    def "classpath container with classpath attributes"(GradleDistribution distribution) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        new TestFile(this.directoryProvider.testDirectory, 'sub1/build.gradle') << """
            apply plugin: 'java'
            apply plugin: 'eclipse'
            eclipse {
                classpath {
                    containers 'customContainer'
                    file {
                        whenMerged { classpath ->
                            classpath.entries.find { it.path == 'customContainer' }.entryAttributes.customKey = 'customValue'
                        }
                    }
                }
            }
        """

        when:
        def rootProject = fetchRootEclipseProject(fixedRequestAttributes, transientRequestAttributes)

        then:
        def eclipseProject = rootProject.tryFind({ it.name == 'sub1' } as Spec).get()
        def classpathContainers = eclipseProject.classpathContainers
        if (higherOrEqual('3.0', distribution)) {
            def attributes = classpathContainers.get().find { it.path == 'customContainer' }.classpathAttributes
            assert attributes.get().size() == 1
            assert attributes.get()[0].name == 'customKey'
            assert attributes.get()[0].value == 'customValue'
        }

        where:
        distribution << gradleDistributionRange(">=1.2")
    }

    def "classpath container with access rules"(GradleDistribution distribution) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        new TestFile(this.directoryProvider.testDirectory, 'sub1/build.gradle') << """
            import org.gradle.plugins.ide.eclipse.model.AccessRule
            apply plugin: 'java'
            apply plugin: 'eclipse'
            eclipse {
                classpath {
                    containers 'customContainer'
                    file {
                        whenMerged { classpath ->
                            def container = classpath.entries.find { it.kind == 'con' && it.path == 'customContainer' }
                            container.accessRules.add(new AccessRule('0', 'accessibleFilesPattern'))
                            container.accessRules.add(new AccessRule('1', 'nonAccessibleFilesPattern'))
                        }
                    }
                }
            }
        """

        when:
        def rootProject = fetchRootEclipseProject(fixedRequestAttributes, transientRequestAttributes)

        then:
        def eclipseProject = rootProject.tryFind({ it.name == 'sub1' } as Spec).get()
        def classpathContainers = eclipseProject.classpathContainers
        if (higherOrEqual('3.0', distribution)) {
            def accessRules = classpathContainers.get().find { it.path == 'customContainer' }.accessRules
            assert accessRules.get().size() == 2
            assert accessRules.get()[0].kind == 0
            assert accessRules.get()[0].pattern == 'accessibleFilesPattern'
            assert accessRules.get()[1].kind == 1
            assert accessRules.get()[1].pattern == 'nonAccessibleFilesPattern'
        }

        where:
        distribution << gradleDistributionRange(">=1.2")
    }

    def "source directory excludes and includes"(GradleDistribution distribution) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        new TestFile(this.directoryProvider.testDirectory, 'sub1/build.gradle') << """
            apply plugin: 'java'
            sourceSets {
                main {
                    java {
                        exclude 'excludePattern'
                        include 'includePattern'
                    }
                }
            }
        """
        new TestFile(this.directoryProvider.testDirectory, 'sub1/src/main/java').mkdirs()


        when:
        def rootProject = fetchRootEclipseProject(fixedRequestAttributes, transientRequestAttributes)

        then:
        def eclipseProject = rootProject.tryFind({ it.name == 'sub1' } as Spec).get()
        def sourceDir = eclipseProject.sourceDirectories.find { it.path == 'src/main/java' }
        if (higherOrEqual('3.0', distribution)) {
            assert sourceDir.excludes.get().size() == 1
            assert sourceDir.excludes.get()[0] == 'excludePattern'
            assert sourceDir.includes.get().size() == 1
            assert sourceDir.includes.get()[0] == 'includePattern'
        } else {
            assert !sourceDir.excludes.isPresent()
            assert !sourceDir.includes.isPresent()
        }

        where:
        distribution << gradleDistributionRange(">=1.2")
    }

    def "source directory classpath attributes"(GradleDistribution distribution) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        new TestFile(this.directoryProvider.testDirectory, 'sub1/build.gradle') << """
            apply plugin: 'java'
            apply plugin: 'eclipse'
            eclipse {
                classpath {
                    file {
                        whenMerged { classpath ->
                            classpath.entries.find { it.kind == 'src' && it.path == 'src/main/java' }.entryAttributes.customKey = 'customValue'
                        }
                    }
                }
            }
        """
        new TestFile(this.directoryProvider.testDirectory, 'sub1/src/main/java').mkdirs()
        new TestFile(this.directoryProvider.testDirectory, 'sub1/src/main/resources').mkdirs()

        when:
        def rootProject = fetchRootEclipseProject(fixedRequestAttributes, transientRequestAttributes)

        then:
        def eclipseProject = rootProject.tryFind({ it.name == 'sub1' } as Spec).get()
        def javaDir = eclipseProject.sourceDirectories.find { it.path == 'src/main/java' }
        def resourcesDir = eclipseProject.sourceDirectories.find { it.path == 'src/main/resources' }
        if (higherOrEqual('4.4', distribution)) {
            assert javaDir.classpathAttributes.get().size() == 3
            assert javaDir.classpathAttributes.get()[2].name == 'customKey'
            assert javaDir.classpathAttributes.get()[2].value == 'customValue'
            assert !resourcesDir.classpathAttributes.get().isEmpty()
        } else if (higherOrEqual('3.0', distribution)) {
            assert javaDir.classpathAttributes.get().size() == 1
            assert javaDir.classpathAttributes.get()[0].name == 'customKey'
            assert javaDir.classpathAttributes.get()[0].value == 'customValue'
            assert resourcesDir.classpathAttributes.get().isEmpty()
        } else {
            assert !javaDir.classpathAttributes.isPresent()
            assert !resourcesDir.classpathAttributes.isPresent()
        }

        where:
        distribution << gradleDistributionRange(">=1.2")
    }

    def "source directory access rules"(GradleDistribution distribution) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        new TestFile(this.directoryProvider.testDirectory, 'sub1/build.gradle') << """
            import org.gradle.plugins.ide.eclipse.model.AccessRule
            apply plugin: 'java'
            apply plugin: 'eclipse'
            eclipse {
                classpath {
                    file {
                        whenMerged { classpath ->
                            def sourceDir = classpath.entries.find { it.kind == 'src' && it.path == 'src/main/java' }
                            sourceDir.accessRules.add(new AccessRule('0', 'accessibleFilesPattern'))
                            sourceDir.accessRules.add(new AccessRule('1', 'nonAccessibleFilesPattern'))
                        }
                    }
                }
            }
         """
        new TestFile(this.directoryProvider.testDirectory, 'sub1/src/main/java').mkdirs()
        new TestFile(this.directoryProvider.testDirectory, 'sub1/src/main/resources').mkdirs()

        when:
        def rootProject = fetchRootEclipseProject(fixedRequestAttributes, transientRequestAttributes)

        then:
        def eclipseProject = rootProject.tryFind({ it.name == 'sub1' } as Spec).get()
        def javaDir = eclipseProject.sourceDirectories.find { it.path == 'src/main/java' }
        def resourcesDir = eclipseProject.sourceDirectories.find { it.path == 'src/main/resources' }
        if (higherOrEqual('4.4', distribution)) {
            assert javaDir.accessRules.get().size() == 2
            assert javaDir.accessRules.get()[0].kind == 0
            assert javaDir.accessRules.get()[0].pattern == 'accessibleFilesPattern'
            assert javaDir.accessRules.get()[1].kind == 1
            assert javaDir.accessRules.get()[1].pattern == 'nonAccessibleFilesPattern'
            assert !resourcesDir.classpathAttributes.get().isEmpty()
        } else if (higherOrEqual('3.0', distribution)) {
            assert javaDir.accessRules.get().size() == 2
            assert javaDir.accessRules.get()[0].kind == 0
            assert javaDir.accessRules.get()[0].pattern == 'accessibleFilesPattern'
            assert javaDir.accessRules.get()[1].kind == 1
            assert javaDir.accessRules.get()[1].pattern == 'nonAccessibleFilesPattern'
            assert resourcesDir.classpathAttributes.get().isEmpty()
        } else {
            assert !javaDir.classpathAttributes.isPresent()
            assert !resourcesDir.classpathAttributes.isPresent()
        }

        where:
        distribution << gradleDistributionRange(">=1.2")
    }

    def "source directory output"(GradleDistribution distribution) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        new TestFile(this.directoryProvider.testDirectory, 'sub1/build.gradle') << """
            import org.gradle.plugins.ide.eclipse.model.AccessRule
            apply plugin: 'java'
            apply plugin: 'eclipse'
            eclipse {
                classpath {
                    file {
                        whenMerged { classpath ->
                            def sourceDir = classpath.entries.find { it.kind == 'src' && it.path == 'src/main/java' }
                            sourceDir.output = 'mainClasses'
                        }
                    }
                }
            }
        """
        new TestFile(this.directoryProvider.testDirectory, 'sub1/src/main/java').mkdirs()
        new TestFile(this.directoryProvider.testDirectory, 'sub1/src/main/resources').mkdirs()

        when:
        def rootProject = fetchRootEclipseProject(fixedRequestAttributes, transientRequestAttributes)

        then:
        def eclipseProject = rootProject.tryFind({ it.name == 'sub1' } as Spec).get()
        def javaDir = eclipseProject.sourceDirectories.find { it.path == 'src/main/java' }
        def resourcesDir = eclipseProject.sourceDirectories.find { it.path == 'src/main/resources' }
        if (higherOrEqual('4.4', distribution)) {
            assert javaDir.output.get() == 'mainClasses'
            assert resourcesDir.output.get() == 'bin/main'
        } else if (higherOrEqual('3.0', distribution)) {
            assert javaDir.output.get() == 'mainClasses'
            assert resourcesDir.output.get() == null
        } else {
            assert !javaDir.output.isPresent()
            assert !resourcesDir.output.isPresent()
        }

        where:
        distribution << gradleDistributionRange(">=1.2")
    }

    def "output location"(GradleDistribution distribution) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        new TestFile(this.directoryProvider.testDirectory, 'sub1/build.gradle') << """
            import org.gradle.plugins.ide.eclipse.model.AccessRule
            apply plugin: 'java'
            apply plugin: 'eclipse'
            eclipse {
                classpath {
                    file {
                        whenMerged { classpath ->
                            classpath.entries.removeAll { it.kind == 'output' }
                            classpath.entries.add(new org.gradle.plugins.ide.eclipse.model.Output('bin/classes'))
                        }
                    }
                }
            }
        """

        when:
        def rootProject = fetchRootEclipseProject(fixedRequestAttributes, transientRequestAttributes)

        then:
        def eclipseProject = rootProject.tryFind({ it.name == 'sub1' } as Spec).get()
        def output = eclipseProject.getOutputLocation()
        if (higherOrEqual('3.0', distribution)) {
            assert output.get().path == 'bin/classes'
        } else {
            assert !output.isPresent()
        }

        where:
        distribution << gradleDistributionRange(">=1.2")
    }

    def "dependency classpath attributes"(GradleDistribution distribution) {
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderMultiProjectBuild.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        new TestFile(directoryProviderMultiProjectBuild.testDirectory, 'impl/build.gradle') << """
            apply plugin: 'eclipse'
            eclipse {
                classpath {
                    downloadSources = false
                    downloadJavadoc = true
                    file {
                        whenMerged { classpath ->
                            classpath.entries.find {  it.path.contains('api') }.entryAttributes.customKey = 'customValue'
                        }
                    }
                }
            }
        """

        when:
        def rootProject = fetchRootEclipseProject(fixedRequestAttributes, transientRequestAttributes)

        then:
        def eclipseProject = rootProject.tryFind({ it.name == 'impl' } as Spec).get()

        if (higherOrEqual('4.4', distribution)) {
            assert eclipseProject.projectDependencies[0].classpathAttributes.get().size() == 1
            assert eclipseProject.projectDependencies[0].classpathAttributes.get()[0].name == 'customKey'
            assert eclipseProject.projectDependencies[0].classpathAttributes.get()[0].value == 'customValue'
            assert eclipseProject.externalDependencies[0].classpathAttributes.get().size() == 2
            assert eclipseProject.externalDependencies[0].classpathAttributes.get()[0].name == 'javadoc_location'
            assert eclipseProject.externalDependencies[0].classpathAttributes.get()[0].value.contains('log4j')
            assert eclipseProject.externalDependencies[0].classpathAttributes.get()[1].name == 'gradle_used_by_scope'
            assert eclipseProject.externalDependencies[0].classpathAttributes.get()[1].value == 'main,test'
        } else if (higherOrEqual('3.0', distribution)) {
            assert eclipseProject.projectDependencies[0].classpathAttributes.get().size() == 1
            assert eclipseProject.projectDependencies[0].classpathAttributes.get()[0].name == 'customKey'
            assert eclipseProject.projectDependencies[0].classpathAttributes.get()[0].value == 'customValue'
            assert eclipseProject.externalDependencies[0].classpathAttributes.get().size() == 1
            assert eclipseProject.externalDependencies[0].classpathAttributes.get()[0].name == 'javadoc_location'
            assert eclipseProject.externalDependencies[0].classpathAttributes.get()[0].value.contains('log4j')
        } else if (higherOrEqual('2.14', distribution)) {
            assert eclipseProject.projectDependencies[0].classpathAttributes.get().size() == 0
            assert eclipseProject.externalDependencies[0].classpathAttributes.get().size() == 0
        } else {
            assert !eclipseProject.projectDependencies[0].classpathAttributes.isPresent()
            assert !eclipseProject.externalDependencies[0].classpathAttributes.isPresent()
        }

        where:
        distribution << gradleDistributionRange(">=1.2")
    }

    def "dependency access rules"(GradleDistribution distribution) {
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderMultiProjectBuild.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        new TestFile(directoryProviderMultiProjectBuild.testDirectory, 'impl/build.gradle') << """
            import org.gradle.plugins.ide.eclipse.model.AccessRule
            apply plugin: 'eclipse'
            eclipse {
                classpath {
                    file {
                        whenMerged { classpath ->
                            def api = classpath.entries.find {  it.path.contains('api') }
                            def log4j = classpath.entries.find {  it.path.contains('log4j') }
                            api.accessRules.add(new AccessRule('0', 'accessibleFilesPattern1'))
                            api.accessRules.add(new AccessRule('1', 'nonAccessibleFilesPattern1'))
                            log4j.accessRules.add(new AccessRule('0', 'accessibleFilesPattern2'))
                            log4j.accessRules.add(new AccessRule('1', 'nonAccessibleFilesPattern2'))

                        }
                    }
                }
            }
        """

        when:
        def rootProject = fetchRootEclipseProject(fixedRequestAttributes, transientRequestAttributes)

        then:
        def eclipseProject = rootProject.tryFind({ it.name == 'impl' } as Spec).get()

        if (higherOrEqual('3.0', distribution)) {
            assert eclipseProject.projectDependencies[0].accessRules.get().size() == 2
            assert eclipseProject.projectDependencies[0].accessRules.get()[0].kind == 0
            assert eclipseProject.projectDependencies[0].accessRules.get()[0].pattern == 'accessibleFilesPattern1'
            assert eclipseProject.projectDependencies[0].accessRules.get()[1].kind == 1
            assert eclipseProject.projectDependencies[0].accessRules.get()[1].pattern == 'nonAccessibleFilesPattern1'
            assert eclipseProject.externalDependencies[0].accessRules.get()[0].kind == 0
            assert eclipseProject.externalDependencies[0].accessRules.get()[0].pattern == 'accessibleFilesPattern2'
            assert eclipseProject.externalDependencies[0].accessRules.get()[1].kind == 1
            assert eclipseProject.externalDependencies[0].accessRules.get()[1].pattern == 'nonAccessibleFilesPattern2'
        } else {
            assert !eclipseProject.projectDependencies[0].accessRules.isPresent()
            assert !eclipseProject.externalDependencies[0].accessRules.isPresent()
        }

        where:
        distribution << gradleDistributionRange(">=1.2")
    }

    def "can handle composite builds"(GradleDistribution distribution) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderCompositeBuild.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        def repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

        when:
        def rootProjects = repository.fetchEclipseGradleProjects(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED).findAll { it.parent == null }

        then:
        rootProjects[0].name == 'root'
        if (higherOrEqual('4.0', distribution)) {
            assert rootProjects.size() == 3
            assert rootProjects[0].name == 'root'
            assert rootProjects[0].children.isEmpty()
            assert rootProjects[1].name == 'included1'
            assert rootProjects[1].children*.name == ['included1-sub1', 'included1-sub2']
            assert rootProjects[2].name == 'included2'
            assert rootProjects[2].children*.name == ['included2-sub1', 'included2-sub2']
        } else if (higherOrEqual('3.3', distribution)) {
            assert rootProjects.size() == 3
            assert rootProjects[0].name == 'root'
            assert rootProjects[0].children.isEmpty()
            assert rootProjects[1].name == 'included1'
            assert rootProjects[1].children*.name == ['sub1', 'sub2']
            assert rootProjects[2].name == 'included2'
            assert rootProjects[2].children*.name == ['sub1', 'sub2']
        } else {
            assert rootProjects[0].name == 'root'
            assert rootProjects[0].children.isEmpty()
        }

        where:
        distribution << gradleDistributionRange(">=3.1")
    }


    def "when exception is thrown"(GradleDistribution distribution) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildFile.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())

        AtomicReference<?> publishedEvent = new AtomicReference<>();
        def listener = new Object() {
            @SuppressWarnings("GroovyUnusedDeclaration")
            @Subscribe
            public void listen(EclipseProjectUpdateEvent event) {
                publishedEvent.set(event)
            }
        }

        when:
        fetchRootEclipseProject(fixedRequestAttributes, transientRequestAttributes, listener)

        then:
        thrown(GradleConnectionException)

        publishedEvent.get() == null

        where:
        distribution << gradleDistributionRange(">=1.2")
    }

    private fetchRootEclipseProject(FixedRequestAttributes fixedRequestAttributes, TransientRequestAttributes transientRequestAttributes, Object listener = new Object()) {
        def repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())
        repository.register(listener)
        return (repository.fetchEclipseGradleProjects(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED) as List)[0]
    }
}
