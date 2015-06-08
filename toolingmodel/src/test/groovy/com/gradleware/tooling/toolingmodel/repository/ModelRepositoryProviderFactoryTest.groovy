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

package com.gradleware.tooling.toolingmodel.repository

import com.gradleware.tooling.toolingclient.ToolingClient
import org.gradle.internal.Factory
import spock.lang.Specification

class ModelRepositoryProviderFactoryTest extends Specification {

  def "create with single-args constructor"() {
    setup:
    def toolingClient = Mock(ToolingClient)

    when:
    def modelRepositoryProvider = ModelRepositoryProviderFactory.create(toolingClient)

    then:
    noExceptionThrown()
    modelRepositoryProvider != null
  }

  def "create with two-args constructor"() {
    setup:
    def toolingClient = Mock(ToolingClient)

    when:
    def modelRepositoryProvider = ModelRepositoryProviderFactory.create(toolingClient, Environment.STANDALONE)

    then:
    noExceptionThrown()
    modelRepositoryProvider != null
  }

  def "create with three-args constructor"() {
    setup:
    def toolingClient = Mock(ToolingClient)
    def factory = Mock(Factory)

    when:
    def modelRepositoryProvider = ModelRepositoryProviderFactory.create(toolingClient, Environment.STANDALONE, factory)

    then:
    noExceptionThrown()
    modelRepositoryProvider != null
  }

}
