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
