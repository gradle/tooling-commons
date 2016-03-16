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

import org.junit.Rule

import com.google.common.collect.ImmutableList
import com.google.common.collect.Sets;

import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.ToolingModelToolingClientSpecification
import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes

class DefaultModelRepositoryProviderTest extends ToolingModelToolingClientSpecification {

    @Rule
    TestDirectoryProvider projectA = new TestDirectoryProvider()
    @Rule
    TestDirectoryProvider projectB = new TestDirectoryProvider()

    def setup() {
        [projectA, projectB].each { project ->
            project.createFile('settings.gradle')
            project.createFile('build.gradle') << 'task myTask {}'
        }
    }

    def "getModelRepository"() {
        setup:
        def modelRepositoryProvider = new DefaultModelRepositoryProvider(toolingClient)

        def attributesOne = new FixedRequestAttributes(projectA.testDirectory, null, GradleDistribution.fromBuild(), null, ImmutableList.of(), ImmutableList.of())
        def attributesTwo = new FixedRequestAttributes(projectA.testDirectory, null, GradleDistribution.forVersion('1.12'), null, ImmutableList.of(), ImmutableList.of())
        def attributesThree = new FixedRequestAttributes(projectB.testDirectory, null, GradleDistribution.fromBuild(), null, ImmutableList.of(), ImmutableList.of())

        assert modelRepositoryProvider.getModelRepository(attributesOne).is(modelRepositoryProvider.getModelRepository(attributesOne))
        assert modelRepositoryProvider.getModelRepository(attributesTwo).is(modelRepositoryProvider.getModelRepository(attributesTwo))
        assert !modelRepositoryProvider.getModelRepository(attributesOne).is(modelRepositoryProvider.getModelRepository(attributesTwo))
        assert !modelRepositoryProvider.getModelRepository(attributesOne).is(modelRepositoryProvider.getModelRepository(attributesThree))
    }

    def "Composite with one root project"() {
        setup:
        def modelRepositoryProvider = new DefaultModelRepositoryProvider(toolingClient)

        def attributesOne = new FixedRequestAttributes(projectA.testDirectory, null, GradleDistribution.fromBuild(), null, ImmutableList.of(), ImmutableList.of())
        def attributesTwo = new FixedRequestAttributes(projectA.testDirectory, null, GradleDistribution.forVersion('1.12'), null, ImmutableList.of(), ImmutableList.of())
        def attributesThree = new FixedRequestAttributes(projectB.testDirectory, null, GradleDistribution.fromBuild(), null, ImmutableList.of(), ImmutableList.of())

        assert modelRepositoryProvider.getCompositeModelRepository(Sets.newHashSet(attributesOne)).is(modelRepositoryProvider.getCompositeModelRepository(Sets.newHashSet(attributesOne)))
        assert modelRepositoryProvider.getCompositeModelRepository(Sets.newHashSet(attributesTwo)).is(modelRepositoryProvider.getCompositeModelRepository(Sets.newHashSet(attributesTwo)))
        assert !modelRepositoryProvider.getCompositeModelRepository(Sets.newHashSet(attributesOne)).is(modelRepositoryProvider.getCompositeModelRepository(Sets.newHashSet(attributesTwo)))
        assert !modelRepositoryProvider.getCompositeModelRepository(Sets.newHashSet(attributesOne)).is(modelRepositoryProvider.getCompositeModelRepository(Sets.newHashSet(attributesThree)))
    }

    def "Composite with multiple root projects"() {
        setup:
        def modelRepositoryProvider = new DefaultModelRepositoryProvider(toolingClient)

        def attributesOne = new FixedRequestAttributes(projectA.testDirectory, null, GradleDistribution.fromBuild(), null, ImmutableList.of(), ImmutableList.of())
        def attributesTwo = new FixedRequestAttributes(projectA.testDirectory, null, GradleDistribution.forVersion('1.12'), null, ImmutableList.of(), ImmutableList.of())
        def attributesThree = new FixedRequestAttributes(projectB.testDirectory, null, GradleDistribution.fromBuild(), null, ImmutableList.of(), ImmutableList.of())

        assert modelRepositoryProvider.getCompositeModelRepository(Sets.newHashSet(attributesOne, attributesThree)).is(modelRepositoryProvider.getCompositeModelRepository(Sets.newHashSet(attributesOne, attributesThree)))
        assert !modelRepositoryProvider.getCompositeModelRepository(Sets.newHashSet(attributesOne, attributesThree)).is(modelRepositoryProvider.getCompositeModelRepository(Sets.newHashSet(attributesTwo, attributesThree)))
        assert !modelRepositoryProvider.getCompositeModelRepository(Sets.newHashSet(attributesOne)).is(modelRepositoryProvider.getCompositeModelRepository(Sets.newHashSet(attributesOne, attributesThree)))
    }

}
