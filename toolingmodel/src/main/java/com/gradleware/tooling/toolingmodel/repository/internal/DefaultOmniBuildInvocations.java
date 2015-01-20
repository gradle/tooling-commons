package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingmodel.BuildInvocationFields;
import com.gradleware.tooling.toolingmodel.OmniBuildInvocations;
import com.gradleware.tooling.toolingmodel.OmniProjectTask;
import com.gradleware.tooling.toolingmodel.OmniTaskSelector;
import com.gradleware.tooling.toolingmodel.ProjectTaskFields;
import com.gradleware.tooling.toolingmodel.TaskSelectorsFields;
import com.gradleware.tooling.toolingmodel.generic.DefaultModel;
import com.gradleware.tooling.toolingmodel.generic.Model;
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

    public static DefaultOmniBuildInvocations from(Model<BuildInvocationFields> model) {
        ImmutableList<OmniProjectTask> projectTasks = toProjectTasks(model.get(BuildInvocationFields.PROJECT_TASKS));
        ImmutableList<OmniTaskSelector> taskSelectors = toSelectorTasks(model.get(BuildInvocationFields.TASK_SELECTORS));
        return new DefaultOmniBuildInvocations(projectTasks, taskSelectors);
    }

    private static ImmutableList<OmniProjectTask> toProjectTasks(List<Model<ProjectTaskFields>> projectTasks) {
        return FluentIterable.from(projectTasks).transform(new Function<Model<ProjectTaskFields>, OmniProjectTask>() {
            @Override
            public DefaultOmniProjectTask apply(Model<ProjectTaskFields> input) {
                return DefaultOmniProjectTask.from(input);
            }
        }).toList();
    }

    private static ImmutableList<OmniTaskSelector> toSelectorTasks(List<Model<TaskSelectorsFields>> taskSelectors) {
        return FluentIterable.from(taskSelectors).transform(new Function<Model<TaskSelectorsFields>, OmniTaskSelector>() {
            @Override
            public DefaultOmniTaskSelector apply(Model<TaskSelectorsFields> input) {
                return DefaultOmniTaskSelector.from(input);
            }
        }).toList();
    }

    public static DefaultOmniBuildInvocations from(List<OmniProjectTask> projectTasks, List<OmniTaskSelector> taskSelectors) {
        return new DefaultOmniBuildInvocations(projectTasks, taskSelectors);
    }

    public static DefaultModel<BuildInvocationFields> from(BuildInvocations buildInvocations, String projectPath) {
        DefaultModel<BuildInvocationFields> model = new DefaultModel<BuildInvocationFields>();
        model.put(BuildInvocationFields.PROJECT_TASKS, createProjectTasksModel(buildInvocations.getTasks()));
        model.put(BuildInvocationFields.TASK_SELECTORS, createTaskSelectorsModel(buildInvocations.getTaskSelectors(), projectPath));
        return model;
    }

    private static ImmutableList<Model<ProjectTaskFields>> createProjectTasksModel(DomainObjectSet<? extends Task> projectTasks) {
        return FluentIterable.from(projectTasks).transform(new Function<Task, Model<ProjectTaskFields>>() {
            @Override
            public Model<ProjectTaskFields> apply(Task input) {
                return DefaultOmniProjectTask.from(input, false);
            }
        }).toList();
    }

    private static ImmutableList<Model<TaskSelectorsFields>> createTaskSelectorsModel(DomainObjectSet<? extends TaskSelector> taskSelectors, final String projectPath) {
        return FluentIterable.from(taskSelectors).transform(new Function<TaskSelector, Model<TaskSelectorsFields>>() {
            @Override
            public Model<TaskSelectorsFields> apply(TaskSelector input) {
                return DefaultOmniTaskSelector.from(input, projectPath);
            }
        }).toList();
    }

    public static DefaultModel<BuildInvocationFields> from(List<Model<ProjectTaskFields>> projectTasks, ImmutableList<Model<TaskSelectorsFields>> selectorTasks) {
        DefaultModel<BuildInvocationFields> buildInvocations = new DefaultModel<BuildInvocationFields>();
        buildInvocations.put(BuildInvocationFields.PROJECT_TASKS, projectTasks);
        buildInvocations.put(BuildInvocationFields.TASK_SELECTORS, selectorTasks);
        return buildInvocations;
    }

}
