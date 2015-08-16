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

import java.util.Arrays;
import java.util.List;

import org.gradle.tooling.TestLauncher;
import org.gradle.tooling.events.test.TestOperationDescriptor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

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

        checkNoMoreThanOneListNotEmpty(jvmTestClasses, tests);
    }

    // todo (etst) is this check really needed/correct?
    private void checkNoMoreThanOneListNotEmpty(List<String> jvmTestClasses, List<TestOperationDescriptor> tests) {
        Preconditions.checkArgument(jvmTestClasses.isEmpty() || tests.isEmpty(), "Both test classes and test descriptors specified.");
    }

    /**
     * Configures the specified test launcher with this test config.
     *
     * @param testLauncher the test launcher to configure
     */
    public void apply(TestLauncher testLauncher) {
        Preconditions.checkNotNull(testLauncher);
        if (!this.jvmTestClasses.isEmpty()) {
            testLauncher.withJvmTestClasses(this.jvmTestClasses);
        } else if (!this.tests.isEmpty()) {
            testLauncher.withTests(this.tests);
        }
    }

    /**
     * Specifies the tests to be executed.
     *
     * @param jvmTestClasses the names of the test classes to be executed
     * @return a new instance
     */
    public static TestConfig forJvmTestClasses(String... jvmTestClasses) {
        return forJvmTestClasses(Arrays.asList(jvmTestClasses));
    }

    /**
     * Specifies the tests to be executed.
     *
     * @param jvmTestClasses the names of the test classes to be executed
     * @return a new instance
     */
    public static TestConfig forJvmTestClasses(Iterable<String> jvmTestClasses) {
        return new TestConfig(ImmutableList.copyOf(jvmTestClasses), ImmutableList.<TestOperationDescriptor>of());
    }

    /**
     * Specifies the tests to be executed.
     *
     * @param tests the tests to be executed
     * @return a new instance
     */
    public static TestConfig forTests(TestOperationDescriptor... tests) {
        return forTests(Arrays.asList(tests));
    }

    /**
     * Specifies the tests to be executed.
     *
     * @param tests the tests to be executed
     * @return a new instance
     */
    public static TestConfig forTests(Iterable<? extends TestOperationDescriptor> tests) {
        return new TestConfig(ImmutableList.<String>of(), ImmutableList.copyOf(tests));
    }

}
