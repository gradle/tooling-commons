package com.gradleware.tooling.toolingmodel;

import com.google.common.reflect.TypeToken;
import com.gradleware.tooling.toolingmodel.generic.DefaultModelField;
import com.gradleware.tooling.toolingmodel.generic.ModelField;
import com.gradleware.tooling.toolingmodel.generic.TypeTokens;

import java.io.File;
import java.util.List;

/**
 * Enumerates the information available on the build's Java environment.
 *
 * @see org.gradle.tooling.model.build.JavaEnvironment
 */
public final class JavaEnvironmentFields {

    /**
     * The Java home used for Gradle operations (for example running tasks or acquiring model information).
     */
    public static final ModelField<File, JavaEnvironmentFields> JAVA_HOME =
            new DefaultModelField<File, JavaEnvironmentFields>(TypeToken.of(File.class), TypeToken.of(JavaEnvironmentFields.class));

    /**
     * The JVM arguments used to start the Java process that handles Gradle operations (for example running tasks or acquiring model information). The returned arguments do not
     * include system properties passed as -Dfoo=bar. They may include implicitly immutable system properties like "file.encoding".
     */
    public static final ModelField<List<String>, JavaEnvironmentFields> JVM_ARGS =
            new DefaultModelField<List<String>, JavaEnvironmentFields>(TypeTokens.listToken(String.class), TypeToken.of(JavaEnvironmentFields.class));

    private JavaEnvironmentFields() {
    }

}
