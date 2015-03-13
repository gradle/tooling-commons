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
