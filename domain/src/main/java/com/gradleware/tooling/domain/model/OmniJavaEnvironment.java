package com.gradleware.tooling.domain.model;

import java.io.File;
import java.util.List;

/**
 * Provides information about the Java environment.
 */
public interface OmniJavaEnvironment {

    /**
     * Returns the Java home used for Gradle operations (for example running tasks or acquiring model information).
     */
    File getJavaHome();

    /**
     * Returns the JVM arguments used to start the Java process that handles Gradle operations (for example running tasks or acquiring model information). The returned arguments do
     * not include system properties passed as -Dfoo=bar. They may include implicitly immutable system properties like "file.encoding".
     */
    List<String> getJvmArguments();

}
