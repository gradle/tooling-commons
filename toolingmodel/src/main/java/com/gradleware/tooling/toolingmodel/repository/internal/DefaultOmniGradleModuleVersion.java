package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.OmniGradleModuleVersion;
import org.gradle.tooling.model.GradleModuleVersion;

/**
 * Default implementation of the {@link OmniGradleModuleVersion} interface.
 */
public final class DefaultOmniGradleModuleVersion implements OmniGradleModuleVersion {

    private final String group;
    private final String name;
    private final String version;

    private DefaultOmniGradleModuleVersion(String group, String name, String version) {
        this.group = group;
        this.name = name;
        this.version = version;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    public static DefaultOmniGradleModuleVersion from(GradleModuleVersion gradleModuleVersion) {
        return new DefaultOmniGradleModuleVersion(gradleModuleVersion.getGroup(), gradleModuleVersion.getName(), gradleModuleVersion.getVersion());
    }

}
