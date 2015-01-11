package com.gradleware.tooling.domain.model;

import com.google.common.base.Suppliers;
import com.google.common.reflect.TypeToken;
import com.gradleware.tooling.domain.model.generic.DomainObjectField;

import java.io.File;

/**
 * Enumerates the information available on a Gradle project without having to parse its build script.
 *
 * @see org.gradle.tooling.model.gradle.BasicGradleProject
 */
public final class BasicGradleProjectFields {

    /**
     * The name of this project. Note that the name is not a unique identifier for the project.
     */
    public static final DomainObjectField<String, BasicGradleProjectFields> NAME =
            new DomainObjectField<String, BasicGradleProjectFields>(TypeToken.of(String.class), TypeToken.of(BasicGradleProjectFields.class));

    /**
     * The path of this project. The path can be used as a unique identifier for the project within a given build.
     */
    public static final DomainObjectField<String, BasicGradleProjectFields> PATH =
            new DomainObjectField<String, BasicGradleProjectFields>(TypeToken.of(String.class), TypeToken.of(BasicGradleProjectFields.class));

    /**
     * The project directory of this project.
     */
    public static final DomainObjectField<File, BasicGradleProjectFields> PROJECT_DIRECTORY =
            new DomainObjectField<File, BasicGradleProjectFields>(TypeToken.of(File.class), TypeToken.of(BasicGradleProjectFields.class), Suppliers.<File>ofInstance(null));

    private BasicGradleProjectFields() {
    }

}
