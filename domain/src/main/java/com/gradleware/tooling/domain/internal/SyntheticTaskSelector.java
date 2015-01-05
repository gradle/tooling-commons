package com.gradleware.tooling.domain.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;
import org.gradle.tooling.internal.consumer.converters.TaskNameComparator;
import org.gradle.tooling.internal.gradle.TaskListingLaunchable;
import org.gradle.tooling.model.TaskSelector;

import java.util.Set;

/**
 * Implementation of the {@link org.gradle.tooling.model.TaskSelector} interface for those Gradle versions that do not provide an implementation.
 */
public final class SyntheticTaskSelector implements TaskSelector, TaskListingLaunchable {

    private final String name;
    private final String displayName;
    private final String description;
    private final boolean isPublic;
    private final ImmutableSortedSet<String> taskNames;

    private SyntheticTaskSelector(String name, String displayName, String description, boolean isPublic, Set<String> taskNames) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.isPublic = isPublic;
        this.taskNames = ImmutableSortedSet.orderedBy(new TaskNameComparator()).addAll(taskNames).build();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isPublic() {
        return isPublic;
    }

    @Override
    public ImmutableSortedSet<String> getTaskNames() {
        return taskNames;
    }

    public static SyntheticTaskSelector from(String name, String displayName, String description, boolean isPublic, Set<String> taskNames) {
        Preconditions.checkNotNull(name);

        return new SyntheticTaskSelector(name, displayName, description, isPublic, taskNames);
    }

}
