package com.gradleware.tooling.toolingclient.internal;

import com.gradleware.tooling.toolingclient.BuildActionRequest;
import org.gradle.tooling.BuildAction;

/**
 * Internal interface that describes the configurable attributes of the build action request.
 */
interface InspectableBuildActionRequest<T> extends InspectableRequest<T>, BuildActionRequest<T> {

    /**
     * @return never null, DefaultToolingClient requires a build action to execute the request
     * @see DefaultToolingClient#mapToBuildActionExecuter(InspectableBuildActionRequest, org.gradle.tooling.ProjectConnection)
     */
    BuildAction<T> getBuildAction();

}
