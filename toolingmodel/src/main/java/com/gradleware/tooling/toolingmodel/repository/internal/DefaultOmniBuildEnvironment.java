package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniGradleEnvironment;
import com.gradleware.tooling.toolingmodel.OmniJavaEnvironment;
import org.gradle.tooling.model.build.BuildEnvironment;

/**
 * Default implementation of the {@link OmniBuildEnvironment} interface.
 */
public final class DefaultOmniBuildEnvironment implements OmniBuildEnvironment {

    private final OmniGradleEnvironment gradle;
    private final OmniJavaEnvironment java;

    private DefaultOmniBuildEnvironment(OmniGradleEnvironment gradle, OmniJavaEnvironment java) {
        this.gradle = gradle;
        this.java = java;
    }

    @Override
    public OmniGradleEnvironment getGradle() {
        return this.gradle;
    }

    @Override
    public OmniJavaEnvironment getJava() {
        return this.java;
    }

    public static DefaultOmniBuildEnvironment from(BuildEnvironment buildEnvironment) {
        return new DefaultOmniBuildEnvironment(
                DefaultOmniGradleEnvironment.from(buildEnvironment.getGradle()),
                DefaultOmniJavaEnvironment.from(buildEnvironment.getJava()));
    }

}
