package com.gradleware.tooling.toolingmodel.repository;

import com.google.common.base.Preconditions;
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;

/**
 * Event that is broadcast when {@code OmniEclipseGradleBuild} has been updated.
 */
public final class NewEclipseGradleBuildUpdateEvent {

    private final OmniEclipseGradleBuild eclipseGradleBuild;

    public NewEclipseGradleBuildUpdateEvent(OmniEclipseGradleBuild eclipseGradleBuild) {
        this.eclipseGradleBuild = Preconditions.checkNotNull(eclipseGradleBuild);
    }

    public OmniEclipseGradleBuild getEclipseGradleBuild() {
        return this.eclipseGradleBuild;
    }

}
