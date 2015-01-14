package com.gradleware.tooling.toolingapi.internal

import com.gradleware.tooling.toolingapi.Consumer
import org.gradle.tooling.GradleConnectionException
import spock.lang.Specification

class ResultHandlerPromiseTest extends Specification {

  def "completionHandlerRegisteredBeforeCompletionEvent"(Object resultValue) {
    setup:
    Consumer<Object> completionConsumer = Mock(Consumer)
    def promise = new ResultHandlerPromise<Object>()

    when:
    promise.onComplete(completionConsumer)
    promise.getResultHandler().onComplete(resultValue)

    then:
    1 * completionConsumer.accept(resultValue)

    where:
    resultValue << ["result", null]
  }

  def "completionHandlerRegisteredAfterCompletionEvent"(Object resultValue) {
    setup:
    Consumer<Object> completionConsumer = Mock(Consumer)
    def promise = new ResultHandlerPromise<Object>()

    when:
    promise.getResultHandler().onComplete(resultValue)
    promise.onComplete(completionConsumer)

    then:
    1 * completionConsumer.accept(resultValue)

    where:
    resultValue << ["result", null]
  }

  def "failureHandlerRegisteredBeforeFailureEvent"(GradleConnectionException exception) {
    setup:
    Consumer<GradleConnectionException> completionConsumer = Mock(Consumer)
    def promise = new ResultHandlerPromise<Object>()

    when:
    promise.onFailure(completionConsumer)
    promise.getResultHandler().onFailure(exception)

    then:
    1 * completionConsumer.accept(exception)

    where:
    exception << [new GradleConnectionException("error")]
  }

  def "failureHandlerRegisteredAfterFailureEvent"(GradleConnectionException exception) {
    setup:
    Consumer<GradleConnectionException> completionConsumer = Mock(Consumer)
    def promise = new ResultHandlerPromise<Object>()

    when:
    promise.getResultHandler().onFailure(exception)
    promise.onFailure(completionConsumer)

    then:
    1 * completionConsumer.accept(exception)

    where:
    exception << [new GradleConnectionException("error")]
  }

}
