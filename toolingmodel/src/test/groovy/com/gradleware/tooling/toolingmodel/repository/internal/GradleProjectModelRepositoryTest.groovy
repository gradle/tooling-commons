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
import com.gradleware.tooling.spock.VerboseUnroll
import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.OmniGradleProject
import com.gradleware.tooling.toolingmodel.Path
import com.gradleware.tooling.toolingmodel.repository.*
import org.gradle.api.specs.Spec
import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProgressListener

import java.util.concurrent.atomic.AtomicReference

@VerboseUnroll(formatter = GradleDistributionFormatter.class)
class GradleProjectModelRepositoryTest extends ModelRepositorySpec {

    def "send event after cache update"(GradleDistribution distribution) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        def repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

        AtomicReference<GradleProjectUpdateEvent> publishedEvent = new AtomicReference<>();
        AtomicReference<OmniGradleProject> modelInRepository = new AtomicReference<>();
        repository.register(new Object() {

            @SuppressWarnings("GroovyUnusedDeclaration")
            @Subscribe
            public void listen(GradleProjectUpdateEvent event) {
                publishedEvent.set(event)
                modelInRepository.set(repository.fetchGradleProjects(transientRequestAttributes, FetchStrategy.FROM_CACHE_ONLY))
            }
        })

        when:
        def gradleProjects = repository.fetchGradleProjects(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
        def rootProject = (gradleProjects as List)[0]

        then:
        rootProject != null
        rootProject != null
        rootProject.name == 'my root project'
        rootProject.path == Path.from(':')
        if (higherOrEqual('2.4', distribution)) {
            assert rootProject.projectDirectory.get().absolutePath == directoryProvider.testDirectory.absolutePath
        } else {
            assert !rootProject.projectDirectory.isPresent()
        }
        if (higherOrEqual('2.0', distribution)) {
            assert rootProject.buildDirectory.get().absolutePath == directoryProvider.file('build').absolutePath
        } else {
            assert !rootProject.buildDirectory.isPresent()
        }
        if (higherOrEqual('1.8', distribution)) {
            assert rootProject.buildScript.get().sourceFile.absolutePath == directoryProvider.file('build.gradle').absolutePath
        } else {
            assert !rootProject.buildScript.isPresent()
        }
        rootProject.projectTasks.findAll { !ImplicitTasks.ALL.contains(it.name) }.size() == 1
        rootProject.root == rootProject
        rootProject.parent == null
        rootProject.children.size() == 2
        rootProject.children*.name == ['sub1', 'sub2']
        rootProject.children*.description == ['sub project 1', 'sub project 2']
        rootProject.children*.path.path == [':sub1', ':sub2']
        rootProject.children*.root == [rootProject, rootProject]
        rootProject.children*.parent == [rootProject, rootProject]
        rootProject.all.size() == 4
        rootProject.all*.name == ['my root project', 'sub1', 'sub2', 'subSub1']

        def projectSub1 = rootProject.tryFind({ OmniGradleProject input ->
            input.path.path == ':sub1'
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
        mySecondTaskOfSub1.isPublic() == !higherOrEqual('2.1', distribution)
        if (higherOrEqual('2.5', distribution)) {
            assert mySecondTaskOfSub1.group.get() == null
        } else {
            assert !mySecondTaskOfSub1.group.present
        }

        def projectSub2 = rootProject.tryFind({ OmniGradleProject input ->
            input.path.path == ':sub2'
        } as Spec).get()
        projectSub2.taskSelectors.findAll { !ImplicitTasks.ALL.contains(it.name) }.size() == 5

        def myTaskSelector = projectSub2.taskSelectors.find { it.name == 'myTask' }
        myTaskSelector.name == 'myTask'
        myTaskSelector.description == 'another task of sub2'
        myTaskSelector.projectPath.path == ':sub2'
        myTaskSelector.isPublic() == (!higherOrEqual('2.1', distribution) || higherOrEqual('2.3', distribution))
        myTaskSelector.selectedTaskPaths*.path as List == [':sub2:myTask', ':sub2:subSub1:myTask']

        def event = publishedEvent.get()
        event != null
        event.gradleProjects == gradleProjects

        def model = modelInRepository.get()
        model == gradleProjects

        where:
        distribution << gradleDistributionRange(">=1.2")
    }

    def "can handle composite builds"(GradleDistribution distribution) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderCompositeBuild.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        def repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

        when:
        def rootProjects = repository.fetchGradleProjects(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED).findAll { it.parent == null }

        then:
        rootProjects[0].name == 'root'
        if (higherOrEqual('3.3', distribution)) {
            assert rootProjects.size() == 3
            assert rootProjects[1].name == 'included1'
            assert rootProjects[1].children*.name == ['sub1', 'sub2']
            assert rootProjects[2].name == 'included2'
            assert rootProjects[2].children*.name == ['sub1', 'sub2']
        } else {
            assert rootProjects.size() == 1
        }

        where:
        distribution << gradleDistributionRange(">=3.1")
    }

    def "when exception is thrown"(GradleDistribution distribution) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildFile.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        def repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

        AtomicReference<GradleProjectUpdateEvent> publishedEvent = new AtomicReference<>();
        repository.register(new Object() {

            @SuppressWarnings("GroovyUnusedDeclaration")
            @Subscribe
            public void listen(GradleProjectUpdateEvent event) {
                publishedEvent.set(event)
            }
        })

        when:
        repository.fetchGradleProjects(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

        then:
        thrown(GradleConnectionException)

        publishedEvent.get() == null

        where:
        distribution << gradleDistributionRange(">=1.2")
    }
}
