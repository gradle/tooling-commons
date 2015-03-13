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

package com.gradleware.tooling.toolingmodel.cache

import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import spock.lang.Specification

@SuppressWarnings("GroovyAccessibility")
class SingleValueCacheStateBoundValueTest extends Specification {

  def "newInstance"() {
    setup:
    def stateBoundValue = new SingleValueCache.StateBoundValue<String>()

    assert stateBoundValue == new SingleValueCache.StateBoundValue<String>(SingleValueCache.CacheState.RESET, null, Thread.currentThread())
  }

  def "set_validationSucceeds"() {
    given:
    def stateBoundValue = new SingleValueCache.StateBoundValue<String>()

    when:
    def state = SingleValueCache.CacheState.RESET
    def value = null
    def modifier = Thread.currentThread()
    stateBoundValue.set(state, value, modifier)

    then:
    stateBoundValue == new SingleValueCache.StateBoundValue<String>(state, value, modifier)

    when:
    state = SingleValueCache.CacheState.LOADING
    value = null
    modifier = Thread.currentThread()
    stateBoundValue.set(state, value, modifier)

    then:
    stateBoundValue == new SingleValueCache.StateBoundValue<String>(state, value, modifier)

    when:
    state = SingleValueCache.CacheState.LOADED
    value = "someValue"
    modifier = Thread.currentThread()
    stateBoundValue.set(state, value, modifier)

    then:
    stateBoundValue == new SingleValueCache.StateBoundValue<String>(state, value, modifier)
  }

  def "set_validationFails"() {
    given:
    def stateBoundValue = new SingleValueCache.StateBoundValue<String>()

    when:
    stateBoundValue.set(null, null, Thread.currentThread())

    then:
    def e = thrown(IllegalStateException)
    e.message.toLowerCase().contains("state must not be null")

    when:
    stateBoundValue.set(SingleValueCache.CacheState.RESET, "nonNullValue", Thread.currentThread())

    then:
    e = thrown(IllegalStateException)
    e.message.toLowerCase().contains("value must be null")

    when:
    stateBoundValue.set(SingleValueCache.CacheState.LOADING, "nonNullValue", Thread.currentThread())

    then:
    e = thrown(IllegalStateException)
    e.message.toLowerCase().contains("value must be null")

    when:
    stateBoundValue.set(SingleValueCache.CacheState.LOADED, null, Thread.currentThread())

    then:
    e = thrown(IllegalStateException)
    e.message.toLowerCase().contains("value must not be null")

    when:
    stateBoundValue.set(SingleValueCache.CacheState.RESET, null, null)

    then:
    e = thrown(IllegalStateException)
    e.message.toLowerCase().contains("modifier must not be null")
  }

  def "getValue_correctState"() {
    given:
    def cachedValue = "someValue"
    def stateBoundValue = new SingleValueCache.StateBoundValue<String>()

    when:
    stateBoundValue.set(SingleValueCache.CacheState.LOADED, cachedValue, Thread.currentThread())
    def value = stateBoundValue.getValue()

    then:
    value == cachedValue
  }

  def "getValue_wrongState"() {
    given:
    def stateBoundValue = new SingleValueCache.StateBoundValue<String>()

    when:
    stateBoundValue.set(SingleValueCache.CacheState.RESET, null, Thread.currentThread())
    stateBoundValue.getValue()

    then:
    def e = thrown(IllegalStateException)
    e.message.toLowerCase().contains("value is not available in state")

    when:
    stateBoundValue.set(SingleValueCache.CacheState.LOADING, null, Thread.currentThread())
    stateBoundValue.getValue()

    then:
    e = thrown(IllegalStateException)
    e.message.toLowerCase().contains("value is not available in state")
  }

  def "copy"() {
    setup:
    def stateBoundValue = new SingleValueCache.StateBoundValue<String>()
    def state = SingleValueCache.CacheState.LOADED
    def value = "someValue"
    def modifier = Thread.currentThread()

    when:
    stateBoundValue.set(state, value, modifier)
    def copy = stateBoundValue.copy()

    then:
    !stateBoundValue.is(copy)
    copy == new SingleValueCache.StateBoundValue<String>(state, value, modifier)
  }

  def "equality"() {
    setup:
    EqualsVerifier.forClass(SingleValueCache.StateBoundValue.class).
            withPrefabValues(Thread.class, Thread.currentThread(), new Thread()).
            suppress(Warning.NONFINAL_FIELDS).
            usingGetClass().
            verify();
  }

}
