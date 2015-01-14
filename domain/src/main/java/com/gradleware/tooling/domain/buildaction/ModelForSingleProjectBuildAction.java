package com.gradleware.tooling.domain.buildaction;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildController;
import org.gradle.tooling.model.gradle.BasicGradleProject;
import org.gradle.tooling.model.gradle.GradleBuild;

/**
 * Build action to get a specific model for a given project.
 *
 * @since 2.3
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
