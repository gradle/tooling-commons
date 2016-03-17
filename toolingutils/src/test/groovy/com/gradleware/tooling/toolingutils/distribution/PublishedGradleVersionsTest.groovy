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
import spock.lang.Shared;
import spock.lang.Specification

import com.gradleware.tooling.toolingutils.distribution.PublishedGradleVersions.LookupStrategy;

class PublishedGradleVersionsTest extends Specification {

    @Shared File CACHE_FILE = PublishedGradleVersions.cacheFile

    def setup() {
        CACHE_FILE.delete()
    }

    def "Can parse valid version info file"() {
        setup:
        def json = PublishedGradleVersions.class.getResource("versions_20150202.json")
        PublishedGradleVersions publishedVersions = PublishedGradleVersions.create(json.text)
        assert publishedVersions.versions == [
            '2.3-rc-1',
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
            '1.0'
        ].collect { GradleVersion.version(it) }
    }

    def "When REMOTE strategy is specified, no cache is written"() {
        when:
        PublishedGradleVersions publishedVersions = PublishedGradleVersions.create(LookupStrategy.REMOTE)
        then:
        publishedVersions.versions.size() >= 18
        !CACHE_FILE.exists()
    }

    def "When REMOTE_IF_NOT_CACHED strategy is specified, a cache file is written"() {
        when:
        PublishedGradleVersions publishedVersions = PublishedGradleVersions.create(LookupStrategy.REMOTE_IF_NOT_CACHED)
        then:
        publishedVersions.versions.size() >= 18
        CACHE_FILE.exists()
    }

    def "The cache file is used if availabe and not outdated"() {
        setup:
        PublishedGradleVersions.create(LookupStrategy.REMOTE_IF_NOT_CACHED)
        def lastModified = CACHE_FILE.lastModified()
        Thread.sleep(500)

        when:
        def publishedVersions = PublishedGradleVersions.create(LookupStrategy.REMOTE_IF_NOT_CACHED)
        def lastModifiedAfterCacheHit = CACHE_FILE.lastModified()

        then:
        publishedVersions.versions.size() >= 18
        assert lastModifiedAfterCacheHit == lastModified
    }

    def "No remote download is attempted if CACHE_ONLY is specified"() {
        when:
        PublishedGradleVersions publishedVersions = PublishedGradleVersions.create(LookupStrategy.CACHED_ONLY)
        then:
        thrown IllegalStateException
    }
}
