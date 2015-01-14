package com.gradleware.tooling.toolingmodel.repository;

import com.google.common.base.Preconditions;

/**
 * Event that is broadcast when {@code BuildInvocations} of one or more projects have been updated.
 */
public final class BuildInvocationsUpdateEvent {

    private final BuildInvocationsContainer buildInvocations;

    public BuildInvocationsUpdateEvent(BuildInvocationsContainer buildInvocations) {
        this.buildInvocations = Preconditions.checkNotNull(buildInvocations);
    }

    public BuildInvocationsContainer getBuildInvocations() {
        return this.buildInvocations;
    }

}
