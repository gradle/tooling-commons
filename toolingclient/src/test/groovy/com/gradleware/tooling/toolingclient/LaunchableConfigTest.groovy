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

package com.gradleware.tooling.toolingclient

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
