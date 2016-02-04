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

import org.gradle.tooling.composite.fixtures.ExternalDependencies
import org.gradle.tooling.composite.fixtures.ExternalDependency
import org.gradle.tooling.composite.fixtures.ProjectDependency
import org.gradle.tooling.model.GradleModuleVersion
import org.gradle.tooling.model.eclipse.EclipseProject

class CompositeBuildConnectorModelResolutionIntegrationTest extends AbstractCompositeBuildConnectorIntegrationTest {

    def "creating a composite with second instance of same participating build only adds it once"() {
        given:
        File projectDir = directoryProvider.createDir('project')
        createBuildFile(projectDir)

        when:
        Set<ModelResult<EclipseProject>> compositeModel = withCompositeConnection([projectDir, projectDir]) { connection ->
            connection.getModels(EclipseProject)
        }

        then:
        compositeModel.size() == 1
        assertModelResult(compositeModel, 'project', [])
    }

    def "creating a composite with second instance of project within the same participating, hierarchical build only adds it once"() {
        given:
        File rootProjectDir = directoryProvider.createDir('project')
        createBuildFile(rootProjectDir)
        File subProjectDir = new File(rootProjectDir, 'sub')
        createBuildFile(subProjectDir)
        File subSubProjectDir = new File(subProjectDir, 'sub-sub')
        createBuildFile(subSubProjectDir)
        createSettingsFile(rootProjectDir, ['sub', 'sub:sub-sub'])

        when:
        Set<ModelResult<EclipseProject>> compositeModel = withCompositeConnection([rootProjectDir, subProjectDir]) { connection ->
            connection.getModels(EclipseProject)
        }

        then:
        compositeModel.size() == 3
        assertModelResult(compositeModel, 'project', ['sub'])
        assertModelResult(compositeModel, 'sub', ['sub-sub'])
        assertModelResult(compositeModel, 'sub-sub', [])
    }

    def "can create composite with single-project participating build"() {
        given:
        File projectDir = directoryProvider.createDir('project')
        createBuildFileWithExternalDependency(projectDir, ExternalDependencies.COMMONS_LANG)

        when:
        Set<ModelResult<EclipseProject>> compositeModel = withCompositeConnection([projectDir]) { connection ->
            connection.getModels(EclipseProject)
        }

        then:
        compositeModel.size() == 1
        assertModelResult(compositeModel, 'project', [], [ExternalDependencies.COMMONS_LANG])
    }

    def "can create composite with single, hierarchical multi-project participating build"() {
        given:
        File rootProjectDir = directoryProvider.createDir('project')
        File subProjectDir1 = new File(rootProjectDir, 'sub-1')
        File subProjectDir2 = new File(rootProjectDir, 'sub-2')
        File subSubProjectDir1 = new File(subProjectDir1, 'a-1')
        File subSubProjectDir2 = new File(subProjectDir2, 'b-2')
        createBuildFileWithExternalDependency(subProjectDir1, ExternalDependencies.COMMONS_LANG)
        createBuildFileWithExternalDependency(subProjectDir2, ExternalDependencies.LOG4J)
        createBuildFileWithExternalDependency(subSubProjectDir1, ExternalDependencies.COMMONS_MATH)
        createBuildFileWithExternalDependency(subSubProjectDir2, ExternalDependencies.COMMONS_CODEC)
        createSettingsFile(rootProjectDir, ['sub-1', 'sub-2', 'sub-1:a-1', 'sub-2:b-2'])

        when:
        Set<ModelResult<EclipseProject>> compositeModel = withCompositeConnection([rootProjectDir]) { connection ->
            connection.getModels(EclipseProject)
        }

        then:
        compositeModel.size() == 5
        assertModelResult(compositeModel, 'project', ['sub-1', 'sub-2'])
        assertModelResult(compositeModel, 'sub-1', ['a-1'], [ExternalDependencies.COMMONS_LANG])
        assertModelResult(compositeModel, 'sub-2', ['b-2'], [ExternalDependencies.LOG4J])
        assertModelResult(compositeModel, 'a-1', [], [ExternalDependencies.COMMONS_MATH])
        assertModelResult(compositeModel, 'b-2', [], [ExternalDependencies.COMMONS_CODEC])
    }

    def "can create composite with single, flat multi-project participating build"() {
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
        Set<ModelResult<EclipseProject>> compositeModel = withCompositeConnection([rootProjectDir]) { connection ->
            connection.getModels(EclipseProject)
        }

        then:
        compositeModel.size() == 4
        assertModelResult(compositeModel, 'master', ['sub-1', 'sub-2', 'sub-3'])
        assertModelResult(compositeModel, 'sub-1', [])
        assertModelResult(compositeModel, 'sub-2', [])
        assertModelResult(compositeModel, 'sub-3', [])
    }

    def "can create composite with multiple, single-project participating builds"() {
        given:
        File projectDir1 = directoryProvider.createDir('project-1')
        createBuildFileWithExternalDependency(projectDir1, ExternalDependencies.COMMONS_LANG)
        File projectDir2 = directoryProvider.createDir('project-2')
        createBuildFileWithExternalDependency(projectDir2, ExternalDependencies.LOG4J)

        when:
        Set<ModelResult<EclipseProject>> compositeModel = withCompositeConnection([projectDir1, projectDir2]) { connection ->
            connection.getModels(EclipseProject)
        }

        then:
        compositeModel.size() == 2
        assertModelResult(compositeModel, 'project-1', [], [ExternalDependencies.COMMONS_LANG])
        assertModelResult(compositeModel, 'project-2', [], [ExternalDependencies.LOG4J])
    }

