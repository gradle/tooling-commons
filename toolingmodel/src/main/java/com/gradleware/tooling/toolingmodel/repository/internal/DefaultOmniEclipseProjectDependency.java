package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.OmniEclipseProjectDependency;
import com.gradleware.tooling.toolingmodel.Path;
import org.gradle.internal.reflect.JavaReflectionUtil;
import org.gradle.internal.reflect.PropertyAccessor;
import org.gradle.tooling.internal.adapter.ProtocolToModelAdapter;
import org.gradle.tooling.model.eclipse.EclipseProjectDependency;

/**
 * Default implementation of the {@link OmniEclipseProjectDependency} interface.
 */
public final class DefaultOmniEclipseProjectDependency implements OmniEclipseProjectDependency {

    private final Path targetProjectPath;
    private final String path;

    private DefaultOmniEclipseProjectDependency(Path targetProjectPath, String path) {
        this.targetProjectPath = targetProjectPath;
        this.path = path;
    }

    @Override
    public Path getTargetProjectPath() {
        return this.targetProjectPath;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    public static DefaultOmniEclipseProjectDependency from(EclipseProjectDependency projectDependency) {
        // cannot cast due to class in proxy delegate is loaded by a different class loader
        Object targetEclipseProject = new ProtocolToModelAdapter().unpack(projectDependency.getTargetProject());
        PropertyAccessor pathProperty = JavaReflectionUtil.readableProperty(targetEclipseProject.getClass(), "path");
        String targetEclipseProjectPath = (String) pathProperty.getValue(targetEclipseProject);

        return new DefaultOmniEclipseProjectDependency(
                Path.from(targetEclipseProjectPath),
                projectDependency.getPath());
    }

}