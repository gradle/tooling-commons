package com.gradleware.tooling.toolingmodel.repository;

import com.google.common.base.Preconditions;
import org.gradle.tooling.model.build.BuildEnvironment;

/**
 * Event that is broadcast when {@code BuildEnvironment} has been updated.
 */
public final class BuildEnvironmentUpdateEvent {

    private final BuildEnvironment buildEnvironment;

    public BuildEnvironmentUpdateEvent(BuildEnvironment buildEnvironment) {
        this.buildEnvironment = Preconditions.checkNotNull(buildEnvironment);
    }

    public BuildEnvironment getBuildEnvironment() {
        return this.buildEnvironment;
    }

}
