package com.gradleware.tooling.toolingmodel.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;
import org.gradle.tooling.internal.consumer.converters.TaskNameComparator;
import org.gradle.tooling.internal.gradle.TaskListingLaunchable;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;

/**
 * Implementation of the {@link org.gradle.tooling.model.GradleTask} interface for those Gradle versions that do not provide an implementation.
 */
@SuppressWarnings("deprecation")
public final class SyntheticGradleTask implements GradleTask, TaskListingLaunchable {

    private final GradleTask gradleTask;
    private final ImmutableSortedSet<String> taskNames;

    private SyntheticGradleTask(GradleTask gradleTask) {
        this.gradleTask = gradleTask;
        this.taskNames = ImmutableSortedSet.orderedBy(new TaskNameComparator()).add(gradleTask.getPath()).build();
    }

    @Override
    public String getName() {
        return this.gradleTask.getName();
    }

    @Override
    public String getDisplayName() {
        return this.gradleTask.getDisplayName();
    }

    @Override
    public String getDescription() {
        return this.gradleTask.getDescription();
    }

    @Override
    public boolean isPublic() {
        return this.gradleTask.isPublic();
    }

    @Override
    public String getPath() {
        return this.gradleTask.getPath();
    }

    @Override
    public GradleProject getProject() {
        return this.gradleTask.getProject();
    }

    @Override
    public ImmutableSortedSet<String> getTaskNames() {
        return this.taskNames;
    }

    public static SyntheticGradleTask from(GradleTask gradleTask) {
        Preconditions.checkNotNull(gradleTask);

        return new SyntheticGradleTask(gradleTask);
    }

}
