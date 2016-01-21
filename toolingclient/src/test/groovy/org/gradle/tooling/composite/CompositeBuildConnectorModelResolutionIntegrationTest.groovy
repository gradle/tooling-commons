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

import org.gradle.tooling.model.eclipse.EclipseProject

class CompositeBuildConnectorModelResolutionIntegrationTest extends AbstractCompositeBuildConnectorIntegrationTest {

    def "can create composite with single-project participating build"() {
        given:
        File projectDir = directoryProvider.createDir('project')
        createBuildFileWithDependency(projectDir, ExternalDependencies.COMMONS_LANG)

        when:
        CompositeBuildConnection compositeBuildConnection = createComposite(projectDir)
        Set<ModelResult<EclipseProject>> compositeModel = compositeBuildConnection.getModels(EclipseProject)

        then:
        compositeModel.size() == 1
        EclipseProject eclipseProject = assertProjectInCompositeModel(compositeModel, 'project')
        assertExternalDependencies(eclipseProject, ExternalDependencies.COMMONS_LANG)
        assertNoProjectDependencies(eclipseProject)
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
        CompositeBuildConnection compositeBuildConnection = createComposite(rootProjectDir)
        Set<ModelResult<EclipseProject>> compositeModel = compositeBuildConnection.getModels(EclipseProject)

        then:
        compositeModel.size() == 5
        assertProjectInCompositeModel(compositeModel, 'project')
        EclipseProject eclipseProject1 = assertProjectInCompositeModel(compositeModel, 'sub-1')
        assertExternalDependencies(eclipseProject1, ExternalDependencies.COMMONS_LANG)
        assertNoProjectDependencies(eclipseProject1)
        EclipseProject eclipseProject2 = assertProjectInCompositeModel(compositeModel, 'sub-2')
        assertExternalDependencies(eclipseProject2, ExternalDependencies.LOG4J)
        assertNoProjectDependencies(eclipseProject2)
        EclipseProject eclipseProject3 = assertProjectInCompositeModel(compositeModel, 'a-1')
        assertExternalDependencies(eclipseProject3, ExternalDependencies.COMMONS_MATH)
        assertNoProjectDependencies(eclipseProject3)
        EclipseProject eclipseProject4 = assertProjectInCompositeModel(compositeModel, 'b-2')
        assertExternalDependencies(eclipseProject4, ExternalDependencies.COMMONS_CODEC)
        assertNoProjectDependencies(eclipseProject4)
    }

    def "can create composite with multiple, single-project participating builds"() {
        given:
        File projectDir1 = directoryProvider.createDir('project-1')
        createBuildFileWithDependency(projectDir1, ExternalDependencies.COMMONS_LANG)
        File projectDir2 = directoryProvider.createDir('project-2')
        createBuildFileWithDependency(projectDir2, ExternalDependencies.LOG4J)

        when:
        CompositeBuildConnection compositeBuildConnection = createComposite(projectDir1, projectDir2)
        Set<ModelResult<EclipseProject>> compositeModel = compositeBuildConnection.getModels(EclipseProject)

        then:
        compositeModel.size() == 2
        EclipseProject eclipseProject1 = assertProjectInCompositeModel(compositeModel, 'project-1')
        assertExternalDependencies(eclipseProject1, ExternalDependencies.COMMONS_LANG)
        assertNoProjectDependencies(eclipseProject1)
        EclipseProject eclipseProject2 = assertProjectInCompositeModel(compositeModel, 'project-2')
        assertExternalDependencies(eclipseProject2, ExternalDependencies.LOG4J)
        assertNoProjectDependencies(eclipseProject2)
    }

    def "can create composite with multiple, multi-project participating builds"() {
        given:
        File projectDir1 = directoryProvider.createDir('project-1')
        createBuildFileWithDependency(new File(projectDir1, 'sub-1'), ExternalDependencies.COMMONS_LANG)
        createBuildFileWithDependency(new File(projectDir1, 'sub-2'), ExternalDependencies.LOG4J)
        createSettingsFile(projectDir1, ['sub-1', 'sub-2'])

        File projectDir2 = directoryProvider.createDir('project-2')
        createBuildFileWithDependency(new File(projectDir2, 'sub-a'), ExternalDependencies.COMMONS_MATH)
        createBuildFileWithDependency(new File(projectDir2, 'sub-b'), ExternalDependencies.COMMONS_CODEC)
        createSettingsFile(projectDir2, ['sub-a', 'sub-b'])

        when:
        CompositeBuildConnection compositeBuildConnection = createComposite(projectDir1, projectDir2)
        Set<ModelResult<EclipseProject>> compositeModel = compositeBuildConnection.getModels(EclipseProject)

        then:
        compositeModel.size() == 6
        assertProjectInCompositeModel(compositeModel, 'project-1')
        assertProjectInCompositeModel(compositeModel, 'project-2')
        EclipseProject eclipseProject1 = assertProjectInCompositeModel(compositeModel, 'sub-1')
        assertExternalDependencies(eclipseProject1, ExternalDependencies.COMMONS_LANG)
        assertNoProjectDependencies(eclipseProject1)
        EclipseProject eclipseProject2 = assertProjectInCompositeModel(compositeModel, 'sub-2')
        assertExternalDependencies(eclipseProject2, ExternalDependencies.LOG4J)
        assertNoProjectDependencies(eclipseProject2)
        EclipseProject eclipseProject3 = assertProjectInCompositeModel(compositeModel, 'sub-a')
        assertExternalDependencies(eclipseProject3, ExternalDependencies.COMMONS_MATH)
        assertNoProjectDependencies(eclipseProject3)
        EclipseProject eclipseProject4 = assertProjectInCompositeModel(compositeModel, 'sub-b')
        assertExternalDependencies(eclipseProject4, ExternalDependencies.COMMONS_CODEC)
        assertNoProjectDependencies(eclipseProject4)
    }
}
