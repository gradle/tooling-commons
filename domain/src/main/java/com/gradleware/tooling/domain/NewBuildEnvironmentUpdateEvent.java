package com.gradleware.tooling.domain;

import com.google.common.base.Preconditions;
import com.gradleware.tooling.domain.model.OmniBuildEnvironment;

/**
 * Event that is broadcast when {@code OmniBuildEnvironment} has been updated.
 */
public final class NewBuildEnvironmentUpdateEvent {

    private final OmniBuildEnvironment buildEnvironment;

    public NewBuildEnvironmentUpdateEvent(OmniBuildEnvironment buildEnvironment) {
        this.buildEnvironment = Preconditions.checkNotNull(buildEnvironment);
    }

    public OmniBuildEnvironment getBuildEnvironment() {
        return buildEnvironment;
    }

}
