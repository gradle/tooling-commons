package com.gradleware.tooling.domain.cache

import spock.lang.Specification

@SuppressWarnings("GroovyAccessibility")
class SingleValueCacheCacheStateTest extends Specification {

  def "states"() {
    setup:
    def cacheStateEnumValues = EnumSet.allOf(SingleValueCache.CacheState)

    assert cacheStateEnumValues.size() == 3
    assert cacheStateEnumValues.containsAll([SingleValueCache.CacheState.RESET, SingleValueCache.CacheState.LOADING, SingleValueCache.CacheState.LOADED])
  }

}
