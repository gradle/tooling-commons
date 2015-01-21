package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.OmniGradleEnvironment;
import org.gradle.tooling.model.build.GradleEnvironment;

/**
 * Default implementation of the {@link OmniGradleEnvironment} interface.
 */
public final class DefaultOmniGradleEnvironment implements OmniGradleEnvironment {

    private final String gradleVersion;

    private DefaultOmniGradleEnvironment(String gradleVersion) {
        this.gradleVersion = gradleVersion;
    }

    @Override
    public String getGradleVersion() {
        return this.gradleVersion;
    }

    public static DefaultOmniGradleEnvironment from(GradleEnvironment gradleEnvironment) {
        return new DefaultOmniGradleEnvironment(gradleEnvironment.getGradleVersion());
    }

}
