package com.gradleware.tooling.toolingapi.internal;

import com.gradleware.tooling.toolingapi.BuildLaunchRequest;
import com.gradleware.tooling.toolingapi.LaunchableConfig;

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
