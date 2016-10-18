/*
 * Copyright 2016 the original author or authors.
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
import org.gradle.tooling.model.gradle.GradleBuild;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Build action to get a specific model for all projects of a composite build.
 *
 * @param <T> the type of models to fetch
 * @author Donat Csikos
 */
// TODO (donat) can we merge this class with the other build actions in this package?
public final class RootModelsForCompositeProjectBuildAction<T> implements BuildAction<Collection<T>> {

    private static final long serialVersionUID = 1L;

    private final Class<T> modelType;

    RootModelsForCompositeProjectBuildAction(Class<T> modelType) {
        this.modelType = modelType;
    }

    @Override
    public Collection<T> execute(BuildController controller) {
        Collection<T> models = new ArrayList<T>();
        collectRootModels(controller, controller.getBuildModel(), models);
        return models;
    }

    private void collectRootModels(BuildController controller, GradleBuild build, Collection<T> models) {
        models.add(controller.getModel(build.getRootProject(), this.modelType));

        for (GradleBuild includedBuild : build.getIncludedBuilds()) {
            collectRootModels(controller, includedBuild, models);
        }
    }
}
