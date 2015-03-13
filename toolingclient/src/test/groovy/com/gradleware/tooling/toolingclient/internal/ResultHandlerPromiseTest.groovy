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

package com.gradleware.tooling.toolingclient.internal

import com.gradleware.tooling.toolingclient.Consumer
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
