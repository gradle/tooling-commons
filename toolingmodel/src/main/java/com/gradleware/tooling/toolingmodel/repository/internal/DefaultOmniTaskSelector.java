package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.collect.ImmutableSortedSet;
import com.gradleware.tooling.toolingmodel.OmniTaskSelector;
import com.gradleware.tooling.toolingmodel.TaskSelectorsFields;
import com.gradleware.tooling.toolingmodel.generic.DefaultModel;
import com.gradleware.tooling.toolingmodel.generic.Model;
import com.gradleware.tooling.toolingmodel.generic.ModelField;
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

    public static DefaultOmniTaskSelector from(Model<TaskSelectorsFields> task) {
        DefaultOmniTaskSelector taskSelector = new DefaultOmniTaskSelector();
        taskSelector.setName(task.get(TaskSelectorsFields.NAME));
        taskSelector.setDescription(task.get(TaskSelectorsFields.DESCRIPTION));
        taskSelector.setProjectPath(task.get(TaskSelectorsFields.PROJECT_PATH));
        taskSelector.setPublic(task.get(TaskSelectorsFields.IS_PUBLIC));
        taskSelector.setSelectedTaskPaths(task.get(TaskSelectorsFields.SELECTED_TASK_PATHS));
        return taskSelector;
    }

    public static DefaultModel<TaskSelectorsFields> from(TaskSelector taskSelector, String projectPath) {
        DefaultModel<TaskSelectorsFields> gradleTaskSelector = new DefaultModel<TaskSelectorsFields>();
        gradleTaskSelector.put(TaskSelectorsFields.NAME, taskSelector.getName());
        gradleTaskSelector.put(TaskSelectorsFields.DESCRIPTION, taskSelector.getDescription());
        gradleTaskSelector.put(TaskSelectorsFields.PROJECT_PATH, projectPath);
        setIsPublic(gradleTaskSelector, TaskSelectorsFields.IS_PUBLIC, taskSelector);
        gradleTaskSelector.put(TaskSelectorsFields.SELECTED_TASK_PATHS, ImmutableSortedSet.<String>of());
        return gradleTaskSelector;
    }

    public static DefaultModel<TaskSelectorsFields> from(String name, String description, String projectPath, boolean isPublic, SortedSet<String> selectedTaskPaths) {
        DefaultModel<TaskSelectorsFields> taskSelector = new DefaultModel<TaskSelectorsFields>();
        taskSelector.put(TaskSelectorsFields.NAME, name);
        taskSelector.put(TaskSelectorsFields.DESCRIPTION, description);
        taskSelector.put(TaskSelectorsFields.PROJECT_PATH, projectPath);
        taskSelector.put(TaskSelectorsFields.IS_PUBLIC, isPublic);
        taskSelector.put(TaskSelectorsFields.SELECTED_TASK_PATHS, selectedTaskPaths);
        return taskSelector;
    }

    /**
     * TaskSelector#isPublic is only available in Gradle versions >= 2.1.
     *
     * @param gradleTaskSelector the task selector to populate
     * @param isPublicField the field from which to derive the default isPublic value in case it is not available on the task selector model
     * @param taskSelector the task selector model
     */
    private static void setIsPublic(DefaultModel<TaskSelectorsFields> gradleTaskSelector, ModelField<Boolean, TaskSelectorsFields> isPublicField, TaskSelector taskSelector) {
        try {
            boolean isPublic = taskSelector.isPublic();
            gradleTaskSelector.put(isPublicField, isPublic);
        } catch (Exception ignore) {
            // do not store if field value is not present
        }
    }

}
