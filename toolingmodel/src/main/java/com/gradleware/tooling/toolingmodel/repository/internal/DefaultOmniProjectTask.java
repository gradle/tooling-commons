package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.OmniProjectTask;
import com.gradleware.tooling.toolingmodel.ProjectTaskFields;
import com.gradleware.tooling.toolingmodel.generic.DefaultModel;
import com.gradleware.tooling.toolingmodel.generic.Model;
import com.gradleware.tooling.toolingmodel.generic.ModelField;
import org.gradle.tooling.model.Task;

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

    public static DefaultModel<ProjectTaskFields> from(Task task, boolean enforceAllTasksPublic) {
        DefaultModel<ProjectTaskFields> projectTask = new DefaultModel<ProjectTaskFields>();
        projectTask.put(ProjectTaskFields.NAME, task.getName());
        projectTask.put(ProjectTaskFields.DESCRIPTION, task.getDescription());
        projectTask.put(ProjectTaskFields.PATH, task.getPath());
        setIsPublic(projectTask, ProjectTaskFields.IS_PUBLIC, task, enforceAllTasksPublic);
        return projectTask;
    }

    /**
     * GradleTask#isPublic is only available in Gradle versions >= 2.1.
     *
     * For versions 2.1 and 2.2.x, GradleTask#isPublic always returns {@code false} and needs to be corrected to {@code true}.
     *
     * @param projectTask the task to populate
     * @param isPublicField the field from which to derive the default isPublic value in case it is not available on the task model
     * @param task the task model
     * @param enforceAllTasksPublic flag to signal whether all tasks should be treated as public regardless of what the model says
     */
    private static void setIsPublic(DefaultModel<ProjectTaskFields> projectTask, ModelField<Boolean, ProjectTaskFields> isPublicField, Task task, boolean enforceAllTasksPublic) {
        try {
            boolean isPublic = task.isPublic();
            projectTask.put(isPublicField, enforceAllTasksPublic || isPublic);
        } catch (Exception ignore) {
            // do not store if field value is not present
        }
    }

}
