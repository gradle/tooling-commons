package com.gradleware.tooling.domain.model;

import java.io.File;

/**
 * Represents a Gradle script. A Gradle script may be a build script, a settings script, or an initialization script.
 */
public interface OmniGradleScript {

    /**
     * Returns the source file for this script, or {@code null} if this script has no associated source file. If this method returns a non-null value, the given source file will
     * exist.
     *
     * @return the source file, null if the script has no associated source file
     */
    File getSourceFile();

}
