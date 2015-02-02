package com.gradleware.tooling.toolingutils.distribution

import org.gradle.util.GradleVersion
import spock.lang.Specification

class PublishedGradleVersionsTest extends Specification {

  def "create version info from JSON string"() {
    setup:
    def json = PublishedGradleVersions.class.getResource("versions_20150202.json")
    PublishedGradleVersions publishedVersions = PublishedGradleVersions.create(json.text)
    assert publishedVersions.versions == ['2.3-rc-1',
                                          '2.2.1',
                                          '2.2',
                                          '2.1',
                                          '2.0',
                                          '1.12',
                                          '1.11',
                                          '1.10',
                                          '1.9',
                                          '1.8',
                                          '1.7',
                                          '1.6',
                                          '1.5',
                                          '1.4',
                                          '1.3',
                                          '1.2',
                                          '1.1',
                                          '1.0',
    ].collect { GradleVersion.version(it) }
  }

}
