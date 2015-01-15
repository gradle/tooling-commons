package com.gradleware.tooling.toolingmodel.repository.internal

import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.DomainToolingClientSpecification
import com.gradleware.tooling.toolingmodel.BuildInvocationFields
import com.gradleware.tooling.toolingmodel.ProjectTaskFields
import com.gradleware.tooling.toolingmodel.TaskSelectorsFields
import com.gradleware.tooling.toolingmodel.generic.Model
import org.gradle.tooling.model.GradleProject
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
    def buildInvocationsContainer = BuildInvocationsContainerFactory.createFrom(gradleProject, false)

    then:
    buildInvocationsContainer != null
    Map<String, Model<BuildInvocationFields>> mapping = buildInvocationsContainer.asMap()
    mapping.keySet() == [':', ':sub1', ':sub2', ':sub2:subSub'] as Set

    Model<BuildInvocationFields> invocationsAtRoot = mapping[':']
    invocationsAtRoot.get(BuildInvocationFields.TASK_SELECTORS).collect { it.get(TaskSelectorsFields.NAME) } as Set == ['alpha', 'beta', 'gamma', 'delta', 'epsilon', 'zeta'] as Set
    assertTaskSelector('alpha', 'ALPHA', true, [':alpha', ':sub2:subSub:alpha'], invocationsAtRoot.get(BuildInvocationFields.TASK_SELECTORS))
    assertTaskSelector('beta', null, true, [':beta', ':sub1:beta', ':sub2:beta'], invocationsAtRoot.get(BuildInvocationFields.TASK_SELECTORS))
    assertTaskSelector('gamma', null, false, [':sub1:gamma'], invocationsAtRoot.get(BuildInvocationFields.TASK_SELECTORS))
    assertTaskSelector('delta', 'DELTA', false, [':sub2:delta', ':sub2:subSub:delta'], invocationsAtRoot.get(BuildInvocationFields.TASK_SELECTORS))
    assertTaskSelector('epsilon', null, true, [':sub1:epsilon', ':sub2:epsilon'], invocationsAtRoot.get(BuildInvocationFields.TASK_SELECTORS))
    assertTaskSelector('zeta', null, false, [':sub2:subSub:zeta'], invocationsAtRoot.get(BuildInvocationFields.TASK_SELECTORS))

    invocationsAtRoot.get(BuildInvocationFields.PROJECT_TASKS).collect { it.get(ProjectTaskFields.NAME) } as Set == ['alpha', 'beta'] as Set
    assertTask('alpha', 'ALPHA', true, ':alpha', invocationsAtRoot.get(BuildInvocationFields.PROJECT_TASKS))
    assertTask('beta', null, false, ':beta', invocationsAtRoot.get(BuildInvocationFields.PROJECT_TASKS))

    Model<BuildInvocationFields> invocationsAtSub1 = mapping[':sub1']
    invocationsAtSub1.get(BuildInvocationFields.TASK_SELECTORS).collect { it.get(TaskSelectorsFields.NAME) } as Set == ['beta', 'gamma', 'epsilon'] as Set
    assertTaskSelector('beta', 'BETA', true, [':sub1:beta'], invocationsAtSub1.get(BuildInvocationFields.TASK_SELECTORS))
    assertTaskSelector('gamma', null, false, [':sub1:gamma'], invocationsAtSub1.get(BuildInvocationFields.TASK_SELECTORS))
    assertTaskSelector('epsilon', null, false, [':sub1:epsilon'], invocationsAtSub1.get(BuildInvocationFields.TASK_SELECTORS))

    invocationsAtSub1.get(BuildInvocationFields.PROJECT_TASKS).collect { it.get(ProjectTaskFields.NAME) } as Set == ['beta', 'gamma', 'epsilon'] as Set
    assertTask('beta', 'BETA', true, ':sub1:beta', invocationsAtSub1.get(BuildInvocationFields.PROJECT_TASKS))
    assertTask('gamma', null, false, ':sub1:gamma', invocationsAtSub1.get(BuildInvocationFields.PROJECT_TASKS))
    assertTask('epsilon', null, false, ':sub1:epsilon', invocationsAtSub1.get(BuildInvocationFields.PROJECT_TASKS))

    Model<BuildInvocationFields> invocationsAtSub2 = mapping[':sub2']
    invocationsAtSub2.get(BuildInvocationFields.TASK_SELECTORS).collect { it.get(TaskSelectorsFields.NAME) } as Set == ['alpha', 'beta', 'delta', 'epsilon', 'zeta'] as Set
    assertTaskSelector('alpha', 'ALPHA_SUB2', false, [':sub2:subSub:alpha'], invocationsAtSub2.get(BuildInvocationFields.TASK_SELECTORS))
    assertTaskSelector('beta', null, false, [':sub2:beta'], invocationsAtSub2.get(BuildInvocationFields.TASK_SELECTORS))
    assertTaskSelector('delta', 'DELTA', false, [':sub2:delta', ':sub2:subSub:delta'], invocationsAtSub2.get(BuildInvocationFields.TASK_SELECTORS))
    assertTaskSelector('epsilon', null, true, [':sub2:epsilon'], invocationsAtSub2.get(BuildInvocationFields.TASK_SELECTORS))
    assertTaskSelector('zeta', null, false, [':sub2:subSub:zeta'], invocationsAtSub2.get(BuildInvocationFields.TASK_SELECTORS))

    invocationsAtSub2.get(BuildInvocationFields.PROJECT_TASKS).collect { it.get(ProjectTaskFields.NAME) } as Set == ['beta', 'delta', 'epsilon'] as Set
    assertTask('beta', null, false, ':sub2:beta', invocationsAtSub2.get(BuildInvocationFields.PROJECT_TASKS))
    assertTask('delta', 'DELTA', false, ':sub2:delta', invocationsAtSub2.get(BuildInvocationFields.PROJECT_TASKS))
    assertTask('epsilon', null, true, ':sub2:epsilon', invocationsAtSub2.get(BuildInvocationFields.PROJECT_TASKS))

    Model<BuildInvocationFields> invocationsAtSubSub = mapping[':sub2:subSub']
    invocationsAtSubSub.get(BuildInvocationFields.TASK_SELECTORS).collect { it.get(TaskSelectorsFields.NAME) } as Set == ['alpha', 'delta', 'zeta'] as Set
    assertTaskSelector('alpha', 'ALPHA_SUB2', false, [':sub2:subSub:alpha'], invocationsAtSubSub.get(BuildInvocationFields.TASK_SELECTORS))
    assertTaskSelector('delta', 'DELTA_SUB2', false, [':sub2:subSub:delta'], invocationsAtSubSub.get(BuildInvocationFields.TASK_SELECTORS))
    assertTaskSelector('zeta', null, false, [':sub2:subSub:zeta'], invocationsAtSubSub.get(BuildInvocationFields.TASK_SELECTORS))

    invocationsAtSubSub.get(BuildInvocationFields.PROJECT_TASKS).collect { it.get(ProjectTaskFields.NAME) } as Set == ['alpha', 'delta', 'zeta'] as Set
    assertTask('alpha', 'ALPHA_SUB2', false, ':sub2:subSub:alpha', invocationsAtSubSub.get(BuildInvocationFields.PROJECT_TASKS))
    assertTask('delta', 'DELTA_SUB2', false, ':sub2:subSub:delta', invocationsAtSubSub.get(BuildInvocationFields.PROJECT_TASKS))
    assertTask('zeta', null, false, ':sub2:subSub:zeta', invocationsAtSubSub.get(BuildInvocationFields.PROJECT_TASKS))
  }

  private static void assertTaskSelector(String name, String description, boolean isPublic, List<String> taskNames, List<Model<TaskSelectorsFields>> selectors) {
    def element = selectors.find { it.get(TaskSelectorsFields.NAME) == name }
    assert element != null
    assert element.get(TaskSelectorsFields.NAME) == name
    assert element.get(TaskSelectorsFields.DESCRIPTION) == description
    assert element.get(TaskSelectorsFields.IS_PUBLIC) == isPublic
    assert element.get(TaskSelectorsFields.SELECTED_TASK_PATHS) as List == taskNames
  }

  private static void assertTask(String name, String description, boolean isPublic, String path, List<Model<ProjectTaskFields>> tasks) {
    def element = tasks.find { it.get(ProjectTaskFields.NAME) == name }
    assert element != null
    assert element.get(ProjectTaskFields.NAME) == name
    assert element.get(ProjectTaskFields.DESCRIPTION) == description
    assert element.get(ProjectTaskFields.IS_PUBLIC) == isPublic
    assert element.get(ProjectTaskFields.PATH) == path
  }

}
