package com.gradleware.tooling.toolingclient.internal;

import org.gradle.api.Action;

import com.gradleware.tooling.toolingclient.CompositeModelRequest;
import com.gradleware.tooling.toolingclient.ConnectionDescriptor;
import com.gradleware.tooling.toolingclient.LongRunningOperationPromise;

public class DefaultCompositeModelRequest<T> implements InspectableCompositeModelRequest<T> {

    private ExecutableToolingClient toolingClient;

    public DefaultCompositeModelRequest(ExecutableToolingClient toolingClient, Class<T> modelType) {
        this.toolingClient = toolingClient;
    }

   @Override
public ConnectionDescriptor addProject() {
    // TODO Auto-generated method stub
    return null;
}

    @Override
    public ConnectionDescriptor[] getProjects() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public T executeAndWait() {
        return this.toolingClient.executeAndWait(this);
    }

    @Override
    public LongRunningOperationPromise<T> execute() {
        return this.toolingClient.execute(this);
    }

}
