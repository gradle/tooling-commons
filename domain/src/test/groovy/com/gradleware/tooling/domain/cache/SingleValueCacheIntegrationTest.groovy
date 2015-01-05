package com.gradleware.tooling.domain.cache

import com.google.common.base.Optional
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

@SuppressWarnings("GroovyAccessibility")
class SingleValueCacheIntegrationTest extends Specification {

  @Shared
  ExecutorService executors

  def setupSpec() {
    executors = Executors.newCachedThreadPool()
  }

  def cleanupSpec() {
    executors.shutdown
    executors.awaitTermination(5, TimeUnit.SECONDS)
  }

  def "getIfPresent_stateRESET"() {
    given:
    def cache = new SingleValueCache<String>()

    when:
    def value = cache.getIfPresent()

    then:
    value == Optional.absent()
  }

  def "getIfPresent_stateLOADING"() {
    given:
    def cache = new SingleValueCache<String>()
    def loading = new CountDownLatch(1)
    def operationInvoked = new CountDownLatch(1)
    def callable = Mock(Callable)
    callable.call() >> { loading.countDown(); operationInvoked.await(); "someValue" }

    // wait until a separate thread starts loading the cache value
    def thread = runInSeparateThread { cache.get(callable) }
    loading.await()

    when:
    // while the other thread is still loading the cache value, query the cache value
    def value = cache.getIfPresent()

    then:
    // let the other thread finish before asserting
    operationInvoked.countDown()
    thread.get()
    value == Optional.absent()
  }

  def "getIfPresent_stateLOADED"() {
    given:
    def cache = new SingleValueCache<String>()
    def cachedValue = "someValue"
    cache.put(cachedValue)

    when:
    def value = cache.getIfPresent()

    then:
    value == Optional.of(cachedValue)
  }

  def "put_stateRESET"() {
    given:
    def cache = new SingleValueCache<String>()
    def cachedValue = "someValue"

    when:
    cache.put(cachedValue)

    then:
    cache.getIfPresent() == Optional.of(cachedValue)
  }

  def "put_stateLOADING"() {
    given:
    def cache = new SingleValueCache<String>()
    def loading = new CountDownLatch(1)
    def operationInvoked = new CountDownLatch(1)
    def callable = Mock(Callable)
    callable.call() >> { loading.countDown(); operationInvoked.await(); "someValue" }

    // wait until a separate thread starts loading the cache value
    def thread = runInSeparateThread { cache.get(callable) }
    loading.await()

    def cachedValue = "someValue"

    when:
    // while the other thread is still loading the cache value, query the cache value
    cache.put(cachedValue)

    then:
    // let the other thread finish before asserting
    operationInvoked.countDown()
    thread.get()
    cache.getIfPresent() == Optional.of(cachedValue)
  }

  def "put_stateLOADED"() {
    given:
    def cache = new SingleValueCache<String>()
    cache.put("someInitialValue")
    def cachedValue = "someValue"

    when:
    cache.put(cachedValue)

    then:
    cache.getIfPresent() == Optional.of(cachedValue)
  }

  def "put_nullValueNotAllowed"() {
    given:
    def cache = new SingleValueCache<String>()

    when:
    cache.put(null)

    then:
    thrown(IllegalArgumentException)
  }

  def "invalidate_stateRESET"() {
    given:
    def cache = new SingleValueCache<String>()

    when:
    cache.invalidate()

    then:
    cache.getIfPresent() == Optional.absent()
  }

  // todo (etst) unignore
  @Ignore
  def "invalidate_stateLOADING"() {
    given:
    def cache = new SingleValueCache<String>()
    def loading = new CountDownLatch(1)
    def operationInvoked = new CountDownLatch(1)
    def callable = Mock(Callable)
    callable.call() >> { loading.countDown(); operationInvoked.await(); "someValue" }

    // wait until a separate thread starts loading the cache value
    def thread = runInSeparateThread { cache.get(callable) }
    loading.await()

    when:
    // while the other thread is still loading the cache value, query the cache value
    cache.invalidate()

    then:
    // let the other thread finish before asserting
    operationInvoked.countDown()
    thread.get()
    cache.getIfPresent() == Optional.absent()
  }

  def "invalidate_stateLOADED"() {
    given:
    def cache = new SingleValueCache<String>()
    cache.put("someInitialValue")

    when:
    cache.invalidate()

    then:
    cache.getIfPresent() == Optional.absent()
  }

  private Future<?> runInSeparateThread(Closure action) {
    return executors.submit(action as Callable)
  }

}
