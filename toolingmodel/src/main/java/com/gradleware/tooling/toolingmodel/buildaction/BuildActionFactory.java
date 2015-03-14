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

import com.google.common.base.Preconditions;
import org.gradle.tooling.BuildAction;

/**
 * Factory for {@link org.gradle.tooling.BuildAction} instances.
 *
 * @author Etienne Studer
 */
public final class BuildActionFactory {

    /**
     * Creates a new {@code ModelForSingleProjectBuildAction} that fetches the given model for the given project path.
     *
     * @param projectPath the path of the project for which to fetch the model
     * @param modelType the model to fetch
     * @param <T> the model type
     * @return the build action
     */
    public static <T> ModelForSingleProjectBuildAction<T> getModelForProject(String projectPath, Class<T> modelType) {
        Preconditions.checkNotNull(projectPath);
        Preconditions.checkNotNull(modelType);
        return new ModelForSingleProjectBuildAction<T>(projectPath, modelType);
    }

    /**
     * Creates a new {@code ModelForAllProjectsBuildAction} that fetches the given model for all projects of the build.
     *
     * @param modelType the model to fetch
     * @param <T> the model type
     * @return the build action
     */
    public static <T> ModelForAllProjectsBuildAction<T> getModelForAllProjects(Class<T> modelType) {
        Preconditions.checkNotNull(modelType);
        return new ModelForAllProjectsBuildAction<T>(modelType);
    }

    /**
     * Creates a new {@code GlobalModelBuildAction} that fetches the given global build model.
     *
     * @param modelType the model to fetch
     * @param <T> the model type
     * @return the build action
     */
    public static <T> GlobalModelBuildAction<T> getBuildModel(Class<T> modelType) {
        Preconditions.checkNotNull(modelType);
        return new GlobalModelBuildAction<T>(modelType);
    }

    /**
     * Creates a new {@code DoubleBuildAction} that executes two actions in a single invocation.
     *
     * @param first the first action to execute
     * @param second the second action to execute
     * @param <S> the result type of the first action
     * @param <T> the result type of the second action
     * @return the build action
     */
    public static <S, T> DoubleBuildAction<S, T> getPairResult(BuildAction<S> first, BuildAction<T> second) {
        Preconditions.checkNotNull(first);
        Preconditions.checkNotNull(second);
        return new DoubleBuildAction<S, T>(first, second);
    }

    /**
     * Creates a new {@code TripleBuildAction} that executes three actions in a single invocation.
     *
     * @param first the first action to execute
     * @param second the second action to execute
     * @param third the third action to execute
     * @param <S> the result type of the first action
     * @param <T> the result type of the second action
     * @param <U> the result type of the third action
     * @return the build action
     */
    public static <S, T, U> TripleBuildAction<S, T, U> getTripleResult(BuildAction<S> first, BuildAction<T> second, BuildAction<U> third) {
        Preconditions.checkNotNull(first);
        Preconditions.checkNotNull(second);
        Preconditions.checkNotNull(third);
        return new TripleBuildAction<S, T, U>(first, second, third);
    }

}
