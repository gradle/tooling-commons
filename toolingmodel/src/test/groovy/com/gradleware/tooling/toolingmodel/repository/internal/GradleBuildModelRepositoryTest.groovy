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
import com.gradleware.tooling.toolingmodel.OmniGradleBuild
import com.gradleware.tooling.toolingmodel.Path
import com.gradleware.tooling.toolingmodel.repository.*
import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProgressListener

import java.util.concurrent.atomic.AtomicReference

@VerboseUnroll(formatter = GradleDistributionFormatter.class)
class GradleBuildModelRepositoryTest extends ModelRepositorySpec {

    def "send event after cache update"(GradleDistribution distribution) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
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
        def gradleBuild = repository.fetchGradleBuild(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
        def rootProject = gradleBuild.rootProject

        then:
        gradleBuild != null
        rootProject != null
        rootProject.name == 'my root project'
        rootProject.path == Path.from(':')
        if (higherOrEqual('1.8', distribution)) {
            assert rootProject.projectDirectory.get().absolutePath == directoryProvider.testDirectory.absolutePath
        } else {
            assert !rootProject.projectDirectory.isPresent()
        }
        rootProject.root == rootProject
        rootProject.parent == null
        rootProject.children.size() == 2
        rootProject.children*.name == ['sub1', 'sub2']
        rootProject.children*.path.path == [':sub1', ':sub2']
        rootProject.children*.projectDirectory.collect {
            it.present ? it.get().absolutePath : null
        } == (higherOrEqual('1.8', distribution) ? ['sub1', 'sub2'].collect { new File(directoryProvider.testDirectory, it).absolutePath } : [null, null])
        rootProject.children*.root == [rootProject, rootProject]
        rootProject.children*.parent == [rootProject, rootProject]
        rootProject.all.size() == 4
        rootProject.all*.name == ['my root project', 'sub1', 'sub2', 'subSub1']

        def event = publishedEvent.get()
        event != null
        event.gradleBuild == gradleBuild

        def model = modelInRepository.get()
        model == gradleBuild

        where:
        distribution << gradleDistributionRange(">=1.2")
    }

    def "can handle composite builds"(GradleDistribution distribution) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderCompositeBuild.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        def repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus())

        when:
        def gradleBuild = repository.fetchGradleBuild(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
        def includedBuilds = gradleBuild.includedBuilds as List

        then:
        gradleBuild.rootProject.name == 'root'
        if (higherOrEqual('3.3', distribution)) {
            assert includedBuilds.size() == 2
            assert includedBuilds[0].rootProject.name == 'included1'
            assert includedBuilds[0].rootProject.children*.name == ['sub1', 'sub2']
            assert includedBuilds[1].rootProject.name == 'included2'
            assert includedBuilds[1].rootProject.children*.name == ['sub1', 'sub2']
        } else {
            assert includedBuilds.isEmpty()
        }

        where:
        distribution << gradleDistributionRange(">=3.1")
    }

    def "when exception is thrown"(GradleDistribution distribution) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildStructure.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
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
        distribution << gradleDistributionRange(">=1.2")
    }
}
