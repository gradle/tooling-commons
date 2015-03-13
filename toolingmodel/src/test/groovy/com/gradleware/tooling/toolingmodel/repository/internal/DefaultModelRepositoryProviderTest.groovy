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

package com.gradleware.tooling.toolingmodel.repository.internal

import com.google.common.collect.ImmutableList
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes
import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.ToolingModelToolingClientSpecification
import com.gradleware.tooling.toolingclient.GradleDistribution
import org.junit.Rule

class DefaultModelRepositoryProviderTest extends ToolingModelToolingClientSpecification {

  @Rule
  TestDirectoryProvider directoryProvider = new TestDirectoryProvider();

  def setup() {
    directoryProvider.createFile('settings.gradle');
    directoryProvider.createFile('build.gradle') << 'task myTask {}'
  }

  def "getModelRepository"() {
    setup:
    def modelRepositoryProvider = new DefaultModelRepositoryProvider(toolingClient)

    def attributesOne = new FixedRequestAttributes(directoryProvider.testDirectory, null, GradleDistribution.fromBuild(), null, ImmutableList.of(), ImmutableList.of())
    def attributesTwo = new FixedRequestAttributes(directoryProvider.testDirectory, null, GradleDistribution.forVersion('1.12'), null, ImmutableList.of(), ImmutableList.of())

    assert modelRepositoryProvider.getModelRepository(attributesOne).is(modelRepositoryProvider.getModelRepository(attributesOne))
    assert modelRepositoryProvider.getModelRepository(attributesTwo).is(modelRepositoryProvider.getModelRepository(attributesTwo))
    assert !modelRepositoryProvider.getModelRepository(attributesOne).is(modelRepositoryProvider.getModelRepository(attributesTwo))
  }

}
