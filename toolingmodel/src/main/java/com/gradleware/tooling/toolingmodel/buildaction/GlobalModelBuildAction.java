package com.gradleware.tooling.toolingmodel.buildaction;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildController;

/**
 * Build action to get a specific global model of a build.
 */
public final class GlobalModelBuildAction<T> implements BuildAction<T> {

    private static final long serialVersionUID = 1L;

    private final Class<T> modelType;

    GlobalModelBuildAction(Class<T> modelType) {
        this.modelType = modelType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T execute(BuildController controller) {
        return controller.getModel(this.modelType);
    }

}
