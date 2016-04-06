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

package com.gradleware.tooling.toolingmodel.repository.internal

import com.google.common.collect.ImmutableList
import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.ToolingModelToolingClientSpecification
import com.gradleware.tooling.spock.VerboseUnroll
import com.gradleware.tooling.testing.GradleVersionParameterization
import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.OmniBuildInvocations
import com.gradleware.tooling.toolingmodel.OmniProjectTask
import com.gradleware.tooling.toolingmodel.OmniTaskSelector
import com.gradleware.tooling.toolingmodel.Path
import com.gradleware.tooling.toolingmodel.util.Maybe

import org.gradle.tooling.model.GradleProject
import org.junit.Rule

@VerboseUnroll(formatter = GradleDistributionFormatter.class)
class DefaultOmniBuildInvocationsContainerBuilderTest extends ToolingModelToolingClientSpecification {

    @Rule
    public TestDirectoryProvider directoryProvider = new TestDirectoryProvider();

    @Rule
    public TestDirectoryProvider directoryProviderProjectsWithoutTasks = new TestDirectoryProvider();

    def setup() {
        directoryProvider.createFile('settings.gradle') << '''
      rootProject.name = 'TestProject'
      include 'sub1'
      include 'sub2'
      include 'sub2:subSub'
    '''

        directoryProvider.createFile('build.gradle') << '''
       task alpha {
         description = 'ALPHA'
         group = 'build'
       }
       task beta {}
    '''

        directoryProvider.createDir('sub1')
        directoryProvider.createFile('sub1', 'build.gradle') << '''
       task beta {
         description = 'BETA'
         group = 'build'
       }
       task gamma {}
       task epsilon {}
    '''

        directoryProvider.createDir('sub2')
        directoryProvider.createFile('sub2', 'build.gradle') << '''
       task beta {}
       task delta { description = 'DELTA' }
       task epsilon { group = 'build' }
    '''

        directoryProvider.createDir('sub2', 'subSub')
        directoryProvider.createFile('sub2', 'subSub', 'build.gradle') << '''
       task alpha { description = 'ALPHA_SUB2' }
       task delta { description = 'DELTA_SUB2' }
       task zeta {}
    '''

        directoryProviderProjectsWithoutTasks.createFile('settings.gradle') << '''
       include 'sub1'
       include 'sub2'
     '''

        directoryProviderProjectsWithoutTasks.createDir('sub1')
        directoryProviderProjectsWithoutTasks.createDir('sub2')
    }

