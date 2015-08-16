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

import com.gradleware.tooling.toolingclient.TestConfig
import spock.lang.Specification

class DefaultTestLaunchRequestTest extends Specification {

  def "deriveForTests"() {
    setup:
    ExecutableToolingClient toolingClient = Mock(ExecutableToolingClient)
    DefaultTestLaunchRequest testLaunchRequest = new DefaultTestLaunchRequest(toolingClient, TestConfig.forJvmTestClasses('com.foo.MyClass'))

    def newTests = TestConfig.forJvmTestClasses('org.bar.MyOtherClass')
    DefaultTestLaunchRequest copy = testLaunchRequest.deriveForTests(newTests)

    assert copy.tests == newTests
    assert copy.cancellationToken.is(testLaunchRequest.cancellationToken)
  }

}
