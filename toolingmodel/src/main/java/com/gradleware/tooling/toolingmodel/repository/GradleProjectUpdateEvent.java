package com.gradleware.tooling.toolingmodel.repository;

import com.google.common.base.Preconditions;
import org.gradle.tooling.model.GradleProject;

/**
 * Event that is broadcast when {@code GradleProject} has been updated.
 */
public final class GradleProjectUpdateEvent {

    private final GradleProject gradleProject;

    public GradleProjectUpdateEvent(GradleProject gradleProject) {
        this.gradleProject = Preconditions.checkNotNull(gradleProject);
    }

    public GradleProject getGradleProject() {
        return this.gradleProject;
    }

}
