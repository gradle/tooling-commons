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
import com.gradleware.tooling.toolingmodel.OmniBuildInvocationsContainer
import com.gradleware.tooling.toolingmodel.Path
import com.gradleware.tooling.toolingmodel.repository.BuildInvocationsUpdateEvent
import com.gradleware.tooling.toolingmodel.repository.Environment
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes
import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProgressListener

import java.util.concurrent.atomic.AtomicReference

@VerboseUnroll(formatter = GradleDistributionFormatter.class)
class BuildInvocationsModelRepositoryTest extends ModelRepositorySpec {

    def "send event after cache update"(GradleDistribution distribution, Environment environment) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        def repository = new DefaultSingleBuildModelRepository(fixedRequestAttributes, toolingClient, new EventBus(), environment)

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
        def rootProjectExplicitTasks = buildInvocations.get(Path.from(':')).get().projectTasks.findAll { !ImplicitTasks.ALL.contains(it.name) }
        rootProjectExplicitTasks.size() == 1

        def projectSub1 = buildInvocations.get(Path.from(':sub1')).get()
        def sub1ExplicitTasks = projectSub1.projectTasks.findAll { !ImplicitTasks.ALL.contains(it.name) }
        sub1ExplicitTasks.size() == 2

        def myFirstTaskOfSub1 = sub1ExplicitTasks[0]
        myFirstTaskOfSub1.name == 'myFirstTaskOfSub1'
        myFirstTaskOfSub1.description == '1st task of sub1'
        myFirstTaskOfSub1.path.path == ':sub1:myFirstTaskOfSub1'
        myFirstTaskOfSub1.isPublic() == (!higherOrEqual('2.1', distribution) || !higherOrEqual("2.3", distribution) && environment != Environment.ECLIPSE || higherOrEqual('2.3', distribution))

        def mySecondTaskOfSub1 = sub1ExplicitTasks[1]
        mySecondTaskOfSub1.name == 'mySecondTaskOfSub1'
        mySecondTaskOfSub1.description == '2nd task of sub1'
        mySecondTaskOfSub1.path.path == ':sub1:mySecondTaskOfSub1'
        mySecondTaskOfSub1.isPublic() == !higherOrEqual('2.1', distribution)

        def projectSub2 = buildInvocations.get(Path.from(':sub2')).get()
        def sub2ExplicitTaskSelectors = projectSub2.taskSelectors.findAll { !ImplicitTasks.ALL.contains(it.name) }
        sub2ExplicitTaskSelectors.size() == 5

        def myTaskSelector = sub2ExplicitTaskSelectors.find { it.name == 'myTask' }
        myTaskSelector.name == 'myTask'
        myTaskSelector.description != null
        myTaskSelector.projectPath.path == ':sub2'
        myTaskSelector.isPublic() == (!higherOrEqual('2.1', distribution) || !higherOrEqual("2.3", distribution) && environment != Environment.ECLIPSE || higherOrEqual('2.3', distribution))
        myTaskSelector.selectedTaskPaths*.path as List == (!higherOrEqual('1.12', distribution) || !higherOrEqual('2.3', distribution) && environment == Environment.ECLIPSE ? [':sub2:myTask', ':sub2:subSub1:myTask'] : [])
        // empty selected task paths for task selectors from 'authentic' build invocations

        def event = publishedEvent.get()
        event != null
        event.buildInvocations == buildInvocations

        def model = modelInRepository.get()
        model == buildInvocations

        where:
        [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.2")
    }

    def "when exception is thrown"(GradleDistribution distribution, Environment environment) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProviderErroneousBuildFile.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        def repository = new DefaultSingleBuildModelRepository(fixedRequestAttributes, toolingClient, new EventBus(), environment)

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
}
