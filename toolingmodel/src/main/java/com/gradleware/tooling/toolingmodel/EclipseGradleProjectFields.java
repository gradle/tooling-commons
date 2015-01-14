package com.gradleware.tooling.toolingmodel;

import com.google.common.base.Suppliers;
import com.google.common.reflect.TypeToken;
import com.gradleware.tooling.toolingmodel.generic.DefaultModelField;
import com.gradleware.tooling.toolingmodel.generic.ModelField;

import java.io.File;

/**
 * Enumerates the detailed information available on a Gradle project, suited for consumption in Eclipse.
 *
 * @see org.gradle.tooling.model.eclipse.EclipseProject
 */
public final class EclipseGradleProjectFields {

    /**
     * The name of this project. Note that the name is not a unique identifier for the project.
     */
    public static final ModelField<String, EclipseGradleProjectFields> NAME =
            new DefaultModelField<String, EclipseGradleProjectFields>(TypeToken.of(String.class), TypeToken.of(EclipseGradleProjectFields.class));

    /**
     * The description of this project, or {@code null} if it has no description.
     */
    public static final ModelField<String, EclipseGradleProjectFields> DESCRIPTION =
            new DefaultModelField<String, EclipseGradleProjectFields>(TypeToken.of(String.class), TypeToken.of(EclipseGradleProjectFields.class));

    /**
     * The path of this project. The path can be used as a unique identifier for the project within a given build.
     */
    public static final ModelField<String, EclipseGradleProjectFields> PATH =
            new DefaultModelField<String, EclipseGradleProjectFields>(TypeToken.of(String.class), TypeToken.of(EclipseGradleProjectFields.class));

    /**
     * The project directory of this project.
     */
    public static final ModelField<File, EclipseGradleProjectFields> PROJECT_DIRECTORY =
            new DefaultModelField<File, EclipseGradleProjectFields>(TypeToken.of(File.class), TypeToken.of(EclipseGradleProjectFields.class), Suppliers.<File>ofInstance(null));

    private EclipseGradleProjectFields() {
    }

}