    def "can create composite with multiple, multi-project, hierarchical participating builds"() {
        given:
        File projectDir1 = directoryProvider.createDir('project-1')
        File sub1ProjectDir = new File(projectDir1, 'sub-1')
        createBuildFileWithExternalDependency(sub1ProjectDir, ExternalDependencies.COMMONS_LANG)
        File sub2ProjectDir = new File(projectDir1, 'sub-2')
        createBuildFileWithExternalDependency(sub2ProjectDir, ExternalDependencies.LOG4J)
        createSettingsFile(projectDir1, ['sub-1', 'sub-2'])

        File projectDir2 = directoryProvider.createDir('project-2')
        File subAProjectDir = new File(projectDir2, 'sub-a')
        createBuildFileWithExternalDependency(subAProjectDir, ExternalDependencies.COMMONS_MATH)
        File subBProjectDir = new File(projectDir2, 'sub-b')
        createBuildFileWithExternalDependency(subBProjectDir, ExternalDependencies.COMMONS_CODEC)
        createSettingsFile(projectDir2, ['sub-a', 'sub-b'])

        when:
        Set<ModelResult<EclipseProject>> compositeModel = withCompositeConnection([projectDir1, projectDir2]) { connection ->
            connection.getModels(EclipseProject)
        }

        then:
        compositeModel.size() == 6
        assertModelResult(compositeModel, 'project-1', ['sub-1', 'sub-2'])
        assertModelResult(compositeModel, 'project-2', ['sub-a', 'sub-b'])
        assertModelResult(compositeModel, 'sub-1', [], [ExternalDependencies.COMMONS_LANG])
        assertModelResult(compositeModel, 'sub-2', [], [ExternalDependencies.LOG4J])
        assertModelResult(compositeModel, 'sub-a', [], [ExternalDependencies.COMMONS_MATH])
        assertModelResult(compositeModel, 'sub-b', [], [ExternalDependencies.COMMONS_CODEC])
    }

    def "can create composite with participating build declaring project dependencies"() {
        given:
        File rootProjectDir = directoryProvider.createDir('project')
        createBuildFile(rootProjectDir) << """
            subprojects {
                apply plugin: 'java'
            }
        """
        File sub1ProjectDir = new File(rootProjectDir, 'sub-1')
        createBuildFile(sub1ProjectDir)
        File sub2ProjectDir = new File(rootProjectDir, 'sub-2')
        createBuildFile(sub2ProjectDir) << """
            dependencies {
                compile project(':sub-1')
            }
        """
        File subSubProjectDir = new File(sub2ProjectDir, 'sub-sub')
        createBuildFile(subSubProjectDir) << """
            dependencies {
                compile project(':sub-2')
            }
        """
        createSettingsFile(rootProjectDir, ['sub-1', 'sub-2', 'sub-2:sub-sub'])

        when:
        Set<ModelResult<EclipseProject>> compositeModel = withCompositeConnection([rootProjectDir, sub1ProjectDir]) { connection ->
            connection.getModels(EclipseProject)
        }

        then:
        compositeModel.size() == 4
        assertModelResult(compositeModel, 'project', ['sub-1', 'sub-2'])
        assertModelResult(compositeModel, 'sub-1', [])
        assertModelResult(compositeModel, 'sub-2', ['sub-sub'], [], [new ProjectDependency('sub-1')])
        assertModelResult(compositeModel, 'sub-sub', [], [], [new ProjectDependency('sub-1'), new ProjectDependency('sub-2')])
    }

    private EclipseProject assertModelResult(Set<ModelResult<EclipseProject>> compositeModel, String projectName,
                                             List<String> childrenProjectNames,
                                             List<ExternalDependency> externalDependencies = [],
                                             List<ProjectDependency> projectDependencies = []) {
        EclipseProject eclipseProject = assertEclipseProjectInCompositeModel(compositeModel, projectName)
        assertChildren(eclipseProject, childrenProjectNames)
        assertExternalDependencies(eclipseProject, externalDependencies)
        assertProjectDependencies(eclipseProject, projectDependencies)
        eclipseProject
    }

    private EclipseProject assertEclipseProjectInCompositeModel(Set<ModelResult<EclipseProject>> compositeModel, String projectName) {
        ModelResult<EclipseProject> modelResult = compositeModel.find { it.model.name == projectName }
        EclipseProject eclipseProject = modelResult.model
        assert eclipseProject
        eclipseProject
    }

    private void assertChildren(EclipseProject eclipseProject, List<String> childrenProjectNames) {
        assert eclipseProject.children.size() == childrenProjectNames.size()

        eclipseProject.children.each {
            assert childrenProjectNames.contains(it.name)
        }
    }

    private void assertExternalDependencies(EclipseProject eclipseProject, List<ExternalDependency> externalDependencies) {
        assert eclipseProject.classpath.size() == externalDependencies.size()
        List<GradleModuleVersion> moduleVersions = eclipseProject.classpath.collect { it.gradleModuleVersion }

        externalDependencies.each { externalDependency ->
            assert moduleVersions.find {
                it.group == externalDependency.group && it.name == externalDependency.name && it.version == externalDependency.version
            }
        }
    }

    private void assertProjectDependencies(EclipseProject eclipseProject, List<ProjectDependency> projectDependencies) {
        assert eclipseProject.projectDependencies.size() == projectDependencies.size()

        projectDependencies.each { projectDependency ->
            assert eclipseProject.projectDependencies.find {
                it.path == projectDependency.path
            }
        }
    }
}
