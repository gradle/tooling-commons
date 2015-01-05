package com.gradleware.tooling.toolingapi

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.model.Launchable
import spock.lang.Specification

@SuppressWarnings("GroovyAssignabilityCheck")
class LaunchableConfigTest extends Specification {

  def "forTasksVarArgs"() {
    setup:
    BuildLauncher buildLauncher = Mock(BuildLauncher.class)
    def launchables = LaunchableConfig.forTasks("alpha")

    when:
    launchables.apply(buildLauncher)

    then:
    1 * buildLauncher.forTasks(["alpha"])
    0 * buildLauncher.forLaunchables(_)
  }

  def "forTasksIterable"() {
    setup:
    BuildLauncher buildLauncher = Mock(BuildLauncher.class)
    def launchables = LaunchableConfig.forTasks(Arrays.asList("alpha"))

    when:
    launchables.apply(buildLauncher)

    then:
    1 * buildLauncher.forTasks(["alpha"])
    0 * buildLauncher.forLaunchables(_)
  }

  def "forLaunchablesVarArgs"() {
    setup:
    BuildLauncher buildLauncher = Mock(BuildLauncher.class)
    def launchable = Mock(Launchable.class)
    def launchables = LaunchableConfig.forLaunchables(launchable)

    when:
    launchables.apply(buildLauncher)

    then:
    1 * buildLauncher.forLaunchables([launchable])
    0 * buildLauncher.forTasks(_)
  }

  def "forLaunchablesIterable"() {
    setup:
    BuildLauncher buildLauncher = Mock(BuildLauncher.class)
    def launchable = Mock(Launchable.class)
    def launchables = LaunchableConfig.forLaunchables(Arrays.asList(launchable))

    when:
    launchables.apply(buildLauncher)

    then:
    1 * buildLauncher.forLaunchables([launchable])
    0 * buildLauncher.forTasks(_)
  }

}
