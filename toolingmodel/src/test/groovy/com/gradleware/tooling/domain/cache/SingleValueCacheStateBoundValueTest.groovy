package com.gradleware.tooling.domain.cache

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
