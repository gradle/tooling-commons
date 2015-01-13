package com.gradleware.tooling.domain.model.internal;

import com.gradleware.tooling.domain.GradleProjectFields;
import com.gradleware.tooling.domain.OmniGradleBuild;
import com.gradleware.tooling.domain.OmniGradleProject;
import com.gradleware.tooling.domain.generic.HierarchicalModel;
import org.gradle.tooling.model.GradleProject;

/**
 * Default implementation of the {@link OmniGradleBuild} interface.
 */
public final class DefaultOmniGradleBuild implements OmniGradleBuild {

    private final HierarchicalModel<GradleProjectFields> rootProjectModel;
    private final OmniGradleProject rootProject;

    private DefaultOmniGradleBuild(HierarchicalModel<GradleProjectFields> rootProjectModel, OmniGradleProject rootProject) {
        this.rootProjectModel = rootProjectModel;
        this.rootProject = rootProject;
    }

    @Override
    public OmniGradleProject getRootProject() {
        return this.rootProject;
    }

    @Override
    public HierarchicalModel<GradleProjectFields> getRootProjectModel() {
        return this.rootProjectModel;
    }

    public static DefaultOmniGradleBuild from(GradleProject gradleRootProject, boolean enforceAllTasksPublic) {
        HierarchicalModel<GradleProjectFields> rootProjectModel = DefaultOmniGradleProject.from(gradleRootProject, enforceAllTasksPublic);
        OmniGradleProject rootProject = DefaultOmniGradleProject.from(rootProjectModel);
        return new DefaultOmniGradleBuild(rootProjectModel, rootProject);
    }

}
