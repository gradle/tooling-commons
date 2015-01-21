package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.collect.ImmutableSortedSet;
import com.gradleware.tooling.toolingmodel.OmniTaskSelector;
import org.gradle.tooling.model.TaskSelector;

import java.util.SortedSet;

/**
 * Default implementation of the {@link OmniTaskSelector} interface.
 */
public final class DefaultOmniTaskSelector implements OmniTaskSelector {

    private String name;
    private String description;
    private String projectPath;
    private boolean isPublic;
    private ImmutableSortedSet<String> selectedTaskPaths;

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
    public String getProjectPath() {
        return this.projectPath;
    }

    public void setProjectPath(String projectPath) {
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
    public ImmutableSortedSet<String> getSelectedTaskPaths() {
        return this.selectedTaskPaths;
    }

    public void setSelectedTaskPaths(SortedSet<String> selectedTaskPaths) {
        this.selectedTaskPaths = ImmutableSortedSet.copyOfSorted(selectedTaskPaths);
    }

    public static DefaultOmniTaskSelector from(TaskSelector selector, String projectPath) {
        DefaultOmniTaskSelector taskSelector = new DefaultOmniTaskSelector();
        taskSelector.setName(selector.getName());
        taskSelector.setDescription(selector.getDescription());
        taskSelector.setProjectPath(projectPath);
        setIsPublic(taskSelector, selector);
        taskSelector.setSelectedTaskPaths(ImmutableSortedSet.<String>of());
        return taskSelector;
    }

    public static DefaultOmniTaskSelector from(String name, String description, String projectPath, boolean isPublic, SortedSet<String> selectedTaskPaths) {
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
