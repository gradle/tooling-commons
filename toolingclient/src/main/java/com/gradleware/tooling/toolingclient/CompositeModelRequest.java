package com.gradleware.tooling.toolingclient;

import org.gradle.api.Action;

public interface CompositeModelRequest<T> {
    ConnectionDescriptor addProject();

    /**
     * Fetches the requested model synchronously. Calling this method will block until the model has been fetched or a failure has occurred.
     *
     * @return the requested model
     * @see org.gradle.tooling.ModelBuilder#get()
     */
    T executeAndWait();

    /**
     * Fetches the requested model asynchronously. Calling this method will return immediately. The returned promise is used to configure the success and failure behavior.
     *
     * @return the promise of the requested model
     * @see org.gradle.tooling.ModelBuilder#get(org.gradle.tooling.ResultHandler)
     */
    LongRunningOperationPromise<T> execute();
}
