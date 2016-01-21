package com.gradleware.tooling.toolingclient.internal;

import com.gradleware.tooling.toolingclient.CompositeModelRequest;
import com.gradleware.tooling.toolingclient.ConnectionDescriptor;


public interface InspectableCompositeModelRequest<T> extends CompositeModelRequest<T> {
    ConnectionDescriptor[] getProjects();
}
