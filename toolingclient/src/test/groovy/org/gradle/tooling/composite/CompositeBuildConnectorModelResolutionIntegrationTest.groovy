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

    def "cannot create composite with multiple participating build that contain projects with the same name"() {
        given:
        File projectDir1 = directoryProvider.createDir('project-1/my-project')
        createBuildFileWithDependency(projectDir1, ExternalDependencies.COMMONS_LANG)
        File projectDir2 = directoryProvider.createDir('project-2/my-project')
        createBuildFileWithDependency(projectDir2, ExternalDependencies.LOG4J)

        when:
        CompositeBuildConnection compositeBuildConnection = createComposite(projectDir1, projectDir2)
        compositeBuildConnection.getModels(EclipseProject)

        then:
        Throwable t = thrown(IllegalStateException)
        t.message == "A composite build does not allow duplicate project names for any of the participating project. Offending project name: 'my-project'"

        cleanup:
        compositeBuildConnection.close()
    }

    def "creating a composite with second instance of same participating build only adds it once"() {
        given:
        File projectDir = directoryProvider.createDir('project')
        createBuildFileWithDependency(projectDir, ExternalDependencies.COMMONS_LANG)

        when:
        CompositeBuildConnection compositeBuildConnection = createComposite(projectDir, projectDir)
        Set<ModelResult<EclipseProject>> compositeModel = compositeBuildConnection.getModels(EclipseProject)

        then:
        compositeModel.size() == 1
        assertModelResult(compositeModel, 'project', ExternalDependencies.COMMONS_LANG)

        cleanup:
        compositeBuildConnection.close()
    }

    def "can create composite with single-project participating build"() {
        given:
        File projectDir = directoryProvider.createDir('project')
        createBuildFileWithDependency(projectDir, ExternalDependencies.COMMONS_LANG)

        when:
        CompositeBuildConnection compositeBuildConnection = createComposite(projectDir)
        Set<ModelResult<EclipseProject>> compositeModel = compositeBuildConnection.getModels(EclipseProject)

        then:
        compositeModel.size() == 1
        assertModelResult(compositeModel, 'project', ExternalDependencies.COMMONS_LANG)

        cleanup:
        compositeBuildConnection.close()
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
        assertModelResult(compositeModel, 'project')
        assertModelResult(compositeModel, 'sub-1', ExternalDependencies.COMMONS_LANG)
        assertModelResult(compositeModel, 'sub-2', ExternalDependencies.LOG4J)
        assertModelResult(compositeModel, 'a-1', ExternalDependencies.COMMONS_MATH)
        assertModelResult(compositeModel, 'b-2', ExternalDependencies.COMMONS_CODEC)

        cleanup:
        compositeBuildConnection.close()
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
        assertModelResult(compositeModel, 'project-1', ExternalDependencies.COMMONS_LANG)
        assertModelResult(compositeModel, 'project-2', ExternalDependencies.LOG4J)

        cleanup:
        compositeBuildConnection.close()
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
        CompositeBuildConnection compositeBuildConnection = createComposite(projectDir1, projectDir2)
        Set<ModelResult<EclipseProject>> compositeModel = compositeBuildConnection.getModels(EclipseProject)

        then:
        compositeModel.size() == 6
        assertModelResult(compositeModel, 'project-1')
        assertModelResult(compositeModel, 'project-2')
        assertModelResult(compositeModel, 'sub-1', ExternalDependencies.COMMONS_LANG)
        assertModelResult(compositeModel, 'sub-2', ExternalDependencies.LOG4J)
        assertModelResult(compositeModel, 'sub-a', ExternalDependencies.COMMONS_MATH)
        assertModelResult(compositeModel, 'sub-b', ExternalDependencies.COMMONS_CODEC)

        cleanup:
        compositeBuildConnection.close()
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
