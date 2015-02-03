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
