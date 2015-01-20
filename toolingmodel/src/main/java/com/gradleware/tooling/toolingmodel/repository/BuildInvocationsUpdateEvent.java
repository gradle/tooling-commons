package com.gradleware.tooling.toolingmodel.repository;

import com.google.common.base.Preconditions;
import com.gradleware.tooling.toolingmodel.OmniBuildInvocationsContainer;

/**
 * Event that is broadcast when {@code OmniBuildInvocations} of one or more projects have been updated.
 */
public final class BuildInvocationsUpdateEvent {

    private final OmniBuildInvocationsContainer buildInvocations;

    public BuildInvocationsUpdateEvent(OmniBuildInvocationsContainer buildInvocations) {
        this.buildInvocations = Preconditions.checkNotNull(buildInvocations);
    }

    public OmniBuildInvocationsContainer getBuildInvocations() {
        return this.buildInvocations;
    }

}
