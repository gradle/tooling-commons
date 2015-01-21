package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Preconditions;
import com.gradleware.tooling.toolingmodel.EclipseProjectFields;
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import com.gradleware.tooling.toolingmodel.generic.HierarchicalModel;
import org.gradle.tooling.model.eclipse.EclipseProject;

/**
 * Default implementation of the {@link OmniEclipseGradleBuild} interface.
 */
public final class DefaultOmniEclipseGradleBuild implements OmniEclipseGradleBuild {

    private final OmniEclipseProject rootEclipseProject;
    private final OmniGradleProject rootProject;

    private DefaultOmniEclipseGradleBuild(OmniEclipseProject rootEclipseProject, OmniGradleProject rootProject) {
        this.rootEclipseProject = rootEclipseProject;
        this.rootProject = rootProject;
    }

    @Override
    public OmniEclipseProject getRootEclipseProject() {
        return this.rootEclipseProject;
    }

    @Override
    public OmniGradleProject getRootProject() {
        return this.rootProject;
    }

    public static DefaultOmniEclipseGradleBuild from(EclipseProject eclipseProject, boolean enforceAllTasksPublic) {
        Preconditions.checkState(eclipseProject.getParent() == null, "Provided Eclipse project is not the root project.");
        HierarchicalModel<EclipseProjectFields> rootEclipseProjectModel = DefaultOmniEclipseProject.from(eclipseProject);
        OmniEclipseProject rootEclipseProject = DefaultOmniEclipseProject.from(rootEclipseProjectModel);
        return new DefaultOmniEclipseGradleBuild(
                rootEclipseProject,
                DefaultOmniGradleProject.from(eclipseProject.getGradleProject(), enforceAllTasksPublic));
    }

}
