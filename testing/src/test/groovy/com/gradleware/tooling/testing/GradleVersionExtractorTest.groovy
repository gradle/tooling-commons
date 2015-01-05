package com.gradleware.tooling.testing

import com.google.common.base.Optional
import spock.lang.Specification

class GradleVersionExtractorTest extends Specification {

  def "getVersionFromURI"(String location, String version) {
    when:
    Optional<String> versionPart = GradleVersionExtractor.getVersion(new URI(location))

    then:
    versionPart.orNull() == version

    where:
    location                                                                           | version
    'https://services.gradle.org/distributions/gradle-2.2-20141018225750+0000-bin.zip' | '2.2-20141018225750+0000'
    'https://services.gradle.org/distributions/gradle-2.2.1-rc-1-bin.zip'              | '2.2.1-rc-1'
    'https://services.gradle.org/distributions/gradle-2.2.1-bin.zip'                   | '2.2.1'
    'https://services.gradle.org/distributions/gradle-bin.zip'                         | null
  }

}
