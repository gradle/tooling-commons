package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.EclipseProjectFields;
import com.gradleware.tooling.toolingmodel.GradleProjectFields;
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import com.gradleware.tooling.toolingmodel.generic.DefaultHierarchicalModel;
import com.gradleware.tooling.toolingmodel.generic.HierarchicalModel;
import org.gradle.tooling.model.eclipse.EclipseProject;

/**
 * Default implementation of the {@link OmniEclipseGradleBuild} interface.
 */
public final class DefaultOmniEclipseGradleBuild implements OmniEclipseGradleBuild {

    private final HierarchicalModel<EclipseProjectFields> rootEclipseProjectModel;
    private final OmniEclipseProject rootEclipseProject;
    private final HierarchicalModel<GradleProjectFields> rootProjectModel;
    private final OmniGradleProject rootProject;

    public DefaultOmniEclipseGradleBuild(HierarchicalModel<EclipseProjectFields> rootEclipseProjectModel, OmniEclipseProject rootEclipseProject, HierarchicalModel<GradleProjectFields> rootProjectModel, OmniGradleProject rootProject) {
        this.rootEclipseProjectModel = rootEclipseProjectModel;
        this.rootEclipseProject = rootEclipseProject;
        this.rootProjectModel = rootProjectModel;
        this.rootProject = rootProject;
    }

    @Override
    public OmniEclipseProject getRootEclipseProject() {
        return this.rootEclipseProject;
    }

    @Override
    public HierarchicalModel<EclipseProjectFields> getRootEclipseProjectModel() {
        return this.rootEclipseProjectModel;
    }

    @Override
    public HierarchicalModel<GradleProjectFields> getRootProjectModel() {
        return this.rootProjectModel;
    }

    @Override
    public OmniGradleProject getRootProject() {
        return this.rootProject;
    }

    public static DefaultOmniEclipseGradleBuild from(EclipseProject eclipseGradleRootProject, boolean enforceAllTasksPublic) {
        HierarchicalModel<EclipseProjectFields> rootEclipseProjectModel = DefaultOmniEclipseProject.from(eclipseGradleRootProject);
        OmniEclipseProject rootEclipseProject = DefaultOmniEclipseProject.from(rootEclipseProjectModel);
        DefaultHierarchicalModel<GradleProjectFields> rootGradleProjectModel = DefaultOmniGradleProject.from(eclipseGradleRootProject.getGradleProject(), enforceAllTasksPublic);
        DefaultOmniGradleProject rootGradleProject = DefaultOmniGradleProject.from(rootGradleProjectModel);
        return new DefaultOmniEclipseGradleBuild(rootEclipseProjectModel, rootEclipseProject, rootGradleProjectModel, rootGradleProject);
    }

}
