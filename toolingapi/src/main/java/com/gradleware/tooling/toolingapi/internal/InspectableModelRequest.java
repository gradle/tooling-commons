package com.gradleware.tooling.toolingapi.internal;

import com.gradleware.tooling.toolingapi.ModelRequest;

/**
 * Internal interface that describes the configurable attributes of the model request.
 */
interface InspectableModelRequest<T> extends InspectableRequest<T>, ModelRequest<T> {

    /**
     * @return never null, DefaultToolingClient requires a model type to execute the request
     * @see DefaultToolingClient#mapToModelBuilder(InspectableModelRequest, org.gradle.tooling.ProjectConnection)
     */
    Class<T> getModelType();

    /**
     * @return never null, DefaultModelBuilder builder takes care of converting null and empty arrays to null
     * @see org.gradle.tooling.internal.consumer.DefaultModelBuilder#forTasks(String...)
     */
    String[] getTasks();

}
