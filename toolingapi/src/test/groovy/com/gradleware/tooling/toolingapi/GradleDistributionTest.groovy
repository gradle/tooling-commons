package com.gradleware.tooling.toolingapi

import nl.jqno.equalsverifier.EqualsVerifier
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.internal.consumer.DefaultGradleConnector
import spock.lang.Specification

class GradleDistributionTest extends Specification {

  def "forLocalInstallation"() {
    setup:
    GradleConnector connector = Mock(GradleConnector.class)
    def file = new File('.')
    def distribution = GradleDistribution.forLocalInstallation(file)

    when:
    distribution.apply(connector)

    then:
    1 * connector.useInstallation(file)
  }

  def "forRemoteDistribution"() {
    setup:
    GradleConnector connector = Mock(GradleConnector.class)
    def uri = new File('.').toURI()
    def distribution = GradleDistribution.forRemoteDistribution(uri)

    when:
    distribution.apply(connector)

    then:
    1 * connector.useDistribution(uri)
  }

  def "forVersion"() {
    setup:
    GradleConnector connector = Mock(GradleConnector.class)
    def version = '2.0'
    def distribution = GradleDistribution.forVersion(version)

    when:
    distribution.apply(connector)

    then:
    1 * connector.useGradleVersion(version)
  }

  def "fromBuild"() {
    setup:
    DefaultGradleConnector connector = Mock(DefaultGradleConnector.class)
    def distribution = GradleDistribution.fromBuild()

    when:
    distribution.apply(connector)

    then:
    1 * connector.useBuildDistribution()
  }

  def "string"() {
    when:
    def file = new File('.')
    def distribution = GradleDistribution.forLocalInstallation(file)

    then:
    distribution.toString() == "Gradle installation " + file.absolutePath

    when:
    distribution = GradleDistribution.forRemoteDistribution(file.toURI())

    then:
    distribution.toString() == "Gradle distribution " + file.toURI().toString()

    when:
    distribution = GradleDistribution.forVersion('2.1')

    then:
    distribution.toString() == "Gradle version 2.1"

    when:
    distribution = GradleDistribution.fromBuild()

    then:
    distribution.toString() == "Gradle version of target build"
  }

  def "equality"() {
    setup:
    EqualsVerifier.forClass(GradleDistribution.class).usingGetClass().verify();
  }

}
