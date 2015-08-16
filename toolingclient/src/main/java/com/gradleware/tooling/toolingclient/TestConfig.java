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
import org.gradle.tooling.TestLauncher;
import org.gradle.tooling.events.test.TestOperationDescriptor;

import java.util.Arrays;
import java.util.List;

/**
 * Encapsulates the {@link TestOperationDescriptor} instances to execute through a Gradle build.
 *
 * @author Donát Csikós
 */
public final class TestConfig {

    private final ImmutableList<String> jvmTestClasses;
    private final ImmutableList<TestOperationDescriptor> tests;

    private TestConfig(List<String> jvmTestClasses, List<TestOperationDescriptor> tests) {
        this.jvmTestClasses = ImmutableList.copyOf(jvmTestClasses);
        this.tests = ImmutableList.copyOf(tests);

        checkNotAllListsEmpty(jvmTestClasses, tests);
    }

    private void checkNotAllListsEmpty(List<String> jvmTestClasses, List<TestOperationDescriptor> tests) {
        Preconditions.checkArgument(!jvmTestClasses.isEmpty() || !tests.isEmpty(), "Either JVM test classes or test operations, or both, must be specified.");
    }

    /**
     * Configures the specified test launcher with this test config.
     *
     * @param testLauncher the test launcher to configure
     */
    public void apply(TestLauncher testLauncher) {
        Preconditions.checkNotNull(testLauncher);
        testLauncher.withJvmTestClasses(this.jvmTestClasses);
        testLauncher.withTests(this.tests);
    }

    /**
     * Specifies the tests to be executed.
     *
     * @param jvmTestClasses the names of the test classes to be executed
     * @return a new instance
     */
    public static TestConfig forJvmTestClasses(String... jvmTestClasses) {
        return new TestConfig.Builder().jvmTestClasses(jvmTestClasses).build();
    }

    public static final class Builder {

        private ImmutableList.Builder<String> jvmTestClasses;
        private ImmutableList.Builder<TestOperationDescriptor> tests;

        public Builder() {
            this.jvmTestClasses = ImmutableList.builder();
            this.tests = ImmutableList.builder();
        }

        /**
         * Specifies the test classes to be executed.
         *
         * @param jvmTestClasses the names of the test classes to be executed
         * @return a new instance
         */
        public Builder jvmTestClasses(String... jvmTestClasses) {
            this.jvmTestClasses.addAll(Arrays.asList(jvmTestClasses));
            return this;
        }

        /**
         * Specifies the test classes to be executed.
         *
         * @param jvmTestClasses the names of the test classes to be executed
         * @return a new instance
         */
        public Builder jvmTestClasses(Iterable<String> jvmTestClasses) {
            this.jvmTestClasses.addAll(jvmTestClasses);
            return this;
        }

        /**
         * Specifies the tests to be executed.
         *
         * @param tests the tests to be executed
         * @return a new instance
         */
        public Builder tests(TestOperationDescriptor... tests) {
            this.tests.addAll(Arrays.asList(tests));
            return this;
        }

        /**
         * Specifies the tests to be executed.
         *
         * @param tests the tests to be executed
         * @return a new instance
         */
        public Builder tests(Iterable<? extends TestOperationDescriptor> tests) {
            this.tests.addAll(tests);
            return this;
        }

        public TestConfig build() {
            return new TestConfig(this.jvmTestClasses.build(), this.tests.build());
        }

    }

}
