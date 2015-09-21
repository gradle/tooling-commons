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

import org.gradle.tooling.TestLauncher
import org.gradle.tooling.events.test.TestOperationDescriptor
import spock.lang.Specification

@SuppressWarnings("GroovyAssignabilityCheck")
class TestConfigTest extends Specification {

  def "forJvmTestClassesVarArgs"() {
    setup:
    TestLauncher testLauncher = Mock(TestLauncher.class)
    def tests = TestConfig.forJvmTestClasses("alpha")

    when:
    tests.apply(testLauncher)

    then:
    1 * testLauncher.withJvmTestClasses(["alpha"])
    0 * testLauncher.withJvmTestMethods(*_)
    1 * testLauncher.withTests([])
  }

  def "forJvmTestClassesIterable"() {
    setup:
    TestLauncher testLauncher = Mock(TestLauncher.class)
    def tests = TestConfig.forJvmTestClasses(Arrays.asList("alpha"))

    when:
    tests.apply(testLauncher)

    then:
    1 * testLauncher.withJvmTestClasses(["alpha"])
    0 * testLauncher.withJvmTestMethods(*_)
    1 * testLauncher.withTests([])
  }

  def "forJvmTestMethodsVarArgs"() {
    setup:
    TestLauncher testLauncher = Mock(TestLauncher.class)
    def tests = TestConfig.forJvmTestMethods("alpha", "beta")

    when:
    tests.apply(testLauncher)

    then:
    1 * testLauncher.withJvmTestClasses([])
    1 * testLauncher.withJvmTestMethods("alpha", ["beta"])
    1 * testLauncher.withTests([])
  }

  def "forJvmTestMethodsIterable"() {
    setup:
    TestLauncher testLauncher = Mock(TestLauncher.class)
    def tests = TestConfig.forJvmTestMethods("alpha", Arrays.asList("beta"))

    when:
    tests.apply(testLauncher)

    then:
    1 * testLauncher.withJvmTestClasses([])
    1 * testLauncher.withJvmTestMethods("alpha", ["beta"])
    1 * testLauncher.withTests([])
  }

  def "forTestsVarArgs"() {
    setup:
    TestLauncher testLauncher = Mock(TestLauncher.class)
    def testDescriptor = Mock(TestOperationDescriptor.class)
    def tests = TestConfig.forTests(testDescriptor)

    when:
    tests.apply(testLauncher)

    then:
    1 * testLauncher.withTests([testDescriptor])
    0 * testLauncher.withJvmTestMethods(*_)
    1 * testLauncher.withJvmTestClasses([])
  }

  def "forTestsIterable"() {
    setup:
    TestLauncher testLauncher = Mock(TestLauncher.class)
    def testDescriptor = Mock(TestOperationDescriptor.class)
    def tests = TestConfig.forTests(Arrays.asList(testDescriptor))

    when:
    tests.apply(testLauncher)

    then:
    1 * testLauncher.withTests([testDescriptor])
    0 * testLauncher.withJvmTestMethods(*_)
    1 * testLauncher.withJvmTestClasses([])
  }

}
