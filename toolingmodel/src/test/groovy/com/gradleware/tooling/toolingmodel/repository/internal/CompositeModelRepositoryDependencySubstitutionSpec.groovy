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

import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProgressListener
import org.gradle.util.GradleVersion;
import org.junit.Rule

import com.google.common.collect.ImmutableList
import com.google.common.eventbus.EventBus

import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.ToolingModelToolingClientSpecification
import com.gradleware.tooling.spock.VerboseUnroll
import com.gradleware.tooling.testing.GradleVersionParameterization
import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.OmniEclipseProject
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes

@VerboseUnroll(formatter = GradleDistributionFormatter.class)
class CompositeModelRepositoryDependencySubstitutionSpec extends ToolingModelToolingClientSpecification {

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

        projectB.createFile('settings.gradle') << '''
            rootProject.name = "junit"
        '''
        projectB.createFile('build.gradle') << '''
            apply plugin: 'java'
            group "junit"
        '''
    }

    def "External dependencies are substituted for other projects in the composite"(GradleDistribution distribution) {
        when:
        Set<OmniEclipseProject> eclipseProjects= fetchEclipseProjects(distribution, distribution)

        then:
        eclipseProjects.size() == 2
        OmniEclipseProject eclipseProjectA = eclipseProjects.find { it.name == "projectA" }
        OmniEclipseProject eclipseProjectB = eclipseProjects.find { it.name == "junit" }
        eclipseProjectA.projectDependencies.size() == 1
        eclipseProjectA.projectDependencies[0].getTarget() == eclipseProjectB.identifier

        where:
        distribution << runWithAllGradleVersions(">=2.14")
    }

    def "Dependency substitution is deactivated if projects have different Gradle versions"() {
        when:
        Set<OmniEclipseProject> eclipseProjects= fetchEclipseProjects(GradleDistribution.forVersion(GradleVersion.current().version), GradleDistribution.forVersion('2.13'))

        then:
        eclipseProjects.size() == 2
        OmniEclipseProject eclipseProjectA = eclipseProjects.find { it.name == "projectA" }
        OmniEclipseProject eclipseProjectB = eclipseProjects.find { it.name == "junit" }
        eclipseProjectA.projectDependencies.size() == 0
    }

    private Set<OmniEclipseProject> fetchEclipseProjects(GradleDistribution distributionA, GradleDistribution distributionB) {
        def participantA = new FixedRequestAttributes(projectA.testDirectory, null, distributionA, null, ImmutableList.of(), ImmutableList.of())
        def participantB = new FixedRequestAttributes(projectB.testDirectory, null, distributionB, null, ImmutableList.of(), ImmutableList.of())
        def repository = new DefaultCompositeModelRepository([participantA, participantB] as Set, toolingClient, new EventBus())
        def transientRequestAttributes = new TransientRequestAttributes(true, null, null, null, ImmutableList.of(Mock(ProgressListener)), ImmutableList.of(Mock(org.gradle.tooling.events.ProgressListener)), GradleConnector.newCancellationTokenSource().token())
        repository.fetchEclipseProjects(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED).findAll { !it.failure }.collect { it.model }
    }

    private static ImmutableList<GradleDistribution> runWithAllGradleVersions(String versionPattern) {
        GradleVersionParameterization.Default.INSTANCE.getGradleDistributions(versionPattern)
    }
}
