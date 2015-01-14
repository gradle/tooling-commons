package com.gradleware.tooling.toolingclient.internal;

import com.gradleware.tooling.toolingclient.LongRunningOperationPromise;

/**
 * Internal interface that describes the request execution part of the tooling client.
 */
interface ExecutableToolingClient {

    <T> T executeAndWait(InspectableModelRequest<T> modelRequest);

    <T> LongRunningOperationPromise<T> execute(InspectableModelRequest<T> modelRequest);

    <T> T executeAndWait(InspectableBuildActionRequest<T> buildActionRequest);

    <T> LongRunningOperationPromise<T> execute(InspectableBuildActionRequest<T> buildActionRequest);

    Void executeAndWait(InspectableBuildLaunchRequest buildLaunchRequest);

    LongRunningOperationPromise<Void> execute(InspectableBuildLaunchRequest buildLaunchRequest);

}
