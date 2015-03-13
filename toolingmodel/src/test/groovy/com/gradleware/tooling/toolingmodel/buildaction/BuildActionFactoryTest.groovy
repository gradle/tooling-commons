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

package com.gradleware.tooling.toolingmodel.buildaction

import com.gradleware.tooling.toolingmodel.util.Pair
import com.gradleware.tooling.toolingmodel.util.Triple
import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.ToolingModelToolingClientSpecification
import com.gradleware.tooling.toolingclient.BuildActionRequest
import org.gradle.tooling.model.GradleProject
import org.gradle.tooling.model.gradle.BuildInvocations
import org.gradle.tooling.model.gradle.ProjectPublications
import org.junit.Rule

class BuildActionFactoryTest extends ToolingModelToolingClientSpecification {

  @Rule
  TestDirectoryProvider directoryProvider = new TestDirectoryProvider();

  def setup() {
    directoryProvider.createFile('settings.gradle');
    directoryProvider.createFile('build.gradle') << 'task myTask {}'
  }

  def "ModelForSingleProjectBuildAction"() {
    setup:
    ModelForSingleProjectBuildAction<BuildInvocations> action = BuildActionFactory.getModelForProject(":", BuildInvocations.class)
    BuildActionRequest<BuildInvocations> buildActionRequest = toolingClient.newBuildActionRequest(action)
    buildActionRequest.projectDir(directoryProvider.testDirectory)
    BuildInvocations buildInvocations = buildActionRequest.executeAndWait()
    assert buildInvocations != null
  }

  def "ModelForAllProjectsBuildAction"() {
    setup:
    ModelForAllProjectsBuildAction<BuildInvocations> action = BuildActionFactory.getModelForAllProjects(BuildInvocations.class)
    BuildActionRequest<Map<String, BuildInvocations>> buildActionRequest = toolingClient.newBuildActionRequest(action)
    buildActionRequest.projectDir(directoryProvider.testDirectory)
    Map<String, BuildInvocations> buildInvocationsMap = buildActionRequest.executeAndWait()
    assert buildInvocationsMap != null
  }

  def "GlobalModelBuildAction"() {
    setup:
    GlobalModelBuildAction<GradleProject> action = BuildActionFactory.getBuildModel(GradleProject.class)
    BuildActionRequest<GradleProject> buildActionRequest = toolingClient.newBuildActionRequest(action)
    buildActionRequest.projectDir(directoryProvider.testDirectory)
    GradleProject gradleProject = buildActionRequest.executeAndWait()
    assert gradleProject != null
  }

  def "DoubleBuildAction"() {
    setup:
    def someAction = BuildActionFactory.getModelForProject(':', BuildInvocations.class)
    def anotherAction = BuildActionFactory.getModelForAllProjects(BuildInvocations.class)
    DoubleBuildAction<BuildInvocations, Map<String, BuildInvocations>> action = BuildActionFactory.getPairResult(someAction, anotherAction)
    BuildActionRequest<Pair<BuildInvocations, Map<String, BuildInvocations>>> buildActionRequest = toolingClient.newBuildActionRequest(action)
    buildActionRequest.projectDir(directoryProvider.testDirectory)
    Pair<BuildInvocations, Map<String, BuildInvocations>> result = buildActionRequest.executeAndWait()
    assert result != null
    assert result.getFirst() != null
    assert result.getSecond() != null
  }

  def "TripleBuildAction"() {
    setup:
    def someAction = BuildActionFactory.getModelForProject(':', BuildInvocations.class)
    def anotherAction = BuildActionFactory.getModelForAllProjects(BuildInvocations.class)
    def yetAnotherAction = BuildActionFactory.getModelForAllProjects(ProjectPublications.class)
    TripleBuildAction<BuildInvocations, Map<String, BuildInvocations>, Map<String, ProjectPublications>> action = BuildActionFactory.getTripleResult(someAction, anotherAction, yetAnotherAction)
    BuildActionRequest<Triple<BuildInvocations, Map<String, BuildInvocations>, Map<String, ProjectPublications>>> buildActionRequest = toolingClient.newBuildActionRequest(action)
    buildActionRequest.projectDir(directoryProvider.testDirectory)
    Triple<BuildInvocations, Map<String, BuildInvocations>, Map<String, ProjectPublications>> result = buildActionRequest.executeAndWait()
    assert result != null
    assert result.getFirst() != null
    assert result.getSecond() != null
    assert result.getThird() != null
  }

}
