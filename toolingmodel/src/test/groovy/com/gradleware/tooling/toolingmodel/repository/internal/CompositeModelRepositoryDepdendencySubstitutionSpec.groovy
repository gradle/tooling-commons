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
import spock.lang.Ignore;

import com.google.common.collect.ImmutableList
import com.google.common.eventbus.EventBus

import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.ToolingModelToolingClientSpecification
import com.gradleware.tooling.spock.VerboseUnroll
import com.gradleware.tooling.testing.GradleVersionParameterization
import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniEclipseWorkspace
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes
import com.gradleware.tooling.toolingmodel.repository.ModelRepositoryProvider
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes

@Ignore("Not yet implemented")
@VerboseUnroll(formatter = GradleDistributionFormatter.class)
class CompositeModelRepositoryDepdendencySubstitutionSpec extends ToolingModelToolingClientSpecification {

    @Rule
    TestDirectoryProvider projectA = new TestDirectoryProvider("projectA");
    @Rule
    TestDirectoryProvider projectB = new TestDirectoryProvider("projectB");


    def setup() {
        projectA.createFile('build.gradle') << '''
            apply plugin: 'java'

            dependencies {
                testCompile 'junit:junit:4.12'
            }
        '''

        projectB.createFile('build.gradle') << '''
            apply plugin: 'java'
            apply plugin: 'maven'
            group = 'junit'
            uploadArchives.repositories.mavenDeployer {
                pom.artifactId = 'junit'
            }
        '''
    }

    def "External dependencies are substituted for other projects in the composite"(GradleDistribution distribution) {
        when:
        OmniEclipseWorkspace eclipseWorkspace = getWorkspaceModel(distribution)

        then:
        eclipseWorkspace != null
        OmniEclipseProject eclipseProjectA = eclipseWorkspace.tryFind { it.name == 'projectA' }.get()
        eclipseProjectA.projectDependencies.size() == 1
        eclipseProjectA.projectDependencies[0].targetProjectDir == projectB.testDirectory

        where:
        distribution << runWithAllGradleVersions(">=2.14")
    }

    private getWorkspaceModel(GradleDistribution distribution) {
        def participantA = new FixedRequestAttributes(projectA.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def participantB = new FixedRequestAttributes(projectB.testDirectory, null, distribution, null, ImmutableList.of(), ImmutableList.of())
        def repoProvider = Stub(ModelRepositoryProvider)
        repoProvider.getModelRepository(participantA) >> new DefaultSimpleModelRepository(participantA, toolingClient, new EventBus())
        repoProvider.getModelRepository(participantB) >> new DefaultSimpleModelRepository(participantB, toolingClient, new EventBus())
        def repository = new DefaultCompositeModelRepository(repoProvider, [participantA, participantB] as Set, toolingClient, new EventBus())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        repository.fetchEclipseWorkspace(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED)
    }

    private static ImmutableList<GradleDistribution> runWithAllGradleVersions(String versionPattern) {
        GradleVersionParameterization.Default.INSTANCE.getGradleDistributions(versionPattern)
    }
}
