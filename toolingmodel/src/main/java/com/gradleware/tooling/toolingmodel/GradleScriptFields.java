package com.gradleware.tooling.toolingmodel;

import com.google.common.base.Suppliers;
import com.google.common.reflect.TypeToken;
import com.gradleware.tooling.toolingmodel.generic.DefaultModelField;
import com.gradleware.tooling.toolingmodel.generic.ModelField;

import java.io.File;

/**
 * Represents a Gradle script. A Gradle script may be a build script, settings script, or initialization script.
 *
 * @see org.gradle.tooling.model.gradle.GradleScript
 */
public final class GradleScriptFields {

    /**
     * The source file for this script, or {@code null} if this script has no associated source file. If the value is not null, the given source file will exist.
     */
    public static final ModelField<File, GradleScriptFields> SOURCE_FILE =
            new DefaultModelField<File, GradleScriptFields>(TypeToken.of(File.class), TypeToken.of(GradleScriptFields.class), Suppliers.<File>ofInstance(null));

    private GradleScriptFields() {
    }

}
