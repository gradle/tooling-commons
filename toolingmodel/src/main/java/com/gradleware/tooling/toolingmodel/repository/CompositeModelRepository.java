package com.gradleware.tooling.toolingmodel.repository;

import com.gradleware.tooling.toolingmodel.OmniEclipseWorkspace;

public interface CompositeModelRepository {

    /**
     * Fetches the {@link OmniEclipseWorkspace} synchronously and broadcasts it through a {@link EclipseWorkspaceUpdateEvent}
     */
    OmniEclipseWorkspace fetchEclipseWorkspace(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);
}
