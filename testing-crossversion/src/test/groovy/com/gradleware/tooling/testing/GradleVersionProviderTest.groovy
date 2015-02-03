package com.gradleware.tooling.testing

import com.google.common.base.Supplier
import com.google.common.collect.ImmutableList
import org.gradle.util.GradleVersion
import spock.lang.Specification

class GradleVersionProviderTest extends Specification {

  final ReleasedGradleVersions releasedGradleVersions = ReleasedGradleVersions.createFrom(['2.2.1', '2.2', '2.1', '2.0', '1.12', '1.11'] as Set)

  def "getConfiguredGradleVersions_ALL_includesCurrentVersion"() {
    given:
    def versionProvider = new GradleVersionProvider(releasedGradleVersions, new Supplier<String>() {

      @Override
      String get() {
        GradleVersionProvider.ALL
      }
    })

    when:
    ImmutableList<GradleVersion> versions = versionProvider.getConfiguredGradleVersions()

    then:
    versions == [GradleVersion.current(), GradleVersion.version('2.2.1'), GradleVersion.version('2.2'), GradleVersion.version('2.1'), GradleVersion.version('2.0'), GradleVersion.version('1.12'), GradleVersion.version('1.11')]
  }

  def "getConfiguredGradleVersions_LATEST_includesCurrentVersion"() {
    given:
    def versionProvider = new GradleVersionProvider(releasedGradleVersions, new Supplier<String>() {

      @Override
      String get() {
        GradleVersionProvider.LATEST
      }
    })

    when:
    ImmutableList<GradleVersion> versions = versionProvider.getConfiguredGradleVersions()

    then:
    versions == [GradleVersion.current(), GradleVersion.version('2.2.1')]
  }

  def "getConfiguredGradleVersions_PATTERN_doesNotIncludeCurrentVersion"() {
    given:
    def versionProvider = new GradleVersionProvider(releasedGradleVersions, new Supplier<String>() {

      @Override
      String get() {
        "2.2,1.11"
      }
    })

    when:
    ImmutableList<GradleVersion> versions = versionProvider.getConfiguredGradleVersions()

    then:
    versions == [GradleVersion.version('2.2'), GradleVersion.version('1.11')]
  }

  def "getConfiguredGradleVersions_UNKNOW_PATTERN_throwsException"() {
    given:
    def versionProvider = new GradleVersionProvider(releasedGradleVersions, new Supplier<String>() {

      @Override
      String get() {
        "unknown"
      }
    })

    when:
    versionProvider.getConfiguredGradleVersions()

    then:
    def e = thrown(RuntimeException)
    e.message.toLowerCase().contains('invalid range pattern')
  }

}
