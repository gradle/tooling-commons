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
import com.gradleware.tooling.toolingclient.CompositeModelRequest;
import com.gradleware.tooling.toolingclient.GradleBuildIdentifier;
import com.gradleware.tooling.toolingclient.ToolingClient
import com.gradleware.tooling.toolingmodel.OmniEclipseWorkspace

import groovy.transform.NotYetImplemented
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


    def "If no project identifier is set then the request throws IllegalArgumentException"() {
        setup:
        def request = toolingClient.newCompositeModelRequest(EclipseProject)

        when:
        request.executeAndWait()

        then:
        thrown(IllegalArgumentException)
    }

    def "Querying an invalid model throws IllegalArgumentException"(){
        setup:
        def request = toolingClient.newCompositeModelRequest(modelType)

        when:
        request.executeAndWait()

        then:
        thrown IllegalArgumentException

        where:
        modelType << [GradleProject, String]
    }

    def "Can query project models for a single-module project"() {
        setup:
        def request = toolingClient.newCompositeModelRequest(EclipseProject)
        directoryProvider.createFile("build.gradle")
        directoryProvider.createFile("settings.gradle") << "rootProject.name = 'root'"
        request.participants(GradleBuildIdentifier.withProjectDir(directoryProvider.testDirectory))

        when:
        def projects = request.executeAndWait()

        then:
        projects.size() == 1
        projects[0].name == 'root'
    }

    def "Can query workspace model for a multi-module project"() {
        setup:
        directoryProvider.createFile("build.gradle") << """
            project(':sub1'); project('sub2')
        """
        directoryProvider.createFile("settings.gradle") << """
            rootProject.name = 'root'
            include 'sub1', 'sub2'
        """
        def request = toolingClient.newCompositeModelRequest(EclipseProject)
        request.participants(GradleBuildIdentifier.withProjectDir(directoryProvider.testDirectory))

        when:
        def projects = request.executeAndWait()

        then:
        projects.size() == 3
        projects.find { it.name == 'root'}
        projects.find { it.name == 'sub1'}
        projects.find { it.name == 'sub2'}
    }

    def "Can query workspace model if more than one root is specified"() {
        setup:
        def projectA = directoryProvider.createDir("a")
        def projectB = directoryProvider.createDir("b")
        directoryProvider.createFile("a", "build.gradle") << """
            project(':sub1a'); project('sub2a')
        """
        directoryProvider.createFile("a", "settings.gradle") << """
            rootProject.name = 'project-a'
            include 'sub1a', 'sub2a'
        """
        directoryProvider.createFile("b", "build.gradle") << """
            project(':sub1b'); project('sub2b')
        """
        directoryProvider.createFile("b", "settings.gradle") << """
            rootProject.name = 'project-b'
            include 'sub1b', 'sub2b'
        """

        def request = toolingClient.newCompositeModelRequest(EclipseProject)
        request.addParticipants(GradleBuildIdentifier.withProjectDir(projectA))
        request.addParticipants(GradleBuildIdentifier.withProjectDir(projectB))

        when:
        def projects = request.executeAndWait()

        then:
        projects.size() == 6
        projects.find { it.name == 'project-a'}
        projects.find { it.name == 'project-b'}
        projects.find { it.name == 'sub1a'}
        projects.find { it.name == 'sub2a'}
        projects.find { it.name == 'sub1b'}
        projects.find { it.name == 'sub2b'}
    }
}
