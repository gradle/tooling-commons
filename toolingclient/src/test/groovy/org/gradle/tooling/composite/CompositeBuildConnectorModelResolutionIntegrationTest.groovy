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
        assertModelResult(compositeModel, 'project')
    }

    def "creating a composite with second instance of project within the same participating build only adds it once"() {
        given:
        File rootProjectDir = directoryProvider.createDir('project')
        createBuildFile(rootProjectDir)
        File subProject = new File(rootProjectDir, 'sub')
        createBuildFile(subProject)
        File subSubProject = new File(subProject, 'sub-sub')
        createBuildFile(subSubProject)
        createSettingsFile(rootProjectDir, ['sub', 'sub:sub-sub'])

        when:
        Set<ModelResult<EclipseProject>> compositeModel

        withCompositeConnection([rootProjectDir, subProject]) { connection ->
            compositeModel = connection.getModels(EclipseProject)
        }

        then:
        compositeModel.size() == 3
        assertModelResult(compositeModel, 'project')
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
        assertModelResult(compositeModel, 'project', ExternalDependencies.COMMONS_LANG)
    }

    def "can create composite with single, multi-project participating build"() {
        given:
        File rootProjectDir = directoryProvider.createDir('project')
        File subProject1 = new File(rootProjectDir, 'sub-1')
        File subProject2 = new File(rootProjectDir, 'sub-2')
        File subSubProject1 = new File(subProject1, 'a-1')
        File subSubProject2 = new File(subProject2, 'b-2')
        createBuildFileWithDependency(subProject1, ExternalDependencies.COMMONS_LANG)
        createBuildFileWithDependency(subProject2, ExternalDependencies.LOG4J)
        createBuildFileWithDependency(subSubProject1, ExternalDependencies.COMMONS_MATH)
        createBuildFileWithDependency(subSubProject2, ExternalDependencies.COMMONS_CODEC)
        createSettingsFile(rootProjectDir, ['sub-1', 'sub-2', 'sub-1:a-1', 'sub-2:b-2'])

        when:
        Set<ModelResult<EclipseProject>> compositeModel

        withCompositeConnection([rootProjectDir]) { connection ->
            compositeModel = connection.getModels(EclipseProject)
        }

        then:
        compositeModel.size() == 5
        assertModelResult(compositeModel, 'project')
        assertModelResult(compositeModel, 'sub-1', ExternalDependencies.COMMONS_LANG)
        assertModelResult(compositeModel, 'sub-2', ExternalDependencies.LOG4J)
        assertModelResult(compositeModel, 'a-1', ExternalDependencies.COMMONS_MATH)
        assertModelResult(compositeModel, 'b-2', ExternalDependencies.COMMONS_CODEC)
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
        assertModelResult(compositeModel, 'project-1', ExternalDependencies.COMMONS_LANG)
        assertModelResult(compositeModel, 'project-2', ExternalDependencies.LOG4J)
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
        assertModelResult(compositeModel, 'project-1')
        assertModelResult(compositeModel, 'project-2')
        assertModelResult(compositeModel, 'sub-1', ExternalDependencies.COMMONS_LANG)
        assertModelResult(compositeModel, 'sub-2', ExternalDependencies.LOG4J)
        assertModelResult(compositeModel, 'sub-a', ExternalDependencies.COMMONS_MATH)
        assertModelResult(compositeModel, 'sub-b', ExternalDependencies.COMMONS_CODEC)
    }

    private ModelResult<EclipseProject> assertModelResult(Set<ModelResult<EclipseProject>> compositeModel, String projectName,
                                                          ExternalDependency... externalDependencies) {
        ModelResult<EclipseProject> modelResult = assertModelResultInCompositeModel(compositeModel, projectName)
        EclipseProject eclipseProject = modelResult.model
        assert eclipseProject
        assertExternalDependencies(eclipseProject, externalDependencies)
        assertNoProjectDependencies(eclipseProject)
        modelResult
    }
}
