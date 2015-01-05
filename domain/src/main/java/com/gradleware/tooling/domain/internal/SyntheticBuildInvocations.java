package com.gradleware.tooling.domain.internal;

import com.google.common.base.Preconditions;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleTask;
import org.gradle.tooling.model.Task;
import org.gradle.tooling.model.TaskSelector;
import org.gradle.tooling.model.gradle.BuildInvocations;
import org.gradle.tooling.model.internal.ImmutableDomainObjectSet;

import java.util.SortedSet;

/**
 * Implementation of the {@link BuildInvocations} interface for those Gradle versions that do not provide an implementation.
 */
public final class SyntheticBuildInvocations implements BuildInvocations {

    private final DomainObjectSet<GradleTask> tasks;
    private final DomainObjectSet<TaskSelector> taskSelectors;

    private SyntheticBuildInvocations(SortedSet<GradleTask> tasks, SortedSet<TaskSelector> taskSelectors) {
        this.tasks = ImmutableDomainObjectSet.of(tasks);
        this.taskSelectors = ImmutableDomainObjectSet.of(taskSelectors);
    }

    @Override
    public DomainObjectSet<? extends Task> getTasks() {
        return tasks;
    }

    @Override
    public DomainObjectSet<? extends TaskSelector> getTaskSelectors() {
        return taskSelectors;
    }

    public static BuildInvocations from(SortedSet<GradleTask> tasks, SortedSet<TaskSelector> taskSelectors) {
        Preconditions.checkNotNull(tasks);
        Preconditions.checkNotNull(taskSelectors);

        return new SyntheticBuildInvocations(tasks, taskSelectors);
    }

}
