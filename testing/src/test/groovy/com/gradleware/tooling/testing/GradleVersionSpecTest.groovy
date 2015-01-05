package com.gradleware.tooling.testing

import org.gradle.util.GradleVersion
import spock.lang.Specification

class GradleVersionSpecTest extends Specification {

  def "toSpec"() {
    when:
    def spec = GradleVersionSpec.toSpec("current")

    then:
    spec.isSatisfiedBy(GradleVersion.current())
    !spec.isSatisfiedBy(GradleVersion.version("1.0"))

    when:
    spec = GradleVersionSpec.toSpec("!current")

    then:
    !spec.isSatisfiedBy(GradleVersion.current())
    spec.isSatisfiedBy(GradleVersion.version("1.0"))

    when:
    spec = GradleVersionSpec.toSpec("=1.3")

    then:
    spec.isSatisfiedBy(GradleVersion.version("1.3"))
    !spec.isSatisfiedBy(GradleVersion.version("1.3.1"))

    when:
    spec = GradleVersionSpec.toSpec(">=1.3")

    then:
    spec.isSatisfiedBy(GradleVersion.version("1.3"))
    spec.isSatisfiedBy(GradleVersion.version("1.4"))
    !spec.isSatisfiedBy(GradleVersion.version("1.2"))

    when:
    spec = GradleVersionSpec.toSpec(">1.3")

    then:
    spec.isSatisfiedBy(GradleVersion.version("1.4"))
    !spec.isSatisfiedBy(GradleVersion.version("1.3"))

    when:
    spec = GradleVersionSpec.toSpec("<=1.3")

    then:
    spec.isSatisfiedBy(GradleVersion.version("1.3"))
    spec.isSatisfiedBy(GradleVersion.version("1.2"))
    !spec.isSatisfiedBy(GradleVersion.version("1.4"))

    when:
    spec = GradleVersionSpec.toSpec("<1.3")

    then:
    spec.isSatisfiedBy(GradleVersion.version("1.2"))
    !spec.isSatisfiedBy(GradleVersion.version("1.3"))
  }

}
