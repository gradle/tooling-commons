package com.gradleware.tooling.toolingmodel.internal

import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.DomainToolingClientSpecification
import org.gradle.tooling.internal.gradle.TaskListingLaunchable
import org.gradle.tooling.model.GradleProject
import org.gradle.tooling.model.Task
import org.gradle.tooling.model.TaskSelector
import org.gradle.tooling.model.gradle.BuildInvocations
import org.junit.Rule

class BuildInvocationsContainerFactoryTest extends DomainToolingClientSpecification {

  @Rule
  public TestDirectoryProvider directoryProvider = new TestDirectoryProvider();

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
  }

  def "convertFromGradleProject"() {
    given:
    def modelRequest = toolingClient.newModelRequest(GradleProject.class)
    modelRequest.projectDir(directoryProvider.testDirectory)
    def gradleProject = modelRequest.executeAndWait()

    when:
    def buildInvocationsContainer = BuildInvocationsContainerFactory.createFrom(gradleProject)

    then:
    buildInvocationsContainer != null
    Map<String, BuildInvocations> mapping = buildInvocationsContainer.asMap()
    mapping.keySet() == [':', ':sub1', ':sub2', ':sub2:subSub'] as Set

    BuildInvocations invocationsAtRoot = mapping[':']
    invocationsAtRoot.taskSelectors.collect { it.name } as Set == ['alpha', 'beta', 'gamma', 'delta', 'epsilon', 'zeta'] as Set
    assertTaskSelector('alpha', "alpha in GradleProject{path=':'} and subprojects.", 'ALPHA', true, [':alpha', ':sub2:subSub:alpha'], invocationsAtRoot.taskSelectors)
    assertTaskSelector('beta', "beta in GradleProject{path=':'} and subprojects.", null, true, [':beta', ':sub1:beta', ':sub2:beta'], invocationsAtRoot.taskSelectors)
    assertTaskSelector('gamma', "gamma in GradleProject{path=':'} and subprojects.", null, false, [':sub1:gamma'], invocationsAtRoot.taskSelectors)
    assertTaskSelector('delta', "delta in GradleProject{path=':'} and subprojects.", 'DELTA', false, [':sub2:delta', ':sub2:subSub:delta'], invocationsAtRoot.taskSelectors)
    assertTaskSelector('epsilon', "epsilon in GradleProject{path=':'} and subprojects.", null, true, [':sub1:epsilon', ':sub2:epsilon'], invocationsAtRoot.taskSelectors)
    assertTaskSelector('zeta', "zeta in GradleProject{path=':'} and subprojects.", null, false, [':sub2:subSub:zeta'], invocationsAtRoot.taskSelectors)

    invocationsAtRoot.tasks.collect { it.name } as Set == ['alpha', 'beta'] as Set
    assertTask('alpha', "task ':alpha'", 'ALPHA', true, ':alpha', invocationsAtRoot.tasks)
    assertTask('beta', "task ':beta'", null, false, ':beta', invocationsAtRoot.tasks)

    BuildInvocations invocationsAtSub1 = mapping[':sub1']
    invocationsAtSub1.taskSelectors.collect { it.name } as Set == ['beta', 'gamma', 'epsilon'] as Set
    assertTaskSelector('beta', "beta in GradleProject{path=':sub1'} and subprojects.", 'BETA', true, [':sub1:beta'], invocationsAtSub1.taskSelectors)
    assertTaskSelector('gamma', "gamma in GradleProject{path=':sub1'} and subprojects.", null, false, [':sub1:gamma'], invocationsAtSub1.taskSelectors)
    assertTaskSelector('epsilon', "epsilon in GradleProject{path=':sub1'} and subprojects.", null, false, [':sub1:epsilon'], invocationsAtSub1.taskSelectors)

    invocationsAtSub1.tasks.collect { it.name } as Set == ['beta', 'gamma', 'epsilon'] as Set
    assertTask('beta', "task ':sub1:beta'", 'BETA', true, ':sub1:beta', invocationsAtSub1.tasks)
    assertTask('gamma', "task ':sub1:gamma'", null, false, ':sub1:gamma', invocationsAtSub1.tasks)
    assertTask('epsilon', "task ':sub1:epsilon'", null, false, ':sub1:epsilon', invocationsAtSub1.tasks)

    BuildInvocations invocationsAtSub2 = mapping[':sub2']
    invocationsAtSub2.taskSelectors.collect { it.name } as Set == ['alpha', 'beta', 'delta', 'epsilon', 'zeta'] as Set
    assertTaskSelector('alpha', "alpha in GradleProject{path=':sub2'} and subprojects.", 'ALPHA_SUB2', false, [':sub2:subSub:alpha'], invocationsAtSub2.taskSelectors)
    assertTaskSelector('beta', "beta in GradleProject{path=':sub2'} and subprojects.", null, false, [':sub2:beta'], invocationsAtSub2.taskSelectors)
    assertTaskSelector('delta', "delta in GradleProject{path=':sub2'} and subprojects.", 'DELTA', false, [':sub2:delta', ':sub2:subSub:delta'], invocationsAtSub2.taskSelectors)
    assertTaskSelector('epsilon', "epsilon in GradleProject{path=':sub2'} and subprojects.", null, true, [':sub2:epsilon'], invocationsAtSub2.taskSelectors)
    assertTaskSelector('zeta', "zeta in GradleProject{path=':sub2'} and subprojects.", null, false, [':sub2:subSub:zeta'], invocationsAtSub2.taskSelectors)

    invocationsAtSub2.tasks.collect { it.name } as Set == ['beta', 'delta', 'epsilon'] as Set
    assertTask('beta', "task ':sub2:beta'", null, false, ':sub2:beta', invocationsAtSub2.tasks)
    assertTask('delta', "task ':sub2:delta'", 'DELTA', false, ':sub2:delta', invocationsAtSub2.tasks)
    assertTask('epsilon', "task ':sub2:epsilon'", null, true, ':sub2:epsilon', invocationsAtSub2.tasks)

    BuildInvocations invocationsAtSubSub = mapping[':sub2:subSub']
    invocationsAtSubSub.taskSelectors.collect { it.name } as Set == ['alpha', 'delta', 'zeta'] as Set
    assertTaskSelector('alpha', "alpha in GradleProject{path=':sub2:subSub'} and subprojects.", 'ALPHA_SUB2', false, [':sub2:subSub:alpha'], invocationsAtSubSub.taskSelectors)
    assertTaskSelector('delta', "delta in GradleProject{path=':sub2:subSub'} and subprojects.", 'DELTA_SUB2', false, [':sub2:subSub:delta'], invocationsAtSubSub.taskSelectors)
    assertTaskSelector('zeta', "zeta in GradleProject{path=':sub2:subSub'} and subprojects.", null, false, [':sub2:subSub:zeta'], invocationsAtSubSub.taskSelectors)

    invocationsAtSubSub.tasks.collect { it.name } as Set == ['alpha', 'delta', 'zeta'] as Set
    assertTask('alpha', "task ':sub2:subSub:alpha'", 'ALPHA_SUB2', false, ':sub2:subSub:alpha', invocationsAtSubSub.tasks)
    assertTask('delta', "task ':sub2:subSub:delta'", 'DELTA_SUB2', false, ':sub2:subSub:delta', invocationsAtSubSub.tasks)
    assertTask('zeta', "task ':sub2:subSub:zeta'", null, false, ':sub2:subSub:zeta', invocationsAtSubSub.tasks)
  }

  private static void assertTaskSelector(String name, String displayName, String description, boolean isPublic, List<String> taskNames, Set<? extends TaskSelector> selectors) {
    def element = selectors.find { it.name == name }
    assert element != null
    assert element.name == name
    assert element.displayName == displayName
    assert element.description == description
    assert element.isPublic() == isPublic
    assert ((TaskListingLaunchable) element).taskNames as List == taskNames
  }

  private static void assertTask(String name, String displayName, String description, boolean isPublic, String path, Set<? extends Task> tasks) {
    def element = tasks.find { it.name == name }
    assert element != null
    assert element.name == name
    assert element.displayName == displayName
    assert element.description == description
    assert element.isPublic() == isPublic
    assert element.path == path
    assert ((TaskListingLaunchable) element).taskNames as List == [path]
  }

}
