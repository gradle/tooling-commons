/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.tooling.composite

import org.gradle.tooling.BuildException
import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.model.eclipse.EclipseProject

class CompositeBuildConnectorModelResolutionFailureIntegrationTest extends AbstractCompositeBuildConnectorIntegrationTest {

    def "cannot create composite with no participating projects"() {
        when:
        createComposite()

        then:
        Throwable t = thrown(IllegalStateException)
        t.message == "A composite build requires at least one participating project."
    }

    def "cannot create composite for participant with null project directory"() {
        when:
        createComposite([null])

        then:
        Throwable t = thrown(IllegalStateException)
        t.message == "A project directory must be specified before creating a connection."
    }

    def "cannot request model that is not an interface"() {
        given:
        File projectDir = directoryProvider.createDir('project')
        createBuildFile(projectDir)

        when:
        withCompositeConnection([projectDir]) { connection ->
            connection.getModels(String)
        }

        then:
        Throwable t = thrown(IllegalArgumentException)
        t.message == "Cannot fetch a model of type 'java.lang.String' as this type is not an interface."
    }

    def "cannot request model for unknown model"() {
        given:
        File projectDir = directoryProvider.createDir('project')
        createBuildFile(projectDir)

        when:
        withCompositeConnection([projectDir]) { connection ->
            connection.getModels(List)
        }

        then:
        Throwable t = thrown(IllegalArgumentException)
        t.message == "The only supported model for a Gradle composite is EclipseProject.class."
    }

    def "cannot create composite for participant with non-existent project directory"() {
        given:
        File projectDir = new File('dev/project')

        when:
        withCompositeConnection([projectDir]) { connection ->
            connection.getModels(EclipseProject)
        }

        then:
        Throwable t = thrown(BuildException)
        t.message.contains("Could not fetch model of type 'EclipseProject'")
        t.cause.message == "Project directory '$projectDir.absolutePath' does not exist."
    }

    def "an exception is thrown if a the model cannot be built"() {
        given:
        File projectDir = directoryProvider.createDir('project')
        File buildFile = createBuildFile(projectDir)
        buildFile << """
            task myTask {
                doSomething {
                    println 'Hello world!"
                }
            }
        """

        when:
        withCompositeConnection([projectDir]) { connection ->
            connection.getModels(EclipseProject)
        }

        then:
        Throwable t = thrown(GradleConnectionException)
        t.message.contains("Could not fetch model of type 'EclipseProject'")
        t.cause.message.contains("Could not compile build file '$buildFile.absolutePath'.")
    }

    def "cannot create composite with multiple participating builds that contain projects with the same name"() {
        given:
        File projectDir1 = directoryProvider.createDir('project-1/my-project')
        createBuildFile(projectDir1)
        File projectDir2 = directoryProvider.createDir('project-2/my-project')
        createBuildFile(projectDir2)

        when:
        withCompositeConnection([projectDir1, projectDir2]) { connection ->
            connection.getModels(EclipseProject)
        }

        then:
        Throwable t = thrown(GradleConnectionException)
        t.message.contains("Could not fetch model of type 'EclipseProject'")
        t.cause instanceof IllegalStateException
        t.cause.message == "A composite build does not allow duplicate project names for any of the participating project. Offending project name: 'my-project'"
    }

    def "cannot create composite with second instance of project within the same participating, flat build"() {
        given:
        File rootProjectDir = directoryProvider.createDir('master')
        createBuildFile(rootProjectDir)
        File sub1ProjectDir = directoryProvider.createDir('sub-1')
        createBuildFile(sub1ProjectDir)
        File sub2ProjectDir = directoryProvider.createDir('sub-2')
        createBuildFile(sub2ProjectDir)
        File sub3ProjectDir = directoryProvider.createDir('sub-3')
        createBuildFile(sub3ProjectDir)
        createSettingsFile(rootProjectDir, ['sub-1', 'sub-2', 'sub-3'])

        when:
        withCompositeConnection([rootProjectDir, sub2ProjectDir]) { connection ->
            connection.getModels(EclipseProject)
        }

        then:
        Throwable t = thrown(GradleConnectionException)
        t.message.contains("Could not fetch model of type 'EclipseProject'")
        t.cause instanceof IllegalStateException
        t.cause.message == "A composite build does not allow duplicate project names for any of the participating project. Offending project name: 'sub-2'"
    }
}
