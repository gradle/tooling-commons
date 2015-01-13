package com.gradleware.tooling.domain.model.internal;

import com.gradleware.tooling.domain.model.OmniProjectTask;
import com.gradleware.tooling.domain.model.ProjectTaskFields;
import com.gradleware.tooling.domain.generic.Model;

/**
 * Default implementation of the {@link OmniProjectTask} interface.
 */
public final class DefaultOmniProjectTask implements OmniProjectTask {

    private String name;
    private String description;
    private String path;
    private boolean isPublic;

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
    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean isPublic() {
        return this.isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public static DefaultOmniProjectTask from(Model<ProjectTaskFields> task) {
        DefaultOmniProjectTask projectTask = new DefaultOmniProjectTask();
        projectTask.setName(task.get(ProjectTaskFields.NAME));
        projectTask.setDescription(task.get(ProjectTaskFields.DESCRIPTION));
        projectTask.setPath(task.get(ProjectTaskFields.PATH));
        projectTask.setPublic(task.get(ProjectTaskFields.IS_PUBLIC));
        return projectTask;
    }

}
