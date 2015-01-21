package com.gradleware.tooling.toolingmodel;

import com.google.common.base.Suppliers;
import com.google.common.reflect.TypeToken;
import com.gradleware.tooling.toolingmodel.generic.DefaultModelField;
import com.gradleware.tooling.toolingmodel.generic.ModelField;

import java.io.File;

/**
 * Represents an external artifact dependency.
 *
 * @see org.gradle.tooling.model.ExternalDependency
 */
public final class ExternalDependencyFields {

    /**
     * The file for this dependency.
     */
    public static final ModelField<File, ExternalDependencyFields> FILE =
            new DefaultModelField<File, ExternalDependencyFields>(TypeToken.of(File.class), TypeToken.of(ExternalDependencyFields.class), Suppliers.<File>ofInstance(null));

    /**
     * The source directory or archive for this dependency.
     */
    public static final ModelField<File, ExternalDependencyFields> SOURCE =
            new DefaultModelField<File, ExternalDependencyFields>(TypeToken.of(File.class), TypeToken.of(ExternalDependencyFields.class), Suppliers.<File>ofInstance(null));

    /**
     * The Javadoc directory or archive for this dependency.
     */
    public static final ModelField<File, ExternalDependencyFields> JAVADOC =
            new DefaultModelField<File, ExternalDependencyFields>(TypeToken.of(File.class), TypeToken.of(ExternalDependencyFields.class), Suppliers.<File>ofInstance(null));

    /**
     * The Gradle module information for this dependency.
     */
    public static final ModelField<OmniGradleModuleVersion, ExternalDependencyFields> GRADLE_MODULE_VERSION =
            new DefaultModelField<OmniGradleModuleVersion, ExternalDependencyFields>(TypeToken.of(OmniGradleModuleVersion.class), TypeToken.of(ExternalDependencyFields.class), Suppliers.<OmniGradleModuleVersion>ofInstance(null));

    private ExternalDependencyFields() {
    }

}
