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
import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment
import com.gradleware.tooling.toolingmodel.repository.*
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProgressListener

import java.util.concurrent.atomic.AtomicReference

@VerboseUnroll(formatter = GradleDistributionFormatter.class)
class BuildEnvironmentModelRepositoryTest extends ModelRepositorySpec {

    def "send event after cache update"(GradleDistribution distribution, Environment environment) {
        given:
        def fixedRequestAttributes = new FixedRequestAttributes(directoryProvider.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        def repository = new DefaultModelRepository(fixedRequestAttributes, toolingClient, new EventBus(), environment)

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
        if (higherOrEqual('2.4', distribution)) {
            assert buildEnvironment.gradle.gradleUserHome.get() == new File(System.getProperty('user.home'), '.gradle')
        } else {
            assert !buildEnvironment.gradle.gradleUserHome.isPresent()
        }
        buildEnvironment.gradle.gradleVersion == extractVersion(distribution)
        buildEnvironment.java != null
        buildEnvironment.java.javaHome != null
        buildEnvironment.java.jvmArguments.size() > 0
        buildEnvironment.buildIdentifier

        def event = publishedEvent.get()
        event != null
        event.buildEnvironment == buildEnvironment

        def model = modelInRepository.get()
        model == buildEnvironment

        where:
        [distribution, environment] << runInAllEnvironmentsForGradleTargetVersions(">=1.2")
    }
}
