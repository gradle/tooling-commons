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
import org.junit.Rule

import com.google.common.collect.ImmutableList
import com.google.common.eventbus.EventBus

import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.ToolingModelToolingClientSpecification
import com.gradleware.tooling.spock.VerboseUnroll
import com.gradleware.tooling.testing.GradleVersionParameterization
import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.OmniEclipseWorkspace
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes
import com.gradleware.tooling.toolingmodel.repository.ModelRepositoryProvider
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes

@VerboseUnroll(formatter = GradleDistributionFormatter.class)
class CompositeModelRepositoryDeduplicationSpec extends ToolingModelToolingClientSpecification {

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

    def "Workspace contains all projects from all participants"(GradleDistribution distribution) {
        when:
        OmniEclipseWorkspace eclipseWorkspace = getWorkspaceModel(distribution)

        then:
        eclipseWorkspace != null
        eclipseWorkspace.openEclipseProjects*.name as Set == ['projectA', 'server', 'client', 'android', 'projectB', 'api', 'impl'] as Set

        where:
        distribution << runWithAllGradleVersions(">=1.0")
    }

    def "Sub-Project names are de-duplicated"(GradleDistribution distribution) {
        given:
        projectB.file("settings.gradle") << "include 'client:android'"

        when:
        OmniEclipseWorkspace eclipseWorkspace = getWorkspaceModel(distribution)

        then:
        eclipseWorkspace != null
        eclipseWorkspace.openEclipseProjects*.name as Set == ['projectA', 'server', 'projectA-client', 'projectA-client-android', 'api', 'projectB', 'projectB-client', 'projectB-client-android', 'impl'] as Set
        def root = eclipseWorkspace.tryFind { p -> p.name == 'projectA' }.get()
        root.tryFind { p -> p.name == 'projectA-client-android' }.get()
        eclipseWorkspace.tryFind { p -> p.name == 'projectA-client-android' }.get()

        where:
        distribution << runWithAllGradleVersions(">=1.0")
    }

    def "Duplicate root project names are rejected"(GradleDistribution distribution) {
        given:
        projectB.file("settings.gradle") << "rootProject.name = 'projectA'"

        when:
        OmniEclipseWorkspace eclipseWorkspace = getWorkspaceModel(distribution)

        then:
        def problem = thrown IllegalArgumentException
        problem.message.contains("Duplicate root project name 'projectA'")

        where:
        distribution << runWithAllGradleVersions(">=1.0")
    }

    private getWorkspaceModel(GradleDistribution distribution) {
        def participantA = new FixedRequestAttributes(projectA.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def participantB = new FixedRequestAttributes(projectB.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def repository = new DefaultCompositeModelRepository([participantA, participantB] as Set, toolingClient, new EventBus())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        repository.fetchEclipseWorkspace(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
    }

    private static ImmutableList<GradleDistribution> runWithAllGradleVersions(String versionPattern) {
        GradleVersionParameterization.Default.INSTANCE.getGradleDistributions(versionPattern)
    }
}