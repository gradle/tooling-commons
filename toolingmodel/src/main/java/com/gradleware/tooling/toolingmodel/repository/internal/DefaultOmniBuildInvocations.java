package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingmodel.OmniBuildInvocations;
import com.gradleware.tooling.toolingmodel.OmniProjectTask;
import com.gradleware.tooling.toolingmodel.OmniTaskSelector;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.Task;
import org.gradle.tooling.model.TaskSelector;
import org.gradle.tooling.model.gradle.BuildInvocations;

import java.util.List;

/**
 * Default implementation of the {@link OmniBuildInvocations} interface.
 */
public final class DefaultOmniBuildInvocations implements OmniBuildInvocations {

    private final ImmutableList<OmniProjectTask> projectTasks;
    private final ImmutableList<OmniTaskSelector> taskSelectors;

    private DefaultOmniBuildInvocations(List<OmniProjectTask> projectTasks, List<OmniTaskSelector> taskSelectors) {
        this.projectTasks = ImmutableList.copyOf(projectTasks);
        this.taskSelectors = ImmutableList.copyOf(taskSelectors);
    }

    @Override
    public ImmutableList<OmniProjectTask> getProjectTasks() {
        return this.projectTasks;
    }

    @Override
    public ImmutableList<OmniTaskSelector> getTaskSelectors() {
        return this.taskSelectors;
    }

    public static DefaultOmniBuildInvocations from(BuildInvocations buildInvocations, String projectPath) {
        return new DefaultOmniBuildInvocations(
                createProjectTasks(buildInvocations.getTasks()),
                createTaskSelectors(buildInvocations.getTaskSelectors(), projectPath));
    }

    private static ImmutableList<OmniProjectTask> createProjectTasks(DomainObjectSet<? extends Task> projectTasks) {
        return FluentIterable.from(projectTasks).transform(new Function<Task, OmniProjectTask>() {
            @Override
            public OmniProjectTask apply(Task input) {
                return DefaultOmniProjectTask.from(input, false);
            }
        }).toList();
    }

    private static ImmutableList<OmniTaskSelector> createTaskSelectors(DomainObjectSet<? extends TaskSelector> taskSelectors, final String projectPath) {
        return FluentIterable.from(taskSelectors).transform(new Function<TaskSelector, OmniTaskSelector>() {
            @Override
            public OmniTaskSelector apply(TaskSelector input) {
                return DefaultOmniTaskSelector.from(input, projectPath);
            }
        }).toList();
    }

    public static DefaultOmniBuildInvocations from(List<OmniProjectTask> projectTasks, List<OmniTaskSelector> taskSelectors) {
        return new DefaultOmniBuildInvocations(projectTasks, taskSelectors);
    }

}
