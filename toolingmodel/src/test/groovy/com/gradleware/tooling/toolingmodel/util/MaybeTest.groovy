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
