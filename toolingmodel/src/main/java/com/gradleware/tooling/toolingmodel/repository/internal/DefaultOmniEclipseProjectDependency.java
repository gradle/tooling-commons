package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.EclipseProjectDependencyFields;
import com.gradleware.tooling.toolingmodel.OmniEclipseProjectDependency;
import com.gradleware.tooling.toolingmodel.generic.DefaultModel;
import com.gradleware.tooling.toolingmodel.generic.Model;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.eclipse.EclipseProjectDependency;

/**
 * Default implementation of the {@link OmniEclipseProjectDependency} interface.
 */
public final class DefaultOmniEclipseProjectDependency implements OmniEclipseProjectDependency {

    private final String targetProjectPath;
    private final String path;

    private DefaultOmniEclipseProjectDependency(String targetProjectPath, String path) {
        this.targetProjectPath = targetProjectPath;
        this.path = path;
    }

    @Override
    public String getTargetProjectPath() {
        return this.targetProjectPath;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    public static DefaultOmniEclipseProjectDependency from(Model<EclipseProjectDependencyFields> input) {
        return new DefaultOmniEclipseProjectDependency(input.get(EclipseProjectDependencyFields.TARGET_PROJECT_PATH), input.get(EclipseProjectDependencyFields.PATH));
    }

    public static Model<EclipseProjectDependencyFields> from(EclipseProjectDependency input) {
        DefaultModel<EclipseProjectDependencyFields> projectDependency = new DefaultModel<EclipseProjectDependencyFields>();
        projectDependency.put(EclipseProjectDependencyFields.TARGET_PROJECT_PATH, ((EclipseProject) input.getTargetProject()).getGradleProject().getPath());
        projectDependency.put(EclipseProjectDependencyFields.PATH, input.getPath());
        return projectDependency;
    }

}
