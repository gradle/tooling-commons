package com.gradleware.tooling.domain.model.internal;

import com.gradleware.tooling.domain.model.GradleProjectFields;
import com.gradleware.tooling.domain.model.OmniGradleBuild;
import com.gradleware.tooling.domain.model.OmniGradleProject;
import com.gradleware.tooling.domain.model.generic.HierarchicalDomainObject;
import org.gradle.tooling.model.GradleProject;

/**
 * Default implementation of the {@link OmniGradleBuild} interface.
 */
public final class DefaultOmniGradleBuild implements OmniGradleBuild {

    private final HierarchicalDomainObject<GradleProjectFields> rootProjectModel;
    private final OmniGradleProject rootProject;

    private DefaultOmniGradleBuild(HierarchicalDomainObject<GradleProjectFields> rootProjectModel, OmniGradleProject rootProject) {
        this.rootProjectModel = rootProjectModel;
        this.rootProject = rootProject;
    }

    @Override
    public OmniGradleProject getRootProject() {
        return this.rootProject;
    }

    @Override
    public HierarchicalDomainObject<GradleProjectFields> getRootProjectModel() {
        return this.rootProjectModel;
    }

    public static DefaultOmniGradleBuild from(GradleProject gradleRootProject, boolean enforceAllTasksPublic) {
        HierarchicalDomainObject<GradleProjectFields> rootProjectModel = DefaultOmniGradleProject.from(gradleRootProject, enforceAllTasksPublic);
        OmniGradleProject rootProject = DefaultOmniGradleProject.from(rootProjectModel);
        return new DefaultOmniGradleBuild(rootProjectModel, rootProject);
    }

}
