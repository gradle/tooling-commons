package com.gradleware.tooling.toolingclient.internal;

import com.gradleware.tooling.toolingclient.BuildLaunchRequest;
import com.gradleware.tooling.toolingclient.LaunchableConfig;

/**
 * Internal interface that describes the configurable attributes of the build launch request.
 */
interface InspectableBuildLaunchRequest extends InspectableRequest<Void>, BuildLaunchRequest {

    /**
     * @return never null, DefaultToolingClient requires a launch configuration to execute the request
     * @see DefaultToolingClient#mapToBuildLauncher(InspectableBuildLaunchRequest, org.gradle.tooling.ProjectConnection)
     */
    LaunchableConfig getLaunchables();

}
