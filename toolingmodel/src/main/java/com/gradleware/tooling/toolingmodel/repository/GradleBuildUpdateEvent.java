package com.gradleware.tooling.toolingmodel.repository;

import com.google.common.base.Preconditions;
import com.gradleware.tooling.toolingmodel.OmniGradleBuild;

/**
 * Event that is broadcast when {@code OmniGradleBuild} has been updated.
 */
public final class GradleBuildUpdateEvent {

    private final OmniGradleBuild gradleBuild;

    public GradleBuildUpdateEvent(OmniGradleBuild gradleBuild) {
        this.gradleBuild = Preconditions.checkNotNull(gradleBuild);
    }

    public OmniGradleBuild getGradleBuild() {
        return this.gradleBuild;
    }

}
