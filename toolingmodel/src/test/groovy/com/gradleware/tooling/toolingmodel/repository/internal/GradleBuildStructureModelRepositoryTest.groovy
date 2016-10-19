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
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure
import com.gradleware.tooling.toolingmodel.Path
import com.gradleware.tooling.toolingmodel.repository.*
import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProgressListener

import java.util.concurrent.atomic.AtomicReference

@VerboseUnroll(formatter = GradleDistributionFormatter.class)
class GradleBuildStructureModelRepositoryTest extends ModelRepositorySpec {

    def "send event after cache update"(GradleDistribution distribution, Environment environment) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        def repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus(), environment)

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
        gradleBuildStructure.rootProject.path == Path.from(':')
        if (higherOrEqual('1.8', distribution)) {
            assert gradleBuildStructure.rootProject.projectDirectory.get().absolutePath == directoryProvider.testDirectory.absolutePath
        } else {
            assert !gradleBuildStructure.rootProject.projectDirectory.isPresent()
        }
        gradleBuildStructure.rootProject.root == gradleBuildStructure.rootProject
        gradleBuildStructure.rootProject.parent == null
        gradleBuildStructure.rootProject.children.size() == 2
        gradleBuildStructure.rootProject.children*.name == ['sub1', 'sub2']
        gradleBuildStructure.rootProject.children*.path.path == [':sub1', ':sub2']
        gradleBuildStructure.rootProject.children*.projectDirectory.collect {
            it.present ? it.get().absolutePath : null
        } == (higherOrEqual('1.8', distribution) ? ['sub1', 'sub2'].collect { new File(directoryProvider.testDirectory, it).absolutePath } : [null, null])
        gradleBuildStructure.rootProject.children*.root == [gradleBuildStructure.rootProject, gradleBuildStructure.rootProject]
        gradleBuildStructure.rootProject.children*.parent == [gradleBuildStructure.rootProject, gradleBuildStructure.rootProject]
        gradleBuildStructure.rootProject.all.size() == 4
        gradleBuildStructure.rootProject.all*.name == ['my root project', 'sub1', 'sub2', 'subSub1']

        def event = publishedEvent.get()
        event != null
        event.gradleBuildStructure == gradleBuildStructure

        def model = modelInRepository.get()
        model == gradleBuildStructure

        where:
        [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.2")
    }

    def "can handle composite builds"(GradleDistribution distribution, Environment environment) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderCompositeBuild.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        def repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus(), environment)

        when:
        def gradleBuildStructure = repository.fetchGradleBuildStructure(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

        then:
        gradleBuildStructure.rootProject.name == 'root'
        if (higherOrEqual('3.3', distribution)) {
            assert gradleBuildStructure.includedRootProjects.size() == 2
            assert gradleBuildStructure.includedRootProjects[0].name == 'included1'
            assert gradleBuildStructure.includedRootProjects[0].children*.name == ['sub1', 'sub2']
            assert gradleBuildStructure.includedRootProjects[1].name == 'included2'
            assert gradleBuildStructure.includedRootProjects[1].children*.name == ['sub1', 'sub2']
        } else {
            assert gradleBuildStructure.includedRootProjects.isEmpty()
        }

        where:
        [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=3.1")
    }


    def "when exception is thrown"(GradleDistribution distribution, Environment environment) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildStructure.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        def repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus(), environment)

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
        [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.2")
    }
}
