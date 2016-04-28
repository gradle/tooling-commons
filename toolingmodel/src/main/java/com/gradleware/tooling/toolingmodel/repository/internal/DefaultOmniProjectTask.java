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

import org.gradle.tooling.model.Task;

import com.gradleware.tooling.toolingmodel.OmniProjectTask;
import com.gradleware.tooling.toolingmodel.Path;
import com.gradleware.tooling.toolingmodel.util.Maybe;

/**
 * Default implementation of the {@link OmniProjectTask} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniProjectTask implements OmniProjectTask {

    private String name;
    private String description;
    private Path path;
    private boolean isPublic;
    private Maybe<String> group;

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

    @Override
    public Maybe<String> getGroup() {
        return this.group;
    }

    public void setGroup(Maybe<String> group) {
        this.group = group;
    }

    public static DefaultOmniProjectTask from(Task task) {
        DefaultOmniProjectTask projectTask = new DefaultOmniProjectTask();
        projectTask.setName(task.getName());
        projectTask.setDescription(task.getDescription());
        projectTask.setPath(Path.from(task.getPath()));
        setIsPublic(projectTask, task);
        setGroup(projectTask, task);
        return projectTask;
    }

    /**
     * GradleTask#isPublic is only available in Gradle versions >= 2.1.
     * <p/>
     *
     * @param projectTask the task to populate
     * @param task the task model
     */
    private static void setIsPublic(DefaultOmniProjectTask projectTask, Task task) {
        try {
            boolean isPublic = task.isPublic();
            projectTask.setPublic(isPublic);
        } catch (Exception ignore) {
            projectTask.setPublic(true);
        }
    }

   /**
     * GradleTask#getGroup is only available in Gradle versions >= 2.5.
     *
     * @param projectTask the task to populate
     * @param task the task model
     */
    private static void setGroup(DefaultOmniProjectTask projectTask, Task task) {
        try {
            String group = task.getGroup();
            projectTask.setGroup(Maybe.of(group));
        } catch (Exception ignore) {
            projectTask.setGroup(Maybe.<String>absent());
        }
    }

}
