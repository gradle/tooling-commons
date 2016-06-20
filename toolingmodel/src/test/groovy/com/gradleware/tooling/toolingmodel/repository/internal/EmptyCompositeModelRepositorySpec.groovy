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

import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProgressListener
import org.gradle.tooling.connection.ModelResults;
import org.junit.Rule

import com.google.common.collect.ImmutableList
import com.google.common.eventbus.EventBus

import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.ToolingModelToolingClientSpecification
import com.gradleware.tooling.spock.VerboseUnroll
import com.gradleware.tooling.testing.GradleVersionParameterization
import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes
import com.gradleware.tooling.toolingmodel.repository.ModelRepositoryProvider
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes

@VerboseUnroll(formatter = GradleDistributionFormatter.class)
class EmptyCompositeModelRepositorySpec extends ToolingModelToolingClientSpecification {

    def "An empty workspace is returned"(GradleDistribution distribution) {
        when:
        ModelResults<OmniEclipseProject> eclipseProjects = fetchEclipseProjects(distribution)

        then:
        eclipseProjects != null
        eclipseProjects.size() == 0

        where:
        distribution << runWithAllGradleVersions(">=1.2")
    }

    private fetchEclipseProjects(GradleDistribution distribution) {
        def repository = new DefaultCompositeModelRepository([] as Set, toolingClient, new EventBus())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        repository.fetchEclipseProjects(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
    }

    private static ImmutableList<GradleDistribution> runWithAllGradleVersions(String versionPattern) {
        GradleVersionParameterization.Default.INSTANCE.getGradleDistributions(versionPattern)
    }
}
