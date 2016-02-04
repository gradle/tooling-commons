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
package org.gradle.tooling.composite

import org.gradle.tooling.model.eclipse.EclipseProject

import com.google.common.collect.Sets;

class CompsoteBuildConnectorDeduplicationIntegrationTest extends AbstractCompositeBuildConnectorIntegrationTest {

    def "Conflicting root project names are de-duplicated using a counter"() {
        given:
        File projectA = directoryProvider.createDir('projectA')
        createSettingsFile(projectA) << 'rootProject.name = "foo"'
        File projectB = directoryProvider.createDir('projectB')
        createSettingsFile(projectB) << 'rootProject.name = "foo"'

        when:
        def projects = getEclipseProjects(projectA, projectB)

        then:
        assertProjectNames projects, ['foo1', 'foo2']
    }

    def "Conflicting subprojects are prefixed with their parent"() {
        given:
        File projectA = directoryProvider.createDir('projectA')
        createSettingsFile(projectA, ['foo', 'foo:bar'])
        File projectB = directoryProvider.createDir('projectB')
        createSettingsFile(projectB, ['foo', 'foo:bar'])

        when:
        def projects = getEclipseProjects(projectA, projectB)

        then:
        assertProjectNames projects, ['projectA', 'projectB', 'projectA-foo', 'projectB-foo', 'projectA-foo-bar', 'projectB-foo-bar']
    }
    
    def "The de-duplicated name of the parent is used when prefixing children"() {
        given:
        File projectA = directoryProvider.createDir('projectA')
        createSettingsFile(projectA, ['foo']) << "rootProject.name = 'root'"
        File projectB = directoryProvider.createDir('projectB')
        createSettingsFile(projectB, ['foo']) << "rootProject.name = 'root'"

        when:
        def projects = getEclipseProjects(projectA, projectB)

        then:
        assertProjectNames projects, ['root1', 'root2', 'root1-foo', 'root2-foo']
    }

    def "Projects are not prefixed twice" () {
        given:
        File projectA = directoryProvider.createDir('projectA')
        createSettingsFile(projectA, ['foo', 'foo:foo-bar'])
        File projectB = directoryProvider.createDir('projectB')
        createSettingsFile(projectB, ['foo', 'foo:foo-bar'])

        when:
        def projects = getEclipseProjects(projectA, projectB)

        then:
        assertProjectNames projects, ['projectA', 'projectB', 'projectA-foo', 'projectB-foo', 'projectA-foo-bar', 'projectB-foo-bar']
    }

    def "The project hierarchy contains renamed projects" () {
        given:
        File projectA = directoryProvider.createDir('projectA')
        createSettingsFile(projectA, ['foo'])
        File projectB = directoryProvider.createDir('projectB')
        createSettingsFile(projectB, ['foo'])

        when:
        def projects = getEclipseProjects(projectA, projectB)
        def root = projects.find { result ->
            result.name == "projectA"
        }
        def sub = root.children.head()

        then:
        projects.contains(sub)
    }

    def "Project dependencies point to renamed projects"() {
        given:
        File projectA = directoryProvider.createDir('projectA')
        createBuildFile(projectA) << """
            ${javaBuildScript()}
            project('foo') {
                ${javaBuildScript()}
            }
            dependencies {
                compile project(':foo')
            }
        """
        createSettingsFile(projectA, ['foo'])
        File projectB = directoryProvider.createDir('projectB')
        createSettingsFile(projectB, ['foo'])

        when:
        def projects = getEclipseProjects(projectA, projectB)
        def root = projects.find { result ->
            result.name == "projectA"
        }
        def sub = root.children.head()
        def dependency = root.projectDependencies.head()

        then:
        dependency.targetProject == sub
    }

    private def getEclipseProjects(File... rootDirs) {
        def projects
        withCompositeConnection(rootDirs as List) { connection ->
            projects =  connection.getModels(EclipseProject).collect {result -> result.model}
        }
        return projects
    }

    private def void assertProjectNames(projects, names) {
        assert projects*.name as Set == names as Set
    }
}
