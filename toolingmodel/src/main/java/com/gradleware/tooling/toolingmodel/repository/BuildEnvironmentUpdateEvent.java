package com.gradleware.tooling.toolingmodel.repository;

import com.google.common.base.Preconditions;
import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;

/**
 * Event that is broadcast when {@code OmniBuildEnvironment} has been updated.
 */
public final class BuildEnvironmentUpdateEvent {

    private final OmniBuildEnvironment buildEnvironment;

    public BuildEnvironmentUpdateEvent(OmniBuildEnvironment buildEnvironment) {
        this.buildEnvironment = Preconditions.checkNotNull(buildEnvironment);
    }

    public OmniBuildEnvironment getBuildEnvironment() {
        return this.buildEnvironment;
    }

}
