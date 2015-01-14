package com.gradleware.tooling.toolingclient.internal;

import com.google.common.base.Preconditions;
import com.gradleware.tooling.toolingclient.BuildLaunchRequest;
import com.gradleware.tooling.toolingclient.LaunchableConfig;
import com.gradleware.tooling.toolingclient.LongRunningOperationPromise;

/**
 * Internal implementation of the {@link BuildLaunchRequest} API.
 */
final class DefaultBuildLaunchRequest extends BaseRequest<Void, DefaultBuildLaunchRequest> implements InspectableBuildLaunchRequest {

    private final LaunchableConfig launchables;

    DefaultBuildLaunchRequest(ExecutableToolingClient toolingClient, LaunchableConfig launchables) {
        super(toolingClient);
        this.launchables = Preconditions.checkNotNull(launchables);
    }

    @Override
    public LaunchableConfig getLaunchables() {
        return this.launchables;
    }

    @Override
    public DefaultBuildLaunchRequest deriveForLaunchables(LaunchableConfig launchables) {
        return copy(new DefaultBuildLaunchRequest(getToolingClient(), launchables));
    }

    @Override
    public Void executeAndWait() {
        return getToolingClient().executeAndWait(this);
    }

    @Override
    public LongRunningOperationPromise<Void> execute() {
        return getToolingClient().execute(this);
    }

    @Override
    DefaultBuildLaunchRequest getThis() {
        return this;
    }

}
