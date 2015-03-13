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
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.gradle.BasicGradleProject;

import java.util.HashMap;
import java.util.Map;

/**
 * Build action to get a specific model for all projects of a build.
 *
 * @param <T> the type of models to fetch
 */
public final class ModelForAllProjectsBuildAction<T> implements BuildAction<Map<String, T>> {

    private static final long serialVersionUID = 1L;

    private final Class<T> modelType;

    ModelForAllProjectsBuildAction(Class<T> modelType) {
        this.modelType = modelType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, T> execute(BuildController controller) {
        Map<String, T> models = new HashMap<String, T>();
        DomainObjectSet<? extends BasicGradleProject> projects = controller.getBuildModel().getProjects();
        for (BasicGradleProject project : projects) {
            T model = controller.getModel(project, this.modelType);
            models.put(project.getPath(), model);
        }
        return models;
    }

}
