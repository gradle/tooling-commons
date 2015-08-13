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
 * Encapsulates the {@link TestOperationDescriptor} instances to execute as part of running a Gradle
 * test build.
 *
 * @author Donát Csikós
 */
public final class TestOperationConfig {

    private final ImmutableList<String> jvmTestClasses;
    private final ImmutableList<TestOperationDescriptor> tests;

    private TestOperationConfig(List<String> jvmTestClasses, List<TestOperationDescriptor> tests) {
        this.jvmTestClasses = ImmutableList.copyOf(jvmTestClasses);
        this.tests = ImmutableList.copyOf(tests);

        checkNoMoreThanOneListNotEmpty(jvmTestClasses, tests);
    }

    private void checkNoMoreThanOneListNotEmpty(List<String> jvmTestClasses, List<TestOperationDescriptor> tests) {
        Preconditions.checkArgument(jvmTestClasses.isEmpty() || tests.isEmpty(), "Both test classes and test descriptors specified.");
    }

    /**
     * Configures the target test launcher to run the contained tests.
     *
     * @param testLauncher the launcher to be configured
     */
    public void apply(TestLauncher testLauncher) {
        Preconditions.checkNotNull(testLauncher);
        if (!this.jvmTestClasses.isEmpty()) {
            testLauncher.withJvmTestClasses(this.jvmTestClasses.toArray(new String[this.jvmTestClasses.size()]));
        } else if (!this.tests.isEmpty()) {
            testLauncher.withTests(this.tests);
        }
    }

    /**
     * Creates a new {@link TestOperationConfig} instance defining target array of test classes.
     *
     * @param jvmTestClasses the name of the test classes to be executed in the test build
     * @return a new instance
     */
    public static TestOperationConfig forJvmTestClasses(String... jvmTestClasses) {
        return forJvmTestClasses(Arrays.asList(jvmTestClasses));
    }

    /**
     * Creates a new {@link TestOperationConfig} instance defining the target collection of test
     * classes.
     *
     * @param jvmTestClasses the name of the test classes to be executed in the test build
     * @return a new instance
     */
    public static TestOperationConfig forJvmTestClasses(Iterable<String> jvmTestClasses) {
        return new TestOperationConfig(ImmutableList.copyOf(jvmTestClasses), ImmutableList.<TestOperationDescriptor>of());
    }

    /**
     * Creates a new {@link TestOperationConfig} instance defining the target array of tests.
     *
     * @param tests the tests to be executed in the test build
     * @return a new instance
     */
    public static TestOperationConfig forTests(TestOperationDescriptor... tests) {
        return forTests(Arrays.asList(tests));
    }

    /**
     * Creates a new {@link TestOperationConfig} instance defining the target collection of tests.
     *
     * @param tests the tests to be executed in the test build
     * @return a new instance
     */
    public static TestOperationConfig forTests(Iterable<? extends TestOperationDescriptor> tests) {
        return new TestOperationConfig(ImmutableList.<String>of(), ImmutableList.copyOf(tests));
    }

}
