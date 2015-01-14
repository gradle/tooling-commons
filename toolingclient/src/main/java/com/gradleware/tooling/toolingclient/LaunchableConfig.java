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
 * @since 2.3
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
     * @since 2.3
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
     * @since 2.3
     */
    public static LaunchableConfig forTasks(String... tasks) {
        return forTasks(Arrays.asList(tasks));
    }

    /**
     * Specifies the tasks to be executed.
     *
     * @param tasks the paths of the tasks to be executed, relative paths are evaluated relative to the project for which the build launch request was created
     * @return a new instance
     * @since 2.3
     */
    public static LaunchableConfig forTasks(Iterable<String> tasks) {
        return new LaunchableConfig(ImmutableList.copyOf(tasks), ImmutableList.<Launchable>of());
    }

    /**
     * Specifies the launchables to execute.
     *
     * @param launchables the launchables to execute
     * @return a new instance
     * @since 2.3
     */
    public static LaunchableConfig forLaunchables(Launchable... launchables) {
        return forLaunchables(Arrays.asList(launchables));
    }

    /**
     * Specifies the launchables to execute.
     *
     * @param launchables the launchables to execute
     * @return a new instance
     * @since 2.3
     */
    public static LaunchableConfig forLaunchables(Iterable<? extends Launchable> launchables) {
        return new LaunchableConfig(ImmutableList.<String>of(), ImmutableList.copyOf(launchables));
    }

}
