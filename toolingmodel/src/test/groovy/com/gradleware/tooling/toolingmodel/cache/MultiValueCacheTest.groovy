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

import java.util.concurrent.Callable

@SuppressWarnings(["GroovyAccessibility", "GroovyAssignabilityCheck"])
class MultiValueCacheTest extends Specification {

  def "getIfPresent_noHit"() {
    given:
    def cache = new MultiValueCache<Integer, String>()
    def cachedKey = 1

    when:
    def value = cache.getIfPresent(cachedKey)

    then:
    value == Optional.absent()
    cache.singleValueCaches.mapping.size() == 0
  }

  def "getIfPresent_hit"() {
    given:
    def cache = new MultiValueCache<Integer, String>()
    def cacheKey = 1
    def cachedValue = "someValue"
    cache.put(cacheKey, cachedValue)

    when:
    def value = cache.getIfPresent(cacheKey)

    then:
    value == Optional.of(cachedValue)
    cache.singleValueCaches.mapping.size() == 1
  }

  def "get_loadValue"() {
    given:
    def cache = new MultiValueCache<Integer, String>()
    def cachedKey = 1
    def cachedValue = "someValue"
    Callable callable = Mock(Callable)
    1 * callable.call() >> { cachedValue }

    when:
    def value = cache.get(cachedKey, callable)

    then:
    value == cachedValue
    cache.singleValueCaches.mapping.size() == 1
  }

  def "get_valueAlreadyLoaded"() {
    given:
    def cache = new MultiValueCache<Integer, String>()
    def cachedKey = 1
    def cachedValue = "someValue"
    cache.put(cachedKey, cachedValue)
    Callable mock = Mock(Callable)

    when:
    def value = cache.get(cachedKey, mock)

    then:
    value == cachedValue
    cache.singleValueCaches.mapping.size() == 1
    0 * mock.call()
  }

  def "put_insertKeyAndValue"() {
    given:
    def cache = new MultiValueCache<Integer, String>()
    def cachedKey = 1
    def cachedValue = "someValue"

    when:
    cache.put(cachedKey, cachedValue)

    then:
    cache.getIfPresent(cachedKey) == Optional.of(cachedValue)
    cache.singleValueCaches.mapping.size() == 1
  }

  def "put_updateValue"() {
    given:
    def cache = new MultiValueCache<Integer, String>()
    def cachedKey = 1
    def cachedValue = "someValue"
    cache.put(cachedKey, "someInitialValue")

    when:
    cache.put(cachedKey, cachedValue)

    then:
    cache.getIfPresent(cachedKey) == Optional.of(cachedValue)
    cache.singleValueCaches.mapping.size() == 1
  }

  def "invalidate_noHit"() {
    given:
    def cache = new MultiValueCache<Integer, String>()

    when:
    cache.invalidate(1)

    then:
    cache.singleValueCaches.mapping.size() == 0
  }

  def "invalidate_hit"() {
    given:
    def cache = new MultiValueCache<Integer, String>()
    cache.put(1, "someValue")

    when:
    cache.invalidate(1)

    then:
    cache.getIfPresent(1) == Optional.absent()
    cache.singleValueCaches.mapping.size() == 0
  }

  def "nullChecks"() {
    given:
    def cache = new MultiValueCache<Integer, String>()

    when:
    cache.getIfPresent(null)

    then:
    thrown(IllegalArgumentException)

    when:
    cache.get(null, Mock(Callable))

    then:
    thrown(IllegalArgumentException)

    when:
    cache.get(1, null)

    then:
    thrown(IllegalArgumentException)

    when:
    cache.put(null, "someValue")

    then:
    thrown(IllegalArgumentException)

    when:
    cache.put(1, null)

    then:
    thrown(IllegalArgumentException)

    when:
    cache.invalidate(null)

    then:
    thrown(IllegalArgumentException)
  }

}
