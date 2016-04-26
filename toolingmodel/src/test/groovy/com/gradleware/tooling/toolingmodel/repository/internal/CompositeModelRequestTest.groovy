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
import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.toolingclient.CompositeBuildModelRequest;
import com.gradleware.tooling.toolingclient.GradleBuildIdentifier
import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingclient.ToolingClient
import com.gradleware.tooling.toolingmodel.repository.internal.CompositeModelRequestTest.FetchMode;

import groovy.transform.NotYetImplemented
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference
import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.model.GradleProject
import org.gradle.tooling.model.eclipse.EclipseProject
import org.junit.Rule
import spock.lang.Shared
import spock.lang.Specification


class CompositeModelRequestTest extends Specification {

    @Shared
    ToolingClient toolingClient = ToolingClient.newClient()

    @Rule
    TestDirectoryProvider directoryProvider = new TestDirectoryProvider("test-project");

    def "If no model type is given, a NullPointerException is thrown"() {
        when:
        toolingClient.newCompositeModelRequest(null)

        then:
        thrown(NullPointerException)
    }

    def "If no project identifier is set then the request throws IllegalArgumentException"(FetchMode fetchMode) {
        setup:
        def request = toolingClient.newCompositeModelRequest(EclipseProject)

        when:
        getEclipseProjects(request, fetchMode)

        then:
        thrown(IllegalArgumentException)

        where:
        fetchMode << FetchMode.values()
    }

    def "Querying an invalid model throws IllegalArgumentException"(Class<?> modelType, FetchMode fetchMode){
        setup:
        def request = toolingClient.newCompositeModelRequest(modelType)

        when:
        getEclipseProjects(request, fetchMode)

        then:
        thrown IllegalArgumentException

        where:
        modelType << [Serializable, String]
        fetchMode << FetchMode.values()
    }

    def "Can query project models for a single-module project"(FetchMode fetchMode) {
        setup:
        def request = toolingClient.newCompositeModelRequest(EclipseProject)
        directoryProvider.createFile("build.gradle")
        directoryProvider.createFile("settings.gradle") << "rootProject.name = 'root'"
        request.participants(new GradleBuildIdentifier(directoryProvider.testDirectory, GradleDistribution.fromBuild()))

        when:
        def projects = getEclipseProjects(request, fetchMode)

        then:
        projects.size() == 1
        projects[0].name == 'root'

        where:
        fetchMode << FetchMode.values()
    }

    def "Can query workspace model for a multi-module project"(FetchMode fetchMode) {
        setup:
        directoryProvider.createFile("build.gradle") << """
            project(':sub1'); project('sub2')
        """
        directoryProvider.createFile("settings.gradle") << """
            rootProject.name = 'root'
            include 'sub1', 'sub2'
        """
        def request = toolingClient.newCompositeModelRequest(EclipseProject)
        request.participants(new GradleBuildIdentifier(directoryProvider.testDirectory, GradleDistribution.fromBuild()))

        when:
        def projects = getEclipseProjects(request, fetchMode)

        then:
        projects.size() == 3
        projects.find { it.name == 'root'}
        projects.find { it.name == 'sub1'}
        projects.find { it.name == 'sub2'}

        where:
        fetchMode << FetchMode.values()
    }


    def "If one project is broken, models from other projects are still returned"(FetchMode fetchMode) {
        setup:
        def projectA = directoryProvider.createDir("a")
        def projectB = directoryProvider.createDir("b")
        directoryProvider.createFile("a", "build.gradle") << """
            throw new Exception()
        """
        directoryProvider.createFile("a", "settings.gradle") << """
            rootProject.name = 'project-a'
            include 'sub1a', 'sub2a'
        """
        directoryProvider.createFile("b", "settings.gradle") << """
            rootProject.name = 'project-b'
            include 'sub1b', 'sub2b'
        """

        def request = toolingClient.newCompositeModelRequest(EclipseProject)
        request.addParticipants(new GradleBuildIdentifier(projectA, GradleDistribution.fromBuild()))
        request.addParticipants(new GradleBuildIdentifier(projectB, GradleDistribution.fromBuild()))

        when:
        def projects = getEclipseProjects(request, fetchMode)

        then:
        projects.size() == 3
        projects.find { it.name == 'project-b'}
        projects.find { it.name == 'sub1b'}
        projects.find { it.name == 'sub2b'}

        where:
        fetchMode << FetchMode.values()
    }

    def "Can query workspace model if more than one root is specified"(FetchMode fetchMode) {
        setup:
        def projectA = directoryProvider.createDir("a")
        def projectB = directoryProvider.createDir("b")
        directoryProvider.createFile("a", "settings.gradle") << """
            rootProject.name = 'project-a'
            include 'sub1a', 'sub2a'
        """
        directoryProvider.createFile("b", "settings.gradle") << """
            rootProject.name = 'project-b'
            include 'sub1b', 'sub2b'
        """

        def request = toolingClient.newCompositeModelRequest(EclipseProject)
        request.addParticipants(new GradleBuildIdentifier(projectA, GradleDistribution.fromBuild()))
        request.addParticipants(new GradleBuildIdentifier(projectB, GradleDistribution.fromBuild()))

        when:
        def projects = getEclipseProjects(request, fetchMode)

        then:
        projects.size() == 6
        projects.find { it.name == 'project-a'}
        projects.find { it.name == 'project-b'}
        projects.find { it.name == 'sub1a'}
        projects.find { it.name == 'sub2a'}
        projects.find { it.name == 'sub1b'}
        projects.find { it.name == 'sub2b'}

        where:
        fetchMode << FetchMode.values()
    }

    private def Set<EclipseProject> getEclipseProjects(CompositeBuildModelRequest<EclipseProject> request, FetchMode fetchMode) {
        if (fetchMode == FetchMode.SYNC) {
            return request.executeAndWait().findAll { !it.failure }.collect { it.model }.toSet()
        } else {
            def result = new AtomicReference<Set<EclipseProject>>()
            def failure = new AtomicReference<GradleConnectionException>()
            def promise = request.execute()
            def latch = new CountDownLatch(1)

            promise.onFailure { exception ->
                failure.set(exception)
                latch.countDown()
            }
            promise.onComplete { projects ->
                result.set(projects.findAll { !it.failure }.collect { it.model }.toSet())
                latch.countDown()
            }
            latch.await()
            if (failure.get()) {
                throw failure.get()
            } else {
                return result.get()
            }
        }
    }

    private static enum FetchMode {
        SYNC,
        ASYNC
    }
}
