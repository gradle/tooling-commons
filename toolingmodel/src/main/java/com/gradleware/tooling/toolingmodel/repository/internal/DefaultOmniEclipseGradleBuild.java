package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.EclipseGradleProjectFields;
import com.gradleware.tooling.toolingmodel.GradleProjectFields;
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleProject;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import com.gradleware.tooling.toolingmodel.generic.DefaultHierarchicalModel;
import com.gradleware.tooling.toolingmodel.generic.HierarchicalModel;
import org.gradle.tooling.model.eclipse.EclipseProject;

/**
 * Default implementation of the {@link OmniEclipseGradleBuild} interface.
 */
public final class DefaultOmniEclipseGradleBuild implements OmniEclipseGradleBuild {

    private final HierarchicalModel<EclipseGradleProjectFields> rootEclipseProjectModel;
    private final OmniEclipseGradleProject rootEclipseProject;
    private final HierarchicalModel<GradleProjectFields> rootProjectModel;
    private final OmniGradleProject rootProject;

    public DefaultOmniEclipseGradleBuild(HierarchicalModel<EclipseGradleProjectFields> rootEclipseProjectModel, OmniEclipseGradleProject rootEclipseProject, HierarchicalModel<GradleProjectFields> rootProjectModel, OmniGradleProject rootProject) {
        this.rootEclipseProjectModel = rootEclipseProjectModel;
        this.rootEclipseProject = rootEclipseProject;
        this.rootProjectModel = rootProjectModel;
        this.rootProject = rootProject;
    }

    @Override
    public OmniEclipseGradleProject getRootEclipseProject() {
        return this.rootEclipseProject;
    }

    @Override
    public HierarchicalModel<EclipseGradleProjectFields> getRootEclipseProjectModel() {
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
        HierarchicalModel<EclipseGradleProjectFields> rootEclipseProjectModel = DefaultOmniEclipseGradleProject.from(eclipseGradleRootProject);
        OmniEclipseGradleProject rootEclipseProject = DefaultOmniEclipseGradleProject.from(rootEclipseProjectModel);
        DefaultHierarchicalModel<GradleProjectFields> rootGradleProjectModel = DefaultOmniGradleProject.from(eclipseGradleRootProject.getGradleProject(), enforceAllTasksPublic);
        DefaultOmniGradleProject rootGradleProject = DefaultOmniGradleProject.from(rootGradleProjectModel);
        return new DefaultOmniEclipseGradleBuild(rootEclipseProjectModel, rootEclipseProject, rootGradleProjectModel, rootGradleProject);
    }

}
