package com.gradleware.tooling.toolingmodel.repository.internal

import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.ToolingModelToolingClientSpecification
import com.gradleware.tooling.toolingmodel.OmniBuildInvocations
import com.gradleware.tooling.toolingmodel.OmniProjectTask
import com.gradleware.tooling.toolingmodel.OmniTaskSelector
import com.gradleware.tooling.toolingmodel.Path
import org.gradle.tooling.model.GradleProject
import org.junit.Rule

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
        invocationsAtRoot.taskSelectors.collect { it.name } as Set == ['alpha', 'beta', 'gamma', 'delta', 'epsilon', 'zeta'] as Set
        assertTaskSelector('alpha', 'ALPHA', true, [':alpha', ':sub2:subSub:alpha'], invocationsAtRoot.taskSelectors)
        assertTaskSelector('beta', null, true, [':beta', ':sub1:beta', ':sub2:beta'], invocationsAtRoot.taskSelectors)
        assertTaskSelector('gamma', null, false, [':sub1:gamma'], invocationsAtRoot.taskSelectors)
        assertTaskSelector('delta', 'DELTA', false, [':sub2:delta', ':sub2:subSub:delta'], invocationsAtRoot.taskSelectors)
        assertTaskSelector('epsilon', null, true, [':sub1:epsilon', ':sub2:epsilon'], invocationsAtRoot.taskSelectors)
        assertTaskSelector('zeta', null, false, [':sub2:subSub:zeta'], invocationsAtRoot.taskSelectors)

        invocationsAtRoot.projectTasks.collect { it.name } as Set == ['alpha', 'beta'] as Set
        assertTask('alpha', 'ALPHA', true, ':alpha', invocationsAtRoot.projectTasks)
        assertTask('beta', null, false, ':beta', invocationsAtRoot.projectTasks)

        OmniBuildInvocations invocationsAtSub1 = buildInvocations.get(Path.from(':sub1')).get()
        invocationsAtSub1.taskSelectors.collect { it.name } as Set == ['beta', 'gamma', 'epsilon'] as Set
        assertTaskSelector('beta', 'BETA', true, [':sub1:beta'], invocationsAtSub1.taskSelectors)
        assertTaskSelector('gamma', null, false, [':sub1:gamma'], invocationsAtSub1.taskSelectors)
        assertTaskSelector('epsilon', null, false, [':sub1:epsilon'], invocationsAtSub1.taskSelectors)

        invocationsAtSub1.projectTasks.collect { it.name } as Set == ['beta', 'gamma', 'epsilon'] as Set
        assertTask('beta', 'BETA', true, ':sub1:beta', invocationsAtSub1.projectTasks)
        assertTask('gamma', null, false, ':sub1:gamma', invocationsAtSub1.projectTasks)
        assertTask('epsilon', null, false, ':sub1:epsilon', invocationsAtSub1.projectTasks)

        OmniBuildInvocations invocationsAtSub2 = buildInvocations.get(Path.from(':sub2')).get()
        invocationsAtSub2.taskSelectors.collect { it.name } as Set == ['alpha', 'beta', 'delta', 'epsilon', 'zeta'] as Set
        assertTaskSelector('alpha', 'ALPHA_SUB2', false, [':sub2:subSub:alpha'], invocationsAtSub2.taskSelectors)
        assertTaskSelector('beta', null, false, [':sub2:beta'], invocationsAtSub2.taskSelectors)
        assertTaskSelector('delta', 'DELTA', false, [':sub2:delta', ':sub2:subSub:delta'], invocationsAtSub2.taskSelectors)
        assertTaskSelector('epsilon', null, true, [':sub2:epsilon'], invocationsAtSub2.taskSelectors)
        assertTaskSelector('zeta', null, false, [':sub2:subSub:zeta'], invocationsAtSub2.taskSelectors)

        invocationsAtSub2.projectTasks.collect { it.name } as Set == ['beta', 'delta', 'epsilon'] as Set
        assertTask('beta', null, false, ':sub2:beta', invocationsAtSub2.projectTasks)
        assertTask('delta', 'DELTA', false, ':sub2:delta', invocationsAtSub2.projectTasks)
        assertTask('epsilon', null, true, ':sub2:epsilon', invocationsAtSub2.projectTasks)

        OmniBuildInvocations invocationsAtSubSub = buildInvocations.get(Path.from(':sub2:subSub')).get()
        invocationsAtSubSub.taskSelectors.collect { it.name } as Set == ['alpha', 'delta', 'zeta'] as Set
        assertTaskSelector('alpha', 'ALPHA_SUB2', false, [':sub2:subSub:alpha'], invocationsAtSubSub.taskSelectors)
        assertTaskSelector('delta', 'DELTA_SUB2', false, [':sub2:subSub:delta'], invocationsAtSubSub.taskSelectors)
        assertTaskSelector('zeta', null, false, [':sub2:subSub:zeta'], invocationsAtSubSub.taskSelectors)

        invocationsAtSubSub.projectTasks.collect { it.name } as Set == ['alpha', 'delta', 'zeta'] as Set
        assertTask('alpha', 'ALPHA_SUB2', false, ':sub2:subSub:alpha', invocationsAtSubSub.projectTasks)
        assertTask('delta', 'DELTA_SUB2', false, ':sub2:subSub:delta', invocationsAtSubSub.projectTasks)
        assertTask('zeta', null, false, ':sub2:subSub:zeta', invocationsAtSubSub.projectTasks)
    }

    def "convertFromProjectsWithoutTasks"() {
        given:
        def modelRequest = toolingClient.newModelRequest(GradleProject.class)
        modelRequest.projectDir(directoryProviderProjectsWithoutTasks.testDirectory)
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

    private static void assertTaskSelector(String name, String description, boolean isPublic, List<String> taskNames, List<OmniTaskSelector> selectors) {
        def element = selectors.find { it.name == name }
        assert element != null
        assert element.name == name
        assert element.description == description
        assert element.isPublic() == isPublic
        assert element.selectedTaskPaths*.path as List == taskNames
    }

    private static void assertTask(String name, String description, boolean isPublic, String path, List<OmniProjectTask> tasks) {
        def element = tasks.find { it.name == name }
        assert element != null
        assert element.name == name
        assert element.description == description
        assert element.isPublic() == isPublic
        assert element.path.path == path
    }

}
