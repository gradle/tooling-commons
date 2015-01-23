package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.OmniExternalDependency;
import com.gradleware.tooling.toolingmodel.OmniGradleModuleVersion;
import com.gradleware.tooling.toolingmodel.util.Maybe;
import org.gradle.tooling.model.ExternalDependency;
import org.gradle.tooling.model.GradleModuleVersion;

import java.io.File;

/**
 * Default implementation of the {@link OmniExternalDependency} interface.
 */
public final class DefaultOmniExternalDependency implements OmniExternalDependency {

    private final File file;
    private final File source;
    private final File javadoc;
    private final Maybe<OmniGradleModuleVersion> gradleModuleVersion;

    private DefaultOmniExternalDependency(File file, File source, File javadoc, Maybe<OmniGradleModuleVersion> gradleModuleVersion) {
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
    public Maybe<OmniGradleModuleVersion> getGradleModuleVersion() {
        return this.gradleModuleVersion;
    }

    public static DefaultOmniExternalDependency from(ExternalDependency externalDependency) {
        return new DefaultOmniExternalDependency(
                externalDependency.getFile(),
                externalDependency.getSource(),
                externalDependency.getJavadoc(),
                getGradleModuleVersion(externalDependency));
    }

    /**
     * ExternalDependency#getGradleModuleVersion is only available in Gradle versions >= 1.1.
     *
     * @param externalDependency the external dependency model
     */
    private static Maybe<OmniGradleModuleVersion> getGradleModuleVersion(ExternalDependency externalDependency) {
        try {
            GradleModuleVersion gav = externalDependency.getGradleModuleVersion();
            return Maybe.of((OmniGradleModuleVersion) DefaultOmniGradleModuleVersion.from(gav));
        } catch (Exception ignore) {
            return Maybe.absent();
        }
    }

}
