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

import org.gradle.tooling.BuildAction
import spock.lang.Specification

class DefaultBuildActionRequestTest extends Specification {

  def "deriveForBuildAction"() {
    setup:
    ExecutableToolingClient toolingClient = Mock(ExecutableToolingClient)
    BuildAction<Void> buildAction = Mock(BuildAction.class)
    DefaultBuildActionRequest<Void> buildActionRequest = new DefaultBuildActionRequest<Void>(toolingClient, buildAction)

    BuildAction<String> newBuildAction = Mock(BuildAction.class)
    DefaultBuildActionRequest<String> copy = buildActionRequest.deriveForBuildAction(newBuildAction)

    assert copy.buildAction == newBuildAction
    assert copy.cancellationToken.is(buildActionRequest.cancellationToken)
  }

}
