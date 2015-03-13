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

package com.gradleware.tooling.testing

import com.google.common.collect.ImmutableList
import org.gradle.util.GradleVersion
import spock.lang.Specification

class ReleasedGradleVersionsTest extends Specification {

  def "getAll"() {
    when:
    def releasedVersions = ReleasedGradleVersions.createFrom(['2.2', '2.3', '2.1'] as Set)

    then:
    releasedVersions.all == ImmutableList.of(GradleVersion.version('2.3'), GradleVersion.version('2.2'), GradleVersion.version('2.1'))
  }

  def "getLatest"() {
    when:
    def releasedVersions = ReleasedGradleVersions.createFrom(['2.2', '2.3', '2.1'] as Set)

    then:
    releasedVersions.latest == GradleVersion.version('2.3')
  }

  def "versionStringsMustNotBeEmpty"() {
    when:
    ReleasedGradleVersions.createFrom(null)

    then:
    thrown(NullPointerException)

    when:
    ReleasedGradleVersions.createFrom([] as Set)

    then:
    thrown(IllegalStateException)
  }

}
