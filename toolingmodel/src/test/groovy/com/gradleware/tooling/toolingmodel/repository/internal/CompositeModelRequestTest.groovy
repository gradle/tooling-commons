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
import com.gradleware.tooling.toolingclient.GradleBuildIdentifier;
import com.gradleware.tooling.toolingclient.ToolingClient
import com.gradleware.tooling.toolingmodel.OmniEclipseWorkspace
import groovy.transform.NotYetImplemented
import org.gradle.tooling.model.GradleProject
import org.gradle.tooling.model.eclipse.EclipseProject
import org.junit.Rule
import spock.lang.Shared
import spock.lang.Specification


class CompositeModelRequestTest extends Specification{

    @Shared
    ToolingClient toolingClient = ToolingClient.newClient()

    @Rule
    TestDirectoryProvider directoryProvider = new TestDirectoryProvider("test-project");


    @NotYetImplemented
    def "If no project identifier is set then the request throws IllegalArgumentException"() {
        setup:
        def request = toolingClient.newCompositeModelRequest(OmniEclipseWorkspace)

        when:
        request.executeAndWait()

        then:
        thrown(IllegalArgumentException)
    }

    @NotYetImplemented
    def "Querying an invalid model throws UnsupportedModelException"(){
        setup:
        def request = toolingClient.newCompositeModelRequest(modelType)

        when:
        request.executeAndWait()

        then:
        thrown UnsupportedModelException

        where:
        modelType << [EclipseProject, GradleProject]
    }

    @NotYetImplemented
    def "Can query workspace model for a single-module project"() {
        setup:
        def request = toolingClient.newCompositeModelRequest(OmniEclipseWorkspace)
        directoryProvider.createFile("build.gradle")
        directoryProvider.createFile("settings.gradle") << "rootProject.name = 'root'"
        request.participants(GradleBuildIdentifier.create().projectDir(directoryProvider.testDirectory))

        when:
        def eclipseWorkspace = request.executeAndWait()

        then:
        eclipseWorkspace.openEclipseProjects.size() == 1
        eclipseWorkspace.openEclipseProjects[0].name == 'root'
    }

    @NotYetImplemented
    def "Can query workspace model for a multi-module project"() {
        setup:
        directoryProvider.createFile("build.gradle") << """
            project(':sub1'); project('sub2')
        """
        directoryProvider.createFile("settings.gradle") << """
            rootProject.name = 'root'
            include 'sub1', 'sub2'
        """
        def request = toolingClient.newCompositeModelRequest(OmniEclipseWorkspace)
        request.participants(GradleBuildIdentifier.create().projectDir(directoryProvider.testDirectory))

        when:
        def eclipseWorkspace = request.executeAndWait()

        then:
        eclipseWorkspace.openEclipseProjects.size() == 3
        eclipseWorkspace.openEclipseProjects.find { it.name == 'root'}
        eclipseWorkspace.openEclipseProjects.find { it.name == 'sub1'}
        eclipseWorkspace.openEclipseProjects.find { it.name == 'sub2'}
    }

    @NotYetImplemented
    def "Can't query workspace model if more than one root is specified"() {
        setup:
        def projectA = directoryProvider.createDir("a")
        def projectB = directoryProvider.createDir("b")
        def request = toolingClient.newCompositeModelRequest(OmniEclipseWorkspace)
        request.participants(GradleBuildIdentifier.create().projectDir(projectA))
        request.participants(GradleBuildIdentifier.create().projectDir(projectB))

        when:
        request.executeAndWait()

        then:
        thrown IllegalArgumentException
    }

    // TODO (donat) this should be replaced with the real exception class
    private class UnsupportedModelException extends Exception {}
}
