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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference

@VerboseUnroll(formatter = GradleDistributionFormatter.class)
class MultipleRootProjectCompositeModelRepositoryTest extends ToolingModelToolingClientSpecification {

    @Rule
    TestDirectoryProvider projectA = new TestDirectoryProvider("projectA");
    @Rule
    TestDirectoryProvider projectB = new TestDirectoryProvider("projectB");


    def setup() {
        projectA.createFile('settings.gradle') << '''
           rootProject.name = 'projectA'
           include 'server'
           include 'client:android'
        '''

        projectB.createFile('settings.gradle') << '''
          rootProject.name = 'projectB'
          include 'api'
          include 'impl'
        '''

    }


    def "Workspace contains all projects from all participants"(GradleDistribution distribution, Environment environment) {
        given:
        def participantA = new FixedRequestAttributes(projectA.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def participantB = new FixedRequestAttributes(projectB.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def repository = new DefaultCompositeModelRepository([participantA, participantB], toolingClient, new EventBus(), environment)
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())

        when:
        OmniEclipseWorkspace eclipseWorkspace = repository.fetchEclipseWorkspace(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

        then:
        eclipseWorkspace != null
        eclipseWorkspace.openEclipseProjects.size() == 7

        where:
        [distribution, environment]<< runInAllEnvironmentsForGradleTargetVersions(">=1.0")
    }


    def "An exception is thrown when two projects have the same name"(GradleDistribution distribution, Environment environment) {
        given:
        def participantA = new FixedRequestAttributes(projectA.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def participantB = new FixedRequestAttributes(projectB.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def repository = new DefaultCompositeModelRepository([participantA, participantB], toolingClient, new EventBus(), environment)
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())

        projectB.file('settings.gradle') << "include 'server'"

        when:
        OmniEclipseWorkspace eclipseWorkspace = repository.fetchEclipseWorkspace(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)

        then:
        thrown GradleConnectionException

        where:
        [distribution, environment]<< runInAllEnvironmentsForGradleTargetVersions(">=1.0")
    }


    private static ImmutableList<List<Object>> runInAllEnvironmentsForGradleTargetVersions(String versionPattern) {
        GradleVersionParameterization.Default.INSTANCE.getPermutations(versionPattern, Environment.values() as List)
    }
}
