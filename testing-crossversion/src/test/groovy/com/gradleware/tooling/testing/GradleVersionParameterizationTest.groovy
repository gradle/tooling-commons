package com.gradleware.tooling.testing

import com.google.common.base.Supplier
import com.google.common.collect.ImmutableList
import com.gradleware.tooling.toolingclient.GradleDistribution
import org.gradle.util.GradleVersion
import spock.lang.Shared
import spock.lang.Specification

class GradleVersionParameterizationTest extends Specification {

  @Shared
  def gradleVersionParameterization

  def setupSpec() {
    def releasedVersions = ReleasedGradleVersions.createFrom(['2.2.1', '2.2', '2.1', '2.0', '1.12', '1.11'] as Set)
    def versionProvider = new GradleVersionProvider(releasedVersions, { "all" } as Supplier)
    gradleVersionParameterization = new GradleVersionParameterization(versionProvider)
  }

  def "getGradleVersions"() {
    when:
    List<GradleVersion> versions = gradleVersionParameterization.getGradleVersions(">=2.1")

    then:
    versions == [GradleVersion.current(), GradleVersion.version("2.2.1"), GradleVersion.version("2.2"), GradleVersion.version("2.1")]
  }

  def "getGradleDistributions"() {
    when:
    List<GradleDistribution> distributions = gradleVersionParameterization.getGradleDistributions(">=2.1")

    then:
    distributions == [getDistributionForCurrentGradleVersion(), GradleDistribution.forVersion("2.2.1"), GradleDistribution.forVersion("2.2"), GradleDistribution.forVersion("2.1")]
  }

  def "getPermutationsInTwoDimensions"() {
    when:
    ImmutableList<List<Object>> permutations = gradleVersionParameterization.getPermutations(">=2.2", ["Foo", "Bar"])

    then:
    permutations == [[getDistributionForCurrentGradleVersion(), "Foo"], [getDistributionForCurrentGradleVersion(), "Bar"],
                     [GradleDistribution.forVersion("2.2.1"), "Foo"], [GradleDistribution.forVersion("2.2.1"), "Bar"],
                     [GradleDistribution.forVersion("2.2"), "Foo"], [GradleDistribution.forVersion("2.2"), "Bar"]]
  }

  def "getPermutationsInThreeDimensions"() {
    when:
    ImmutableList<List<Object>> permutations = gradleVersionParameterization.getPermutations(">=2.3", ["Foo", "Bar"], ["One"])

    then:
    permutations == [[getDistributionForCurrentGradleVersion(), "Foo", "One"], [getDistributionForCurrentGradleVersion(), "Bar", "One"]]
  }

  private static GradleDistribution getDistributionForCurrentGradleVersion() {
    def pattern = GradleVersion.current().isSnapshot() ?
            "https://services.gradle.org/distributions-snapshots/gradle-%s-bin.zip" :
            "https://services.gradle.org/distributions/gradle-%s-bin.zip"
    def uriToCurrentGradleVersion = String.format(pattern, GradleVersion.current().version)
    GradleDistribution.forRemoteDistribution(new URI(uriToCurrentGradleVersion))
  }

}
