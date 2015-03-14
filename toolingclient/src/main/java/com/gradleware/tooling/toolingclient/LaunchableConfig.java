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
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.model.Launchable;

import java.util.Arrays;
import java.util.List;

/**
 * Encapsulates the {@link Launchable} instances to execute as part of running a Gradle build.
 *
 * @author Etienne Studer
 */
public final class LaunchableConfig {

    private final ImmutableList<String> tasks;
    private final ImmutableList<Launchable> launchables;

    private LaunchableConfig(List<String> tasks, List<Launchable> launchables) {
        this.tasks = ImmutableList.copyOf(tasks);
        this.launchables = ImmutableList.copyOf(launchables);

        checkNoMoreThanOneListNotEmpty(tasks, launchables);
    }

    private void checkNoMoreThanOneListNotEmpty(List<String> tasks, List<Launchable> launchables) {
        Preconditions.checkArgument(tasks.isEmpty() || launchables.isEmpty(), "Both tasks and launchables specified.");
    }

    /**
     * Configures the specified build launcher with this launchable configuration.
     *
     * @param buildLauncher the build launcher to configure
     */
    public void apply(BuildLauncher buildLauncher) {
        Preconditions.checkNotNull(buildLauncher);
        if (!this.tasks.isEmpty()) {
            buildLauncher.forTasks(this.tasks.toArray(new String[this.tasks.size()]));
        } else if (!this.launchables.isEmpty()) {
            buildLauncher.forLaunchables(this.launchables);
        }
    }

    /**
     * Specifies the tasks to be executed.
     *
     * @param tasks the paths of the tasks to be executed, relative paths are evaluated relative to the project for which the build launch request was created
     * @return a new instance
     */
    public static LaunchableConfig forTasks(String... tasks) {
        return forTasks(Arrays.asList(tasks));
    }

    /**
     * Specifies the tasks to be executed.
     *
     * @param tasks the paths of the tasks to be executed, relative paths are evaluated relative to the project for which the build launch request was created
     * @return a new instance
     */
    public static LaunchableConfig forTasks(Iterable<String> tasks) {
        return new LaunchableConfig(ImmutableList.copyOf(tasks), ImmutableList.<Launchable>of());
    }

    /**
     * Specifies the launchables to execute.
     *
     * @param launchables the launchables to execute
     * @return a new instance
     */
    public static LaunchableConfig forLaunchables(Launchable... launchables) {
        return forLaunchables(Arrays.asList(launchables));
    }

    /**
     * Specifies the launchables to execute.
     *
     * @param launchables the launchables to execute
     * @return a new instance
     */
    public static LaunchableConfig forLaunchables(Iterable<? extends Launchable> launchables) {
        return new LaunchableConfig(ImmutableList.<String>of(), ImmutableList.copyOf(launchables));
    }

}
