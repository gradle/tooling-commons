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

package com.gradleware.tooling.toolingmodel.repository

import com.google.common.collect.ImmutableList
import com.gradleware.tooling.toolingclient.BuildActionRequest
import com.gradleware.tooling.toolingclient.GradleDistribution
import nl.jqno.equalsverifier.EqualsVerifier
import spock.lang.Specification

class FixedRequestAttributesTest extends Specification {

  def "apply"() {
    setup:
    BuildActionRequest<?> buildActionRequest = Mock(BuildActionRequest.class)
    def projectDir = new File('.')
    def gradleUserHomeDir = new File('..')
    def distribution = GradleDistribution.fromBuild()
    def javaHomeDir = new File('~')
    def requestAttributes = new FixedRequestAttributes(projectDir, gradleUserHomeDir, distribution, javaHomeDir, ImmutableList.of("alpha"), ImmutableList.of("beta"))

    when:
    requestAttributes.apply(buildActionRequest)

    then:
    1 * buildActionRequest.projectDir(projectDir.canonicalFile)
    1 * buildActionRequest.gradleUserHomeDir(gradleUserHomeDir.canonicalFile)
    1 * buildActionRequest.gradleDistribution(distribution)
    1 * buildActionRequest.javaHomeDir(javaHomeDir.canonicalFile)
    1 * buildActionRequest.jvmArguments("alpha")
    1 * buildActionRequest.arguments("beta")
  }

  def "equalsAndHashCode"() {
    setup:
    def requestAttributes = new FixedRequestAttributes(new File('.'), new File('..'), GradleDistribution.fromBuild(), new File('~'), ImmutableList.of(), ImmutableList.of())
    assert requestAttributes == requestAttributes
    assert requestAttributes.hashCode() == requestAttributes.hashCode()
  }

  def "equality"() {
    setup:
    EqualsVerifier.forClass(FixedRequestAttributes.class).usingGetClass().verify();
  }

}
