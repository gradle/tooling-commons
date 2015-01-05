package com.gradleware.tooling.domain

import com.google.common.collect.ImmutableList
import com.gradleware.tooling.toolingapi.BuildActionRequest
import com.gradleware.tooling.toolingapi.GradleDistribution
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
    1 * buildActionRequest.projectDir(projectDir)
    1 * buildActionRequest.gradleUserHomeDir(gradleUserHomeDir)
    1 * buildActionRequest.gradleDistribution(distribution)
    1 * buildActionRequest.javaHomeDir(javaHomeDir)
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
