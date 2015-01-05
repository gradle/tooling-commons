package com.gradleware.tooling.domain;

import com.google.common.base.Preconditions;
import org.gradle.tooling.model.eclipse.EclipseProject;

/**
 * Event that is broadcast when {@code EclipseProject} has been updated.
 */
public final class EclipseProjectUpdateEvent {

    private final EclipseProject eclipseProject;

    public EclipseProjectUpdateEvent(EclipseProject eclipseProject) {
        this.eclipseProject = Preconditions.checkNotNull(eclipseProject);
    }

    public EclipseProject getEclipseProject() {
        return eclipseProject;
    }

}
