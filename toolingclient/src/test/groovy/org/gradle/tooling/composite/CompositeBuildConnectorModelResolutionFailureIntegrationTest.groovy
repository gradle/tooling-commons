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
        withCompositeConnection([])

        then:
        Throwable t = thrown(IllegalStateException)
        t.message == "A composite build requires at least one participating project."
    }

    def "cannot create composite for participant with null project directory"() {
        when:
        withCompositeConnection([null])

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
        File projectDir = directoryProvider.createDir('dev', 'project')
        projectDir.deleteDir()

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
}
