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

package com.gradleware.tooling.toolingmodel

import nl.jqno.equalsverifier.EqualsVerifier
import spock.lang.Specification

class PathTest extends Specification {

    def "constructed from path string"() {
        setup:
        def path = Path.from(':a:b:c')
        assert path.getPath() == ':a:b:c'
        assert path == Path.from(path.getPath())
    }

    def "cannot be constructed from invalid paths"() {
        when:
        Path.from('does_not_start_with_colon')

        then:
        thrown IllegalArgumentException
    }

    def "paths are first compared by length then lexicographically"() {
        setup:
        assert smallerThan(':a:b', ':a:b:c')
        assert smallerThan(':z:z', ':a:a:a')
        assert smallerThan(':a:b:c', ':a:d:c')
        assert smallerThan(':a:b:c', ':a:b:d')
        assert Path.from(':a:b:c').compareTo(Path.from(':a:b:c')) == 0
    }

    def "equalsAndHashCode"() {
        setup:
        def path = Path.from(':a:b:c')
        assert path == path
        assert path.hashCode() == path.hashCode()
    }

    def "equality"() {
        setup:
        EqualsVerifier.forClass(Path.class).usingGetClass().verify();
    }

    def "dropLastSegment"() {
        expect:
        Path.from(":").dropLastSegment().path == ':'
        Path.from(":a").dropLastSegment().path == ':'
        Path.from(":a:b").dropLastSegment().path == ':a'
        Path.from(":a:b:c").dropLastSegment().path == ':a:b'
    }

    private static boolean smallerThan(String reference, String comparedWith) {
        Path.from(reference).compareTo(Path.from(comparedWith)) < 0
        Path.from(comparedWith).compareTo(Path.from(reference)) > 0
        Path.Comparator.INSTANCE.compare(Path.from(reference), Path.from(comparedWith)) < 0
        Path.Comparator.INSTANCE.compare(Path.from(comparedWith), Path.from(reference)) > 0
    }

}
