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

package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingmodel.OmniBuildInvocations;
import com.gradleware.tooling.toolingmodel.OmniProjectTask;
import com.gradleware.tooling.toolingmodel.OmniTaskSelector;
import com.gradleware.tooling.toolingmodel.Path;
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

    public static DefaultOmniBuildInvocations from(BuildInvocations buildInvocations, Path projectPath) {
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

    private static ImmutableList<OmniTaskSelector> createTaskSelectors(DomainObjectSet<? extends TaskSelector> taskSelectors, final Path projectPath) {
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
