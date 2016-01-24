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
        ModelResult<EclipseProject> modelResult = assertModelResult(compositeModel, 'project', ExternalDependencies.COMMONS_LANG)
        assert modelResult.project.projectDirectory == projectDir
        assert !modelResult.project.parent
        assert modelResult.project.children.empty

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
        ModelResult<EclipseProject> modelResultRoot = assertModelResult(compositeModel, 'project')
        assert modelResultRoot.project.projectDirectory == rootProjectDir
        assert !modelResultRoot.project.parent
        assert modelResultRoot.project.children.collect { it.projectDirectory } == [subProject1, subProject2]
        ModelResult<EclipseProject> modelResultSub1 = assertModelResult(compositeModel, 'sub-1', ExternalDependencies.COMMONS_LANG)
        assert modelResultSub1.project.projectDirectory == subProject1
        assert modelResultSub1.project.parent == modelResultRoot.project
        assert modelResultSub1.project.children.collect { it.projectDirectory } == [subSubProject1]
        ModelResult<EclipseProject> modelResultSub2 = assertModelResult(compositeModel, 'sub-2', ExternalDependencies.LOG4J)
        assert modelResultSub2.project.projectDirectory == subProject2
        assert modelResultSub2.project.parent == modelResultRoot.project
        assert modelResultSub2.project.children.collect { it.projectDirectory } == [subSubProject2]
        ModelResult<EclipseProject> modelResultSubA1 = assertModelResult(compositeModel, 'a-1', ExternalDependencies.COMMONS_MATH)
        assert modelResultSubA1.project.projectDirectory == subSubProject1
        assert modelResultSubA1.project.parent == modelResultSub1.project
        assert modelResultSubA1.project.children.empty
        ModelResult<EclipseProject> modelResultSubA2 = assertModelResult(compositeModel, 'b-2', ExternalDependencies.COMMONS_CODEC)
        assert modelResultSubA2.project.projectDirectory == subSubProject2
        assert modelResultSubA2.project.parent == modelResultSub2.project
        assert modelResultSubA2.project.children.empty

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
        ModelResult<EclipseProject> modelResultRoot1 = assertModelResult(compositeModel, 'project-1', ExternalDependencies.COMMONS_LANG)
        assert modelResultRoot1.project.projectDirectory == projectDir1
        assert !modelResultRoot1.project.parent
        assert modelResultRoot1.project.children.empty
        ModelResult<EclipseProject> modelResultRoot2 = assertModelResult(compositeModel, 'project-2', ExternalDependencies.LOG4J)
        assert modelResultRoot2.project.projectDirectory == projectDir2
        assert !modelResultRoot2.project.parent
        assert modelResultRoot2.project.children.empty

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
        ModelResult<EclipseProject> modelResultRoot1 = assertModelResult(compositeModel, 'project-1')
        assert modelResultRoot1.project.projectDirectory == projectDir1
        assert !modelResultRoot1.project.parent
        assert modelResultRoot1.project.children.collect { it.projectDirectory } == [sub1ProjectDir, sub2ProjectDir]
        ModelResult<EclipseProject> modelResultRoot2 = assertModelResult(compositeModel, 'project-2')
        assert modelResultRoot2.project.projectDirectory == projectDir2
        assert !modelResultRoot2.project.parent
        assert modelResultRoot2.project.children.collect { it.projectDirectory } == [subAProjectDir, subBProjectDir]
        ModelResult<EclipseProject> modelResultSub1 = assertModelResult(compositeModel, 'sub-1', ExternalDependencies.COMMONS_LANG)
        assert modelResultSub1.project.projectDirectory == sub1ProjectDir
        assert modelResultSub1.project.parent == modelResultRoot1.project
        assert modelResultSub1.project.children.empty
        ModelResult<EclipseProject> modelResultSub2 = assertModelResult(compositeModel, 'sub-2', ExternalDependencies.LOG4J)
        assert modelResultSub2.project.projectDirectory == sub2ProjectDir
        assert modelResultSub2.project.parent == modelResultRoot1.project
        assert modelResultSub2.project.children.empty
        ModelResult<EclipseProject> modelResultSubA = assertModelResult(compositeModel, 'sub-a', ExternalDependencies.COMMONS_MATH)
        assert modelResultSubA.project.projectDirectory == subAProjectDir
        assert modelResultSubA.project.parent == modelResultRoot2.project
        assert modelResultSubA.project.children.empty
        ModelResult<EclipseProject> modelResultSubB = assertModelResult(compositeModel, 'sub-b', ExternalDependencies.COMMONS_CODEC)
        assert modelResultSubB.project.projectDirectory == subBProjectDir
        assert modelResultSubB.project.parent == modelResultRoot2.project
        assert modelResultSubB.project.children.empty

        cleanup:
        compositeBuildConnection.close()
    }

    private ModelResult<EclipseProject> assertModelResult(Set<ModelResult<EclipseProject>> compositeModel, String projectName,
                                                          ExternalDependency... externalDependencies) {
        ModelResult<EclipseProject> modelResult = assertModelResultInCompositeModel(compositeModel, projectName)
        EclipseProject eclipseProject = modelResult.model
        assertExternalDependencies(eclipseProject, externalDependencies)
        assertNoProjectDependencies(eclipseProject)
        modelResult
    }
}
