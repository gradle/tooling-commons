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
import org.gradle.tooling.model.eclipse.EclipseProject

class CompositeBuildConnectorModelResolutionIntegrationTest extends AbstractCompositeBuildConnectorIntegrationTest {

    def "creating a composite with second instance of same participating build only adds it once"() {
        given:
        File projectDir = directoryProvider.createDir('project')
        createBuildFile(projectDir)

        when:
        Set<ModelResult<EclipseProject>> compositeModel

        withCompositeConnection([projectDir, projectDir]) { connection ->
            compositeModel = connection.getModels(EclipseProject)
        }

        then:
        compositeModel.size() == 1
        EclipseProject rootProject = assertModelResult(compositeModel, 'project')
        assertChildren(rootProject, [])
    }

    def "creating a composite with second instance of project within the same participating build only adds it once"() {
        given:
        File rootProjectDir = directoryProvider.createDir('project')
        createBuildFile(rootProjectDir)
        File subProjectDir = new File(rootProjectDir, 'sub')
        createBuildFile(subProjectDir)
        File subSubProjectDir = new File(subProjectDir, 'sub-sub')
        createBuildFile(subSubProjectDir)
        createSettingsFile(rootProjectDir, ['sub', 'sub:sub-sub'])

        when:
        Set<ModelResult<EclipseProject>> compositeModel

        withCompositeConnection([rootProjectDir, subProjectDir]) { connection ->
            compositeModel = connection.getModels(EclipseProject)
        }

        then:
        compositeModel.size() == 3
        EclipseProject rootProject = assertModelResult(compositeModel, 'project')
        assertChildren(rootProject, [subProjectDir])
        EclipseProject subProject = assertModelResult(compositeModel, 'sub')
        assertChildren(subProject, [subSubProjectDir])
        EclipseProject subSubProject = assertModelResult(compositeModel, 'sub-sub')
        assertChildren(subSubProject, [])
    }

    def "can create composite with single-project participating build"() {
        given:
        File projectDir = directoryProvider.createDir('project')
        createBuildFileWithDependency(projectDir, ExternalDependencies.COMMONS_LANG)

        when:
        Set<ModelResult<EclipseProject>> compositeModel

        withCompositeConnection([projectDir]) { connection ->
            compositeModel = connection.getModels(EclipseProject)
        }

        then:
        compositeModel.size() == 1
        EclipseProject rootProject = assertModelResult(compositeModel, 'project', ExternalDependencies.COMMONS_LANG)
        assertChildren(rootProject, [])
    }

    def "can create composite with single, multi-project participating build"() {
        given:
        File rootProjectDir = directoryProvider.createDir('project')
        File subProjectDir1 = new File(rootProjectDir, 'sub-1')
        File subProjectDir2 = new File(rootProjectDir, 'sub-2')
        File subSubProjectDir1 = new File(subProjectDir1, 'a-1')
        File subSubProjectDir2 = new File(subProjectDir2, 'b-2')
        createBuildFileWithDependency(subProjectDir1, ExternalDependencies.COMMONS_LANG)
        createBuildFileWithDependency(subProjectDir2, ExternalDependencies.LOG4J)
        createBuildFileWithDependency(subSubProjectDir1, ExternalDependencies.COMMONS_MATH)
        createBuildFileWithDependency(subSubProjectDir2, ExternalDependencies.COMMONS_CODEC)
        createSettingsFile(rootProjectDir, ['sub-1', 'sub-2', 'sub-1:a-1', 'sub-2:b-2'])

        when:
        Set<ModelResult<EclipseProject>> compositeModel

        withCompositeConnection([rootProjectDir]) { connection ->
            compositeModel = connection.getModels(EclipseProject)
        }

        then:
        compositeModel.size() == 5
        EclipseProject rootProject = assertModelResult(compositeModel, 'project')
        assertChildren(rootProject, [subProjectDir1, subProjectDir2])
        EclipseProject sub1Project = assertModelResult(compositeModel, 'sub-1', ExternalDependencies.COMMONS_LANG)
        assertChildren(sub1Project, [subSubProjectDir1])
        EclipseProject sub2Project = assertModelResult(compositeModel, 'sub-2', ExternalDependencies.LOG4J)
        assertChildren(sub2Project, [subSubProjectDir2])
        EclipseProject a1Project = assertModelResult(compositeModel, 'a-1', ExternalDependencies.COMMONS_MATH)
        assertChildren(a1Project, [])
        EclipseProject a2Project = assertModelResult(compositeModel, 'b-2', ExternalDependencies.COMMONS_CODEC)
        assertChildren(a2Project, [])
    }

