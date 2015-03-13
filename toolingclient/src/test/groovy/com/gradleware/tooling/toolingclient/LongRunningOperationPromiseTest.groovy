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

package com.gradleware.tooling.toolingclient

import org.gradle.tooling.BuildActionExecuter
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.ModelBuilder
import spock.lang.Specification

class LongRunningOperationPromiseTest extends Specification {

  def "forModelBuilder"() {
    setup:
    ModelBuilder<Object> modelBuilder = Mock(ModelBuilder)
    Consumer<Object> resultConsumer = Mock(Consumer)
    Consumer<GradleConnectionException> exceptionConsumer = Mock(Consumer)
    LongRunningOperationPromise<Object> promise = LongRunningOperationPromise.forModelBuilder(modelBuilder)
    def result = "hello"
    def exception = new GradleConnectionException("error")

    when:
    promise.onComplete(resultConsumer)
    promise.getResultHandler().onComplete(result)

    then:
    1 * resultConsumer.accept(result)

    when:
    promise.onFailure(exceptionConsumer)
    promise.getResultHandler().onFailure(exception)

    then:
    1 * exceptionConsumer.accept(exception)
  }

  def "forBuildLauncher"() {
    setup:
    BuildLauncher buildLauncher = Mock(BuildLauncher)
    Consumer<Void> resultConsumer = Mock(Consumer)
    Consumer<GradleConnectionException> exceptionConsumer = Mock(Consumer)
    LongRunningOperationPromise<Void> promise = LongRunningOperationPromise.forBuildLauncher(buildLauncher)
    def result = null
    def exception = new GradleConnectionException("error")

    when:
    promise.onComplete(resultConsumer)
    promise.getResultHandler().onComplete(result)

    then:
    1 * resultConsumer.accept(result)

    when:
    promise.onFailure(exceptionConsumer)
    promise.getResultHandler().onFailure(exception)

    then:
    1 * exceptionConsumer.accept(exception)
  }

  def "forBuildActionExecuter"() {
    setup:
    BuildActionExecuter<Object> buildActionExecuter = Mock(BuildActionExecuter)
    Consumer<Object> resultConsumer = Mock(Consumer)
    Consumer<GradleConnectionException> exceptionConsumer = Mock(Consumer)
    LongRunningOperationPromise<Object> promise = LongRunningOperationPromise.forBuildActionExecuter(buildActionExecuter)
    def result = "result"
    def exception = new GradleConnectionException("error")

    when:
    promise.onComplete(resultConsumer)
    promise.getResultHandler().onComplete(result)

    then:
    1 * resultConsumer.accept(result)

    when:
    promise.onFailure(exceptionConsumer)
    promise.getResultHandler().onFailure(exception)

    then:
    1 * exceptionConsumer.accept(exception)
  }

}
