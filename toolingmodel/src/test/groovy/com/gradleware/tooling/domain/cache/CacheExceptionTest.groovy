package com.gradleware.tooling.domain.cache

import spock.lang.Specification

class CacheExceptionTest extends Specification {

  @SuppressWarnings("GroovyUnreachableStatement")
  def "publicConstructors"() {
    given:
    def msg = "msg"
    def cause = new IllegalStateException()

    when:
    throw new CacheException(msg, cause)

    then:
    def e = thrown(CacheException)
    e.message == "msg"
    e.cause == cause

    when:
    throw new CacheException(cause)

    then:
    e = thrown(CacheException)
    e.message == cause.toString()
    e.cause == cause
  }

}