    def "convertFromGradleProject"() {
        given:
        def modelRequest = toolingClient.newModelRequest(GradleProject.class)
        modelRequest.projectDir(directoryProvider.testDirectory)
        def gradleProject = modelRequest.executeAndWait()

        when:
        DefaultOmniBuildInvocationsContainer buildInvocations = DefaultOmniBuildInvocationsContainerBuilder.build(gradleProject, false)

        then:
        buildInvocations != null
        buildInvocations.asMap().keySet() == [Path.from(':'), Path.from(':sub1'), Path.from(':sub2'), Path.from(':sub2:subSub')] as Set

        OmniBuildInvocations invocationsAtRoot = buildInvocations.get(Path.from(':')).get()
        collectNamesOfNonImplicitTaskSelectors(invocationsAtRoot.taskSelectors) == ['alpha', 'beta', 'gamma', 'delta', 'epsilon', 'zeta'] as Set
        assertTaskSelector('alpha', 'ALPHA', true, Maybe.of("build"), [':alpha', ':sub2:subSub:alpha'], invocationsAtRoot.taskSelectors)
        assertTaskSelector('beta', null, true, Maybe.of(null), [':beta', ':sub1:beta', ':sub2:beta'], invocationsAtRoot.taskSelectors)
        assertTaskSelector('gamma', null, false, Maybe.of(null), [':sub1:gamma'], invocationsAtRoot.taskSelectors)
        assertTaskSelector('delta', 'DELTA', false, Maybe.of(null), [':sub2:delta', ':sub2:subSub:delta'], invocationsAtRoot.taskSelectors)
        assertTaskSelector('epsilon', null, true, Maybe.of(null), [':sub1:epsilon', ':sub2:epsilon'], invocationsAtRoot.taskSelectors)
        assertTaskSelector('zeta', null, false, Maybe.of(null), [':sub2:subSub:zeta'], invocationsAtRoot.taskSelectors)

        collectNamesOfNonImplicitProjectTasks(invocationsAtRoot.projectTasks) == ['alpha', 'beta'] as Set
        assertTask('alpha', 'ALPHA', true, ':alpha', Maybe.of("build"), invocationsAtRoot.projectTasks)
        assertTask('beta', null, false, ':beta', Maybe.of(null), invocationsAtRoot.projectTasks)

        OmniBuildInvocations invocationsAtSub1 = buildInvocations.get(Path.from(':sub1')).get()
        collectNamesOfNonImplicitTaskSelectors(invocationsAtSub1.taskSelectors) == ['beta', 'gamma', 'epsilon'] as Set
        assertTaskSelector('beta', 'BETA', true, Maybe.of("build"), [':sub1:beta'], invocationsAtSub1.taskSelectors)
        assertTaskSelector('gamma', null, false, Maybe.of(null), [':sub1:gamma'], invocationsAtSub1.taskSelectors)
        assertTaskSelector('epsilon', null, false, Maybe.of(null), [':sub1:epsilon'], invocationsAtSub1.taskSelectors)

        collectNamesOfNonImplicitProjectTasks(invocationsAtSub1.projectTasks) == ['beta', 'gamma', 'epsilon'] as Set
        assertTask('beta', 'BETA', true, ':sub1:beta', Maybe.of("build"), invocationsAtSub1.projectTasks)
        assertTask('gamma', null, false, ':sub1:gamma', Maybe.of(null), invocationsAtSub1.projectTasks)
        assertTask('epsilon', null, false, ':sub1:epsilon', Maybe.of(null), invocationsAtSub1.projectTasks)

        OmniBuildInvocations invocationsAtSub2 = buildInvocations.get(Path.from(':sub2')).get()
        collectNamesOfNonImplicitTaskSelectors(invocationsAtSub2.taskSelectors) == ['alpha', 'beta', 'delta', 'epsilon', 'zeta'] as Set
        assertTaskSelector('alpha', 'ALPHA_SUB2', false, Maybe.of(null), [':sub2:subSub:alpha'], invocationsAtSub2.taskSelectors)
        assertTaskSelector('beta', null, false, Maybe.of(null), [':sub2:beta'], invocationsAtSub2.taskSelectors)
        assertTaskSelector('delta', 'DELTA', false, Maybe.of(null), [':sub2:delta', ':sub2:subSub:delta'], invocationsAtSub2.taskSelectors)
        assertTaskSelector('epsilon', null, true, Maybe.of("build"), [':sub2:epsilon'], invocationsAtSub2.taskSelectors)
        assertTaskSelector('zeta', null, false, Maybe.of(null), [':sub2:subSub:zeta'], invocationsAtSub2.taskSelectors)

        collectNamesOfNonImplicitProjectTasks(invocationsAtSub2.projectTasks) == ['beta', 'delta', 'epsilon'] as Set
        assertTask('beta', null, false, ':sub2:beta', Maybe.of(null), invocationsAtSub2.projectTasks)
        assertTask('delta', 'DELTA', false, ':sub2:delta', Maybe.of(null), invocationsAtSub2.projectTasks)
        assertTask('epsilon', null, true, ':sub2:epsilon', Maybe.of("build"), invocationsAtSub2.projectTasks)

        OmniBuildInvocations invocationsAtSubSub = buildInvocations.get(Path.from(':sub2:subSub')).get()
        collectNamesOfNonImplicitTaskSelectors(invocationsAtSubSub.taskSelectors) == ['alpha', 'delta', 'zeta'] as Set
        assertTaskSelector('alpha', 'ALPHA_SUB2', false, Maybe.of(null), [':sub2:subSub:alpha'], invocationsAtSubSub.taskSelectors)
        assertTaskSelector('delta', 'DELTA_SUB2', false, Maybe.of(null), [':sub2:subSub:delta'], invocationsAtSubSub.taskSelectors)
        assertTaskSelector('zeta', null, false, Maybe.of(null), [':sub2:subSub:zeta'], invocationsAtSubSub.taskSelectors)

        collectNamesOfNonImplicitProjectTasks(invocationsAtSubSub.projectTasks) == ['alpha', 'delta', 'zeta'] as Set
        assertTask('alpha', 'ALPHA_SUB2', false, ':sub2:subSub:alpha', Maybe.of(null), invocationsAtSubSub.projectTasks)
        assertTask('delta', 'DELTA_SUB2', false, ':sub2:subSub:delta', Maybe.of(null), invocationsAtSubSub.projectTasks)
        assertTask('zeta', null, false, ':sub2:subSub:zeta', Maybe.of(null), invocationsAtSubSub.projectTasks)
    }

    def "convertFromGradleProjectWithoutTasks"() {
        given:
        def modelRequest = toolingClient.newModelRequest(GradleProject.class)
        modelRequest.projectDir(directoryProviderProjectsWithoutTasks.testDirectory)
        modelRequest.gradleDistribution(GradleDistribution.forVersion('1.0'))
        def gradleProject = modelRequest.executeAndWait()

        when:
        DefaultOmniBuildInvocationsContainer buildInvocations = DefaultOmniBuildInvocationsContainerBuilder.build(gradleProject, false)

        then:
        buildInvocations != null
        buildInvocations.asMap().keySet() == [Path.from(':'), Path.from(':sub1'), Path.from(':sub2')] as Set
        buildInvocations.asMap().keySet().each {
            assert buildInvocations.get(it).get().projectTasks == []
            assert buildInvocations.get(it).get().taskSelectors == []
        }
  }

    private static Set<String> collectNamesOfNonImplicitTaskSelectors(List<OmniTaskSelector> tasks) {
        tasks.collect { it.name }.findAll { !ImplicitTasks.ALL.contains(it) } as Set
    }

    private static Set<String> collectNamesOfNonImplicitProjectTasks(List<OmniProjectTask> tasks) {
        tasks.collect { it.name }.findAll { !ImplicitTasks.ALL.contains(it) } as Set
    }

    private static void assertTaskSelector(String name, String description, boolean isPublic, Maybe<String> group, List<String> taskNames, List<OmniTaskSelector> selectors) {
        OmniTaskSelector element = selectors.find { it.name == name }
        assert element != null
        assert element.name == name
        assert element.description == description
        assert element.public == isPublic
        assert element.group == group

        assert element.selectedTaskPaths*.path as List == taskNames
    }

    private static void assertTask(String name, String description, boolean isPublic, String path, Maybe<String> group, List<OmniProjectTask> tasks) {
        OmniProjectTask element = tasks.find { it.name == name }
        assert element != null
        assert element.name == name
        assert element.description == description
        assert element.isPublic() == isPublic
        assert element.group == group
        assert element.path.path == path
    }

}
