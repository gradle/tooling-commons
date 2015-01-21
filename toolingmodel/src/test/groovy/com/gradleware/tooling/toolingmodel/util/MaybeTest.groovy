package com.gradleware.tooling.toolingmodel.util

import org.spockframework.util.Assert
import spock.lang.Specification

class MaybeTest extends Specification {

  def "present can reference non-null value"() {
    setup:
    def reference = "something"
    def maybe = Maybe.of(reference)

    assert maybe.isPresent()
    assert maybe.get() == "something"
    assert maybe.or("default") == "something"
  }

  def "present can reference null value"() {
    setup:
    def reference = null
    def maybe = Maybe.of(reference)

    assert maybe.isPresent()
    assert maybe.get() == null
    assert maybe.or("default") == null
  }

  def "absent does not reference a value"() {
    setup:
    def maybe = Maybe.absent()

    assert !maybe.isPresent()
    assert maybe.or("default") == "default"

    try {
      maybe.get()
      Assert.fail "isPresent should throw exception on absent"
    } catch (Exception e) {
      assert e.class == IllegalStateException
    }
  }

}
