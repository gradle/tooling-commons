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

import com.google.common.collect.ImmutableSortedSet;
import com.gradleware.tooling.toolingmodel.OmniTaskSelector;
import com.gradleware.tooling.toolingmodel.Path;
import org.gradle.tooling.model.TaskSelector;

import java.util.SortedSet;

/**
 * Default implementation of the {@link OmniTaskSelector} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniTaskSelector implements OmniTaskSelector {

    private String name;
    private String description;
    private Path projectPath;
    private boolean isPublic;
    private ImmutableSortedSet<Path> selectedTaskPaths;

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Path getProjectPath() {
        return this.projectPath;
    }

    public void setProjectPath(Path projectPath) {
        this.projectPath = projectPath;
    }

    @Override
    public boolean isPublic() {
        return this.isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    @Override
    public ImmutableSortedSet<Path> getSelectedTaskPaths() {
        return this.selectedTaskPaths;
    }

    public void setSelectedTaskPaths(SortedSet<Path> selectedTaskPaths) {
        this.selectedTaskPaths = ImmutableSortedSet.copyOfSorted(selectedTaskPaths);
    }

    public static DefaultOmniTaskSelector from(TaskSelector selector, Path projectPath) {
        DefaultOmniTaskSelector taskSelector = new DefaultOmniTaskSelector();
        taskSelector.setName(selector.getName());
        taskSelector.setDescription(selector.getDescription());
        taskSelector.setProjectPath(projectPath);
        setIsPublic(taskSelector, selector);
        taskSelector.setSelectedTaskPaths(ImmutableSortedSet.<Path>of());
        return taskSelector;
    }

    public static DefaultOmniTaskSelector from(String name, String description, Path projectPath, boolean isPublic, SortedSet<Path> selectedTaskPaths) {
        DefaultOmniTaskSelector taskSelector = new DefaultOmniTaskSelector();
        taskSelector.setName(name);
        taskSelector.setDescription(description);
        taskSelector.setProjectPath(projectPath);
        taskSelector.setPublic(isPublic);
        taskSelector.setSelectedTaskPaths(selectedTaskPaths);
        return taskSelector;
    }

    /**
     * TaskSelector#isPublic is only available in Gradle versions >= 2.1.
     *
     * @param gradleTaskSelector the task selector to populate
     * @param taskSelector the task selector model
     */
    private static void setIsPublic(DefaultOmniTaskSelector gradleTaskSelector, TaskSelector taskSelector) {
        try {
            boolean isPublic = taskSelector.isPublic();
            gradleTaskSelector.setPublic(isPublic);
        } catch (Exception ignore) {
            gradleTaskSelector.setPublic(true);
        }
    }

}