    def "can create composite with multiple, single-project participating builds"() {
        given:
        File projectDir1 = directoryProvider.createDir('project-1')
        createBuildFileWithDependency(projectDir1, ExternalDependencies.COMMONS_LANG)
        File projectDir2 = directoryProvider.createDir('project-2')
        createBuildFileWithDependency(projectDir2, ExternalDependencies.LOG4J)

        when:
        Set<ModelResult<EclipseProject>> compositeModel

        withCompositeConnection([projectDir1, projectDir2]) { connection ->
            compositeModel = connection.getModels(EclipseProject)
        }

        then:
        compositeModel.size() == 2
        EclipseProject project1 = assertModelResult(compositeModel, 'project-1', ExternalDependencies.COMMONS_LANG)
        assertChildren(project1, [])
        EclipseProject project2 = assertModelResult(compositeModel, 'project-2', ExternalDependencies.LOG4J)
        assertChildren(project2, [])
    }

    def "can create composite with multiple, multi-project participating builds"() {
        given:
        File projectDir1 = directoryProvider.createDir('project-1')
        File sub1ProjectDir = new File(projectDir1, 'sub-1')
        createBuildFileWithDependency(sub1ProjectDir, ExternalDependencies.COMMONS_LANG)
        File sub2ProjectDir = new File(projectDir1, 'sub-2')
        createBuildFileWithDependency(sub2ProjectDir, ExternalDependencies.LOG4J)
        createSettingsFile(projectDir1, ['sub-1', 'sub-2'])

        File projectDir2 = directoryProvider.createDir('project-2')
        File subAProjectDir = new File(projectDir2, 'sub-a')
        createBuildFileWithDependency(subAProjectDir, ExternalDependencies.COMMONS_MATH)
        File subBProjectDir = new File(projectDir2, 'sub-b')
        createBuildFileWithDependency(subBProjectDir, ExternalDependencies.COMMONS_CODEC)
        createSettingsFile(projectDir2, ['sub-a', 'sub-b'])

        when:
        Set<ModelResult<EclipseProject>> compositeModel

        withCompositeConnection([projectDir1, projectDir2]) { connection ->
            compositeModel = connection.getModels(EclipseProject)
        }

        then:
        compositeModel.size() == 6
        EclipseProject project1 = assertModelResult(compositeModel, 'project-1')
        assertChildren(project1, [sub1ProjectDir, sub2ProjectDir])
        EclipseProject project2 = assertModelResult(compositeModel, 'project-2')
        assertChildren(project2, [subAProjectDir, subBProjectDir])
        EclipseProject sub1Project = assertModelResult(compositeModel, 'sub-1', ExternalDependencies.COMMONS_LANG)
        assertChildren(sub1Project, [])
        EclipseProject sub2Project = assertModelResult(compositeModel, 'sub-2', ExternalDependencies.LOG4J)
        assertChildren(sub2Project, [])
        EclipseProject subAProject = assertModelResult(compositeModel, 'sub-a', ExternalDependencies.COMMONS_MATH)
        assertChildren(subAProject, [])
        EclipseProject subBProject = assertModelResult(compositeModel, 'sub-b', ExternalDependencies.COMMONS_CODEC)
        assertChildren(subBProject, [])
    }

    private EclipseProject assertModelResult(Set<ModelResult<EclipseProject>> compositeModel, String projectName,
                                                          ExternalDependency... externalDependencies) {
        EclipseProject eclipseProject = assertEclipseProjectInCompositeModel(compositeModel, projectName)
        assertExternalDependencies(eclipseProject, externalDependencies)
        assertNoProjectDependencies(eclipseProject)
        eclipseProject
    }
}
