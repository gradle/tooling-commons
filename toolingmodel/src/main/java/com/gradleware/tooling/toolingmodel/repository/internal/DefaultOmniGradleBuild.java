package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Preconditions;
import com.gradleware.tooling.toolingmodel.OmniGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import org.gradle.tooling.model.GradleProject;

/**
 * Default implementation of the {@link OmniGradleBuild} interface.
 */
public final class DefaultOmniGradleBuild implements OmniGradleBuild {

    private final OmniGradleProject rootProject;

    private DefaultOmniGradleBuild(OmniGradleProject rootProject) {
        this.rootProject = rootProject;
    }

    @Override
    public OmniGradleProject getRootProject() {
        return this.rootProject;
    }

    public static DefaultOmniGradleBuild from(GradleProject gradleRootProject, boolean enforceAllTasksPublic) {
        Preconditions.checkState(gradleRootProject.getParent() == null, "Provided Gradle project is not the root project.");
        return new DefaultOmniGradleBuild(DefaultOmniGradleProject.from(gradleRootProject, enforceAllTasksPublic));
    }

}
