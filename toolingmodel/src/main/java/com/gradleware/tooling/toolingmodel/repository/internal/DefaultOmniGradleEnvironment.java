package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.OmniGradleEnvironment;
import com.gradleware.tooling.toolingmodel.util.Maybe;
import org.gradle.tooling.model.build.GradleEnvironment;

import java.io.File;

/**
 * Default implementation of the {@link OmniGradleEnvironment} interface.
 */
public final class DefaultOmniGradleEnvironment implements OmniGradleEnvironment {

    private final Maybe<File> gradleUserHome;
    private final String gradleVersion;

    private DefaultOmniGradleEnvironment(Maybe<File> gradleUserHome, String gradleVersion) {
        this.gradleUserHome = gradleUserHome;
        this.gradleVersion = gradleVersion;
    }

    @Override
    public Maybe<File> getGradleUserHome() {
        return this.gradleUserHome;
    }

    @Override
    public String getGradleVersion() {
        return this.gradleVersion;
    }

    public static DefaultOmniGradleEnvironment from(GradleEnvironment gradleEnvironment) {
        return new DefaultOmniGradleEnvironment(getGradleUserHome(gradleEnvironment), gradleEnvironment.getGradleVersion());
    }

    /**
     * GradleEnvironment#getGradleUserHome is only available in Gradle versions >= 2.4.
     *
     * @param gradleEnvironment the Gradle environment model
     */
    private static Maybe<File> getGradleUserHome(GradleEnvironment gradleEnvironment) {
        try {
            File gradleUserHome = gradleEnvironment.getGradleUserHome();
            return Maybe.of(gradleUserHome);
        } catch (Exception ignore) {
            return Maybe.absent();
        }
    }

}
