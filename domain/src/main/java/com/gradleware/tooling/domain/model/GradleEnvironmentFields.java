package com.gradleware.tooling.domain.model;

import com.google.common.reflect.TypeToken;
import com.gradleware.tooling.domain.generic.DefaultModelField;
import com.gradleware.tooling.domain.generic.ModelField;

/**
 * Enumerates the information available on the build's Gradle environment.
 *
 * @see org.gradle.tooling.model.build.GradleEnvironment
 */
public final class GradleEnvironmentFields {

    /**
     * The Gradle version used for Gradle operations (for example running tasks or acquiring model information).
     */
    public static final ModelField<String, GradleEnvironmentFields> GRADLE_VERSION =
            new DefaultModelField<String, GradleEnvironmentFields>(TypeToken.of(String.class), TypeToken.of(GradleEnvironmentFields.class));

    private GradleEnvironmentFields() {
    }

}
