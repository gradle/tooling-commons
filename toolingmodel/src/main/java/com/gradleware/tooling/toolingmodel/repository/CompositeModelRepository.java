package com.gradleware.tooling.toolingmodel.repository;

import com.gradleware.tooling.toolingmodel.OmniEclipseWorkspace;

public interface CompositeModelRepository {
    /**
     * Registers the given {@code listener} to receive model change events.
     */
    void register(Object listener);

    /**
     * Unregisters the given {@code listener} from receiving model change events.
     */
    void unregister(Object listener);

    /**
     * Fetches the {@link OmniEclipseWorkspace} synchronously and broadcasts it through a {@link EclipseWorkspaceUpdateEvent}
     */
    OmniEclipseWorkspace fetchEclipseWorkspace(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);
}
