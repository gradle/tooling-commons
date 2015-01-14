package com.gradleware.tooling.toolingclient.internal;

import com.google.common.base.Preconditions;
import com.gradleware.tooling.toolingclient.BuildActionRequest;
import com.gradleware.tooling.toolingclient.LongRunningOperationPromise;
import org.gradle.tooling.BuildAction;

/**
 * Internal implementation of the {@link BuildActionRequest} API.
 */
final class DefaultBuildActionRequest<T> extends BaseRequest<T, DefaultBuildActionRequest<T>> implements InspectableBuildActionRequest<T> {

    private final BuildAction<T> buildAction;

    DefaultBuildActionRequest(ExecutableToolingClient toolingClient, BuildAction<T> buildAction) {
        super(toolingClient);
        this.buildAction = Preconditions.checkNotNull(buildAction);
    }

    @Override
    public BuildAction<T> getBuildAction() {
        return this.buildAction;
    }

    @Override
    public <S> DefaultBuildActionRequest<S> deriveForBuildAction(BuildAction<S> buildAction) {
        return copy(new DefaultBuildActionRequest<S>(getToolingClient(), buildAction));
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
    DefaultBuildActionRequest<T> getThis() {
        return this;
    }

}
