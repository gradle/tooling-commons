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

package com.gradleware.tooling.toolingclient;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.gradle.tooling.TestLauncher;
import org.gradle.tooling.events.test.TestOperationDescriptor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the {@link TestOperationDescriptor} instances to execute through a Gradle build.
 *
 * @author Donát Csikós
 */
public final class TestConfig {

    private final ImmutableList<String> jvmTestClasses;
    private final ImmutableMap<String, List<String>> jvmTestMethods;
    private final ImmutableList<? extends TestOperationDescriptor> tests;

    private TestConfig(List<String> jvmTestClasses, Map<String, List<String>> jvmTestMethods, List<? extends TestOperationDescriptor> tests) {
        this.jvmTestClasses = ImmutableList.copyOf(jvmTestClasses);
        this.jvmTestMethods = ImmutableMap.copyOf(jvmTestMethods);
        this.tests = ImmutableList.copyOf(tests);

        checkNotAllEmpty(jvmTestClasses, jvmTestMethods, tests);
    }

    private void checkNotAllEmpty(List<String> jvmTestClasses, Map<String, List<String>> jvmTestMethods, List<? extends TestOperationDescriptor> tests) {
        Preconditions.checkArgument(!jvmTestClasses.isEmpty() || !jvmTestMethods.isEmpty() || !tests.isEmpty(),
                "Either JVM test classes, JVM test methods, test operations, or any combination of them must be specified.");
    }

    /**
     * Configures the specified test launcher with this test config.
     *
     * @param testLauncher the test launcher to configure
     */
    public void apply(TestLauncher testLauncher) {
        Preconditions.checkNotNull(testLauncher);
        testLauncher.withJvmTestClasses(this.jvmTestClasses);
        for(String jvmTestClass : this.jvmTestMethods.keySet()) {
            testLauncher.withJvmTestMethods(jvmTestClass, this.jvmTestMethods.get(jvmTestClass));
        }
        testLauncher.withTests(this.tests);
    }

    /**
     * Specifies the test classes to be executed.
     *
     * @param jvmTestClasses the names of the test classes to be executed
     * @return a new instance
     */
    public static TestConfig forJvmTestClasses(String... jvmTestClasses) {
        return new TestConfig.Builder().jvmTestClasses(jvmTestClasses).build();
    }

    /**
     * Specifies the test classes to be executed.
     *
     * @param jvmTestClasses the names of the test classes to be executed
     * @return a new instance
     */
    public static TestConfig forJvmTestClasses(Iterable<String> jvmTestClasses) {
        return new TestConfig.Builder().jvmTestClasses(jvmTestClasses).build();
    }

    /**
     * Specifies the test methods to be executed.
     *
     * @param jvmTestClass the name of the class containing the test methods
     * @param jvmTestMethods the names of the test methods to be executed
     * @return a new instance
     */
    public static TestConfig forJvmTestMethods(String jvmTestClass, String... jvmTestMethods) {
        return new Builder().jvmTestMethods(jvmTestClass, jvmTestMethods).build();
    }

    /**
     * Specifies the test methods to be executed.
     *
     * @param jvmTestClass the name of the class containing the test methods
     * @param jvmTestMethods the names of the test methods to be executed
     * @return a new instance
     */
    public static TestConfig forJvmTestMethods(String jvmTestClass, Iterable<String> jvmTestMethods) {
        return new Builder().jvmTestMethods(jvmTestClass, jvmTestMethods).build();
    }

    /**
     * Specifies the tests to be executed.
     *
     * @param tests the tests to be executed
     * @return a new instance
     */
    public static TestConfig forTests(TestOperationDescriptor... tests) {
        return new Builder().tests(tests).build();
    }

    /**
     * Specifies the tests to be executed.
     *
     * @param tests the tests to be executed
     * @return a new instance
     */
    public static TestConfig forTests(Iterable<? extends TestOperationDescriptor> tests) {
        return new Builder().tests(tests).build();
    }

    /**
     * Builder to build {@code TestConfig} instances that accumulate multiple types of tests to execute.
     */
    public static final class Builder {

        private ImmutableList.Builder<String> jvmTestClasses;
        private ImmutableMap.Builder<String, List<String>> jvmTestMethods;
        private ImmutableList.Builder<TestOperationDescriptor> tests;

        public Builder() {
            this.jvmTestClasses = ImmutableList.builder();
            this.jvmTestMethods = ImmutableMap.builder();
            this.tests = ImmutableList.builder();
        }

        /**
         * Adds the test classes to be executed.
         *
         * @param jvmTestClasses the names of the test classes to be executed
         * @return this builder
         */
        public Builder jvmTestClasses(String... jvmTestClasses) {
            this.jvmTestClasses.addAll(Arrays.asList(jvmTestClasses));
            return this;
        }

        /**
         * Adds the test classes to be executed.
         *
         * @param jvmTestClasses the names of the test classes to be executed
         * @return this builder
         */
        public Builder jvmTestClasses(Iterable<String> jvmTestClasses) {
            this.jvmTestClasses.addAll(jvmTestClasses);
            return this;
        }

        /**
         * Adds the test methods to be executed.
         *
         * @param jvmTestClass the class on which the methods are defined
         * @param jvmTestMethods the methods to be executed
         * @return this builder
         */
        public Builder jvmTestMethods(String jvmTestClass, String... jvmTestMethods) {
            this.jvmTestMethods.put(jvmTestClass, ImmutableList.copyOf(jvmTestMethods));
            return this;
        }

        /**
         * Adds the test methods to be executed.
         *
         * @param jvmTestClass the class on which the methods are defined
         * @param jvmTestMethods the methods to be executed
         * @return this builder
         */
        public Builder jvmTestMethods(String jvmTestClass, Iterable<String> jvmTestMethods) {
            this.jvmTestMethods.put(jvmTestClass, ImmutableList.copyOf(jvmTestMethods));
            return this;
        }

        /**
         * Adds the tests to be executed.
         *
         * @param tests the tests to be executed
         * @return this builder
         */
        public Builder tests(TestOperationDescriptor... tests) {
            this.tests.addAll(Arrays.asList(tests));
            return this;
        }

        /**
         * Adds the tests to be executed.
         *
         * @param tests the tests to be executed
         * @return this builder
         */
        public Builder tests(Iterable<? extends TestOperationDescriptor> tests) {
            this.tests.addAll(tests);
            return this;
        }

        /**
         * Builds a new {@code TestConfig} instance.
         *
         * @return a new {@code TestConfig} instance
         */
        public TestConfig build() {
            return new TestConfig(this.jvmTestClasses.build(), this.jvmTestMethods.build(), this.tests.build());
        }

    }

}
