/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 \(the "License"\);
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

import com.gradleware.tooling.toolingclient.LongRunningOperationPromise
import org.gradle.tooling.ProgressListener
import spock.lang.Specification

class BaseRequestTest extends Specification {

  def "addingAdditionalProgressListeners"() {
    given:
    ExecutableToolingClient toolingClient = Mock(ExecutableToolingClient)
    def request = new MyBaseRequest(toolingClient)

    def someListener = Mock(ProgressListener)
    def someOtherListener = Mock(ProgressListener)

    when:
    request.progressListeners(someListener)
    request.addProgressListeners(someOtherListener)

    then:
    request.progressListeners == [someListener, someOtherListener]
}

  def "addingAdditionalTypedProgressListeners"() {
    given:
    ExecutableToolingClient toolingClient = Mock(ExecutableToolingClient)
    def request = new MyBaseRequest(toolingClient)

    def someListener = Mock(org.gradle.tooling.events.ProgressListener)
    def someOtherListener = Mock(org.gradle.tooling.events.ProgressListener)

    when:
    request.typedProgressListeners(someListener)
    request.addTypedProgressListeners(someOtherListener)

    then:
    request.typedProgressListeners == [someListener, someOtherListener]
  }

  private static class MyBaseRequest<T> extends BaseRequest<T, MyBaseRequest<T>> {

    MyBaseRequest(ExecutableToolingClient toolingClient) {
      super(toolingClient)
    }

    @Override
    def MyBaseRequest getThis() {
      return this
    }

    @Override
    Object executeAndWait() {
      return null
    }

    @Override
    LongRunningOperationPromise execute() {
      return null
    }
  }

}
