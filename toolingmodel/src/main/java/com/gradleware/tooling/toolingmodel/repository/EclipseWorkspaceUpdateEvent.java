package com.gradleware.tooling.toolingmodel.repository;

import com.gradleware.tooling.toolingmodel.OmniEclipseWorkspace;

/**
 * Event that is broadcast when {@link OmniEclipseWorkspace} has been updated
 */
public final class EclipseWorkspaceUpdateEvent {

    private final OmniEclipseWorkspace eclipseWorkspace;

    public EclipseWorkspaceUpdateEvent(OmniEclipseWorkspace eclipseWorkspace) {
        this.eclipseWorkspace = eclipseWorkspace;
    }

    public OmniEclipseWorkspace getEclipseWorkspace() {
        return this.eclipseWorkspace;
    }
}
