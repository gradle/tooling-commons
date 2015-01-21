package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.ExternalDependencyFields;
import com.gradleware.tooling.toolingmodel.OmniExternalDependency;
import com.gradleware.tooling.toolingmodel.OmniGradleModuleVersion;
import com.gradleware.tooling.toolingmodel.generic.DefaultModel;
import com.gradleware.tooling.toolingmodel.generic.Model;
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

    public static DefaultOmniExternalDependency from(Model<ExternalDependencyFields> input) {
        return new DefaultOmniExternalDependency(
                input.get(ExternalDependencyFields.FILE),
                input.get(ExternalDependencyFields.SOURCE),
                input.get(ExternalDependencyFields.JAVADOC),
                input.get(ExternalDependencyFields.GRADLE_MODULE_VERSION));
    }

    public static Model<ExternalDependencyFields> from(ExternalDependency input) {
        DefaultModel<ExternalDependencyFields> externalDependency = new DefaultModel<ExternalDependencyFields>();
        externalDependency.put(ExternalDependencyFields.FILE, input.getFile());
        externalDependency.put(ExternalDependencyFields.SOURCE, input.getSource());
        externalDependency.put(ExternalDependencyFields.JAVADOC, input.getJavadoc());
        externalDependency.put(ExternalDependencyFields.GRADLE_MODULE_VERSION, DefaultOmniGradleModuleVersion.from(input.getGradleModuleVersion()));
        return externalDependency;
    }

}
