package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.OmniExternalDependency;
import com.gradleware.tooling.toolingmodel.OmniGradleModuleVersion;
import org.gradle.tooling.model.ExternalDependency;

import java.io.File;

/**
 * Default implementation of the {@link OmniExternalDependency} interface.
 */
public final class DefaultOmniExternalDependency implements OmniExternalDependency {

    private final File file;
    private final File source;
    private final File javadoc;
    private final OmniGradleModuleVersion gradleModuleVersion;

    private DefaultOmniExternalDependency(File file, File source, File javadoc, OmniGradleModuleVersion gradleModuleVersion) {
        this.file = file;
        this.source = source;
        this.javadoc = javadoc;
        this.gradleModuleVersion = gradleModuleVersion;
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public File getSource() {
        return this.source;
    }

    @Override
    public File getJavadoc() {
        return this.javadoc;
    }

    @Override
    public OmniGradleModuleVersion getGradleModuleVersion() {
        return this.gradleModuleVersion;
    }

    public static DefaultOmniExternalDependency from(ExternalDependency externalDependency) {
        return new DefaultOmniExternalDependency(
                externalDependency.getFile(),
                externalDependency.getSource(),
                externalDependency.getJavadoc(),
                DefaultOmniGradleModuleVersion.from(externalDependency.getGradleModuleVersion()));
    }

}
