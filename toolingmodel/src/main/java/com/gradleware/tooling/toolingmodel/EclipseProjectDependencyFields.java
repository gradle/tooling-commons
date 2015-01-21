package com.gradleware.tooling.toolingmodel;

import com.google.common.base.Suppliers;
import com.google.common.reflect.TypeToken;
import com.gradleware.tooling.toolingmodel.generic.DefaultModelField;
import com.gradleware.tooling.toolingmodel.generic.ModelField;

/**
 * Represents a dependency on another Eclipse project.
 *
 * @see org.gradle.tooling.model.eclipse.EclipseProjectDependency
 */
public final class EclipseProjectDependencyFields {

    /**
     * The unique path of the target project of this dependency.
     */
    public static final ModelField<String, EclipseProjectDependencyFields> TARGET_PROJECT_PATH =
            new DefaultModelField<String, EclipseProjectDependencyFields>(TypeToken.of(String.class), TypeToken.of(EclipseProjectDependencyFields.class), Suppliers.<String>ofInstance(null));

    /**
     * The path to use for this project dependency.
     */
    public static final ModelField<String, EclipseProjectDependencyFields> PATH =
            new DefaultModelField<String, EclipseProjectDependencyFields>(TypeToken.of(String.class), TypeToken.of(EclipseProjectDependencyFields.class), Suppliers.<String>ofInstance(null));

    private EclipseProjectDependencyFields() {
    }

}
