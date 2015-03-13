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

package com.gradleware.tooling.toolingutils.distribution

import org.gradle.util.GradleVersion
import spock.lang.Specification

class PublishedGradleVersionsTest extends Specification {

  private static final File CACHE_FILE = new File(System.getProperty("user.home"), ".tooling/gradle/versions.json")

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

  def "fetch version info remotely without caching"() {
    setup:
    PublishedGradleVersions publishedVersions = PublishedGradleVersions.create(false)
    assert publishedVersions.versions.size() >= 18
  }

  def "fetch version info remotely with caching"() {
    setup:
    CACHE_FILE.delete()
    PublishedGradleVersions publishedVersions = PublishedGradleVersions.create(true)
    assert publishedVersions.versions.size() >= 18
    assert CACHE_FILE.exists()
  }

  def "fetch version info remotely with caching - only update cache file after download"() {
    when:
    CACHE_FILE.delete()

    then:
    assert !CACHE_FILE.exists()

    when:
    PublishedGradleVersions.create(true)
    def lastModified = CACHE_FILE.lastModified()

    then:
    assert CACHE_FILE.exists()

    when:
    Thread.sleep(2000)
    PublishedGradleVersions.create(true)
    def lastModifiedAfterCacheHit = CACHE_FILE.lastModified()

    then:
    assert CACHE_FILE.exists()
    assert lastModifiedAfterCacheHit == lastModified
  }

}
