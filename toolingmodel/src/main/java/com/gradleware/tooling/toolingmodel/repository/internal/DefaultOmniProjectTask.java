package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.OmniProjectTask;
import com.gradleware.tooling.toolingmodel.Path;
import org.gradle.tooling.model.Task;

/**
 * Default implementation of the {@link OmniProjectTask} interface.
 */
public final class DefaultOmniProjectTask implements OmniProjectTask {

    private String name;
    private String description;
    private Path path;
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
    public Path getPath() {
        return this.path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    public boolean isPublic() {
        return this.isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public static DefaultOmniProjectTask from(Task task, boolean enforceAllTasksPublic) {
        DefaultOmniProjectTask projectTask = new DefaultOmniProjectTask();
        projectTask.setName(task.getName());
        projectTask.setDescription(task.getDescription());
        projectTask.setPath(Path.from(task.getPath()));
        setIsPublic(projectTask, task, enforceAllTasksPublic);
        return projectTask;
    }

    /**
     * GradleTask#isPublic is only available in Gradle versions >= 2.1.
     *
     * For versions 2.1 and 2.2.x, GradleTask#isPublic always returns {@code false} and needs to be corrected to {@code true}.
     *
     * @param projectTask the task to populate
     * @param task the task model
     * @param enforceAllTasksPublic flag to signal whether all tasks should be treated as public regardless of what the model says
     */
    private static void setIsPublic(DefaultOmniProjectTask projectTask, Task task, boolean enforceAllTasksPublic) {
        try {
            boolean isPublic = task.isPublic();
            projectTask.setPublic(enforceAllTasksPublic || isPublic);
        } catch (Exception ignore) {
            projectTask.setPublic(true);
        }
    }

}
