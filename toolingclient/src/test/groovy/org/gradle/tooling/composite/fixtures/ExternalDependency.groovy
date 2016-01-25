package org.gradle.tooling.composite.fixtures

import groovy.transform.TupleConstructor

@TupleConstructor
class ExternalDependency {
    final String group
    final String name
    final String version

    String toString() {
        "$group:$name:$version"
    }
}
