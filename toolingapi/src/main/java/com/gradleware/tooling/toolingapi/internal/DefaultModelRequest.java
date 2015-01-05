package com.gradleware.tooling.toolingapi.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingapi.LongRunningOperationPromise;
import com.gradleware.tooling.toolingapi.ModelRequest;
import org.gradle.tooling.model.gradle.BuildInvocations;
import org.gradle.tooling.model.gradle.ProjectPublications;

/**
 * Internal implementation of the {@link ModelRequest} API.
 */
final class DefaultModelRequest<T> extends BaseRequest<T, DefaultModelRequest<T>> implements ModelRequest<T>, InspectableModelRequest<T> {

    // all models provided by an instance of ProjectSensitiveToolingModelBuilder are listed here
    private static final Class<?>[] UNSUPPORTED_MODEL_TYPES = new Class<?>[]{BuildInvocations.class, ProjectPublications.class};

    private final Class<T> modelType;
    private ImmutableList<String> tasks;

    DefaultModelRequest(ExecutableToolingClient toolingClient, Class<T> modelType) {
        super(toolingClient);
        this.modelType = Preconditions.checkNotNull(modelType);
        this.tasks = ImmutableList.of();

        ensureModelIsBuildScoped(modelType);
    }

    private void ensureModelIsBuildScoped(Class<T> modelType) {
        for (Class<?> unsupportedModelType : UNSUPPORTED_MODEL_TYPES) {
            if (unsupportedModelType == modelType) {
                throw new IllegalArgumentException(String.format("%s model does not support the Build scope", modelType.getSimpleName()));
            }
        }
    }

    @Override
    public Class<T> getModelType() {
        return modelType;
    }

    @Override
    public DefaultModelRequest<T> tasks(String... tasks) {
        this.tasks = ImmutableList.copyOf(tasks);
        return getThis();
    }

    @Override
    public String[] getTasks() {
        return tasks.toArray(new String[tasks.size()]);
    }

    @Override
    public <S> ModelRequest<S> deriveForModel(Class<S> modelType) {
        return copy(new DefaultModelRequest<S>(getToolingClient(), modelType)).tasks(getTasks());
    }

    @Override
    public T executeAndWait() {
        return getToolingClient().executeAndWait(this);
    }

    @Override
    public LongRunningOperationPromise<T> execute() {
        return getToolingClient().execute(this);
    }

    @Override
    DefaultModelRequest<T> getThis() {
        return this;
    }

}
