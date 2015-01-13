package com.gradleware.tooling.domain.model.internal;

import com.google.common.collect.ImmutableSortedSet;
import com.gradleware.tooling.domain.model.OmniTaskSelector;
import com.gradleware.tooling.domain.model.TaskSelectorsFields;
import com.gradleware.tooling.domain.generic.Model;

import java.util.SortedSet;

/**
 * Default implementation of the {@link OmniTaskSelector} interface.
 */
public final class DefaultOmniTaskSelector implements OmniTaskSelector {

    private String name;
    private String description;
    private boolean isPublic;
    private SortedSet<String> selectedTaskPaths;

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
    public boolean isPublic() {
        return this.isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    @Override
    public SortedSet<String> getSelectedTaskPaths() {
        return this.selectedTaskPaths;
    }

    public void setSelectedTaskPaths(SortedSet<String> selectedTaskPaths) {
        this.selectedTaskPaths = ImmutableSortedSet.copyOf(selectedTaskPaths);
    }

    public static DefaultOmniTaskSelector from(Model<TaskSelectorsFields> task) {
        DefaultOmniTaskSelector taskSelector = new DefaultOmniTaskSelector();
        taskSelector.setName(task.get(TaskSelectorsFields.NAME));
        taskSelector.setDescription(task.get(TaskSelectorsFields.DESCRIPTION));
        taskSelector.setPublic(task.get(TaskSelectorsFields.IS_PUBLIC));
        taskSelector.setSelectedTaskPaths(task.get(TaskSelectorsFields.SELECTED_TASK_PATHS));
        return taskSelector;
    }

}
