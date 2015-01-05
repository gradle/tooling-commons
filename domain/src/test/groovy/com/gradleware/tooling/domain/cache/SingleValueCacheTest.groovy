package com.gradleware.tooling.domain.cache

import com.google.common.base.Optional
import spock.lang.Specification

@SuppressWarnings("GroovyAccessibility")
class SingleValueCacheTest extends Specification {

  def "getIfPresent_stateRESET"() {
    given:
    def cache = new SingleValueCache<String>()
    cache.stateBoundValue.set(SingleValueCache.CacheState.RESET, null, Thread.currentThread())

    when:
    def value = cache.getIfPresent()

    then:
    value == Optional.absent()
    cache.stateBoundValue == new SingleValueCache.StateBoundValue<String>(SingleValueCache.CacheState.RESET, null, Thread.currentThread())
  }

  def "getIfPresent_stateLOADING"() {
    given:
    def cache = new SingleValueCache<String>()
    cache.stateBoundValue.set(SingleValueCache.CacheState.LOADING, null, Thread.currentThread())

    when:
    def value = cache.getIfPresent()

    then:
    value == Optional.absent()
    cache.stateBoundValue == new SingleValueCache.StateBoundValue<String>(SingleValueCache.CacheState.LOADING, null, Thread.currentThread())
  }

  def "getIfPresent_stateLOADED"() {
    given:
    def cache = new SingleValueCache<String>()
    def cachedValue = "someValue"
    cache.stateBoundValue.set(SingleValueCache.CacheState.LOADED, cachedValue, Thread.currentThread())

    when:
    def value = cache.getIfPresent()

    then:
    value == Optional.of(cachedValue)
    cache.stateBoundValue == new SingleValueCache.StateBoundValue<String>(SingleValueCache.CacheState.LOADED, cachedValue, Thread.currentThread())
  }

  def "put_stateRESET"() {
    given:
    def cache = new SingleValueCache<String>()
    cache.stateBoundValue.set(SingleValueCache.CacheState.RESET, null, Thread.currentThread())
    def cachedValue = "someValue"

    when:
    cache.put(cachedValue)

    then:
    cache.getIfPresent() == Optional.of(cachedValue)
    cache.stateBoundValue == new SingleValueCache.StateBoundValue<String>(SingleValueCache.CacheState.LOADED, cachedValue, Thread.currentThread())
  }

  def "put_stateLOADING"() {
    given:
    def cache = new SingleValueCache<String>()
    cache.stateBoundValue.set(SingleValueCache.CacheState.LOADING, null, Thread.currentThread())
    def cachedValue = "someValue"

    when:
    cache.put(cachedValue)

    then:
    cache.getIfPresent() == Optional.of(cachedValue)
    cache.stateBoundValue == new SingleValueCache.StateBoundValue<String>(SingleValueCache.CacheState.LOADED, cachedValue, Thread.currentThread())
  }

  def "put_stateLOADED"() {
    given:
    def cache = new SingleValueCache<String>()
    cache.stateBoundValue.set(SingleValueCache.CacheState.LOADED, "someInitialValue", Thread.currentThread())
    def cachedValue = "someValue"

    when:
    cache.put(cachedValue)

    then:
    cache.getIfPresent() == Optional.of(cachedValue)
    cache.stateBoundValue == new SingleValueCache.StateBoundValue<String>(SingleValueCache.CacheState.LOADED, cachedValue, Thread.currentThread())
  }

  def "invalidate_stateRESET"() {
    given:
    def cache = new SingleValueCache<String>()
    cache.stateBoundValue.set(SingleValueCache.CacheState.RESET, null, Thread.currentThread())

    when:
    cache.invalidate()

    then:
    cache.getIfPresent() == Optional.absent()
    cache.stateBoundValue == new SingleValueCache.StateBoundValue<String>(SingleValueCache.CacheState.RESET, null, Thread.currentThread())
  }

  def "invalidate_stateLOADING"() {
    given:
    def cache = new SingleValueCache<String>()
    cache.stateBoundValue.set(SingleValueCache.CacheState.LOADING, null, Thread.currentThread())

    when:
    cache.invalidate()

    then:
    cache.getIfPresent() == Optional.absent()
    cache.stateBoundValue == new SingleValueCache.StateBoundValue<String>(SingleValueCache.CacheState.RESET, null, Thread.currentThread())
  }

  def "invalidate_stateLOADED"() {
    given:
    def cache = new SingleValueCache<String>()
    cache.stateBoundValue.set(SingleValueCache.CacheState.LOADED, "someValue", Thread.currentThread())

    when:
    cache.invalidate()

    then:
    cache.getIfPresent() == Optional.absent()
    cache.stateBoundValue == new SingleValueCache.StateBoundValue<String>(SingleValueCache.CacheState.RESET, null, Thread.currentThread())
  }

  def "nullChecks"() {
    given:
    def cache = new SingleValueCache<String>()

    when:
    cache.get(null)

    then:
    thrown(IllegalArgumentException)

    when:
    cache.put(null)

    then:
    thrown(IllegalArgumentException)
  }

}
