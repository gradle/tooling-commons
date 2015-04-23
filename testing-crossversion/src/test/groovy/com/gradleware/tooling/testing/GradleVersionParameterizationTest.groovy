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
    def versionProvider = new GradleVersionProvider(releasedVersions, { 'all' } as Supplier)
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
    if (GradleVersion.current().isSnapshot()) {
      def pattern = "https://services.gradle.org/distributions-snapshots/gradle-%s-bin.zip"
      def uriToCurrentGradleVersion = String.format(pattern, GradleVersion.current().version)
      GradleDistribution.forRemoteDistribution(new URI(uriToCurrentGradleVersion))
    } else {
      GradleDistribution.forVersion(GradleVersion.current().getVersion())
    }
  }

}
