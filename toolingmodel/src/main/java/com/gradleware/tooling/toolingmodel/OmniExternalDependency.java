package com.gradleware.tooling.toolingmodel;

import java.io.File;

/**
 * Describes an external artifact dependency.
 */
public interface OmniExternalDependency {

    /**
     * Returns the file for this dependency.
     *
     * @return the file for this dependency
     */
    File getFile();

    /**
     * Returns the source directory or archive for this dependency, or {@code null} if no source is available.
     *
     * @return the source directory or archive for this dependency, or {@code null} if no source is available
     */
    File getSource();

    /**
     * Returns the Javadoc directory or archive for this dependency, or {@code null} if no Javadoc is available.
     *
     * @return the Javadoc directory or archive for this dependency, or {@code null} if no Javadoc is available
     */
    File getJavadoc();

    /**
     * Returns the Gradle module information for this dependency, or {@code null} if the dependency does not originate from a remote repository.
     *
     * @return the Gradle module information for this dependency, or {@code null} if the dependency does not originate from a remote repository
     */
    OmniGradleModuleVersion getGradleModuleVersion();

}
