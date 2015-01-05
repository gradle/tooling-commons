package com.gradleware.tooling.domain;

import com.google.common.base.Preconditions;
import org.gradle.tooling.model.gradle.GradleBuild;

/**
 * Event that is broadcast when {@code GradleBuild} has been updated.
 */
public final class GradleBuildUpdateEvent {

    private final GradleBuild gradleBuild;

    public GradleBuildUpdateEvent(GradleBuild gradleBuild) {
        this.gradleBuild = Preconditions.checkNotNull(gradleBuild);
    }

    public GradleBuild getGradleBuild() {
        return gradleBuild;
    }

}
