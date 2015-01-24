package com.gradleware.tooling.toolingmodel.buildaction;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildController;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.gradle.BasicGradleProject;

import java.util.HashMap;
import java.util.Map;

/**
 * Build action to get a specific model for all projects of a build.
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
