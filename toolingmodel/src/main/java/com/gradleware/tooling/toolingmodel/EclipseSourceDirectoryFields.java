package com.gradleware.tooling.toolingmodel;

import com.google.common.base.Suppliers;
import com.google.common.reflect.TypeToken;
import com.gradleware.tooling.toolingmodel.generic.DefaultModelField;
import com.gradleware.tooling.toolingmodel.generic.ModelField;

/**
 * Represents a source directory in an Eclipse project.
 *
 * @see org.gradle.tooling.model.eclipse.EclipseSourceDirectory
 */
public final class EclipseSourceDirectoryFields {

    /**
     * The relative path of this source directory.
     */
    public static final ModelField<String, EclipseSourceDirectoryFields> PATH =
            new DefaultModelField<String, EclipseSourceDirectoryFields>(TypeToken.of(String.class), TypeToken.of(EclipseSourceDirectoryFields.class), Suppliers.<String>ofInstance(null));

    private EclipseSourceDirectoryFields() {
    }

}
