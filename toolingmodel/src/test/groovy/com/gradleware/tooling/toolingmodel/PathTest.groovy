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

    private static boolean smallerThan(String reference, String comparedWith) {
        Path.from(reference).compareTo(Path.from(comparedWith)) < 0
        Path.from(comparedWith).compareTo(Path.from(reference)) > 0
        Path.Comparator.INSTANCE.compare(Path.from(reference), Path.from(comparedWith)) < 0
        Path.Comparator.INSTANCE.compare(Path.from(comparedWith), Path.from(reference)) > 0
    }

}
