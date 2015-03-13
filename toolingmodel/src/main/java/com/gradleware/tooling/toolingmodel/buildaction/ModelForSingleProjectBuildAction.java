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

package com.gradleware.tooling.toolingmodel.buildaction;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildController;
import org.gradle.tooling.model.gradle.BasicGradleProject;
import org.gradle.tooling.model.gradle.GradleBuild;

/**
 * Build action to get a specific model for a given project.
 *
 * @param <T> the type of model to fetch
 */
public final class ModelForSingleProjectBuildAction<T> implements BuildAction<T> {

    private static final long serialVersionUID = 1L;

    private final String projectPath;
    private final Class<T> modelType;

    ModelForSingleProjectBuildAction(String projectPath, Class<T> modelType) {
        this.projectPath = projectPath;
        this.modelType = modelType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T execute(BuildController controller) {
        BasicGradleProject project = findProject(controller.getBuildModel());
        return controller.getModel(project, this.modelType);
    }

    private BasicGradleProject findProject(GradleBuild build) {
        for (BasicGradleProject project : build.getProjects()) {
            if (project.getPath().equals(this.projectPath)) {
                return project;
            }
        }

        throw new IllegalStateException("Invalid project path: " + this.projectPath);
    }

}
