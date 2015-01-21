package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.OmniEclipseProjectDependency;
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

    public static DefaultOmniEclipseProjectDependency from(EclipseProjectDependency projectDependency) {
        return new DefaultOmniEclipseProjectDependency(
                ((EclipseProject) projectDependency.getTargetProject()).getGradleProject().getPath(),
                projectDependency.getPath());
    }

}
