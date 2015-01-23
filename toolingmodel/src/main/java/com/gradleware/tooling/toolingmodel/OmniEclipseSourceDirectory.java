package com.gradleware.tooling.toolingmodel;

import java.io.File;

/**
 * Describes a source directory in an Eclipse project.
 */
public interface OmniEclipseSourceDirectory {

    /**
     * Returns the source directory.
     *
     * @return the source directory
     */
    File getDirectory();

    /**
     * Returns the relative path of this source directory.
     *
     * @return the relative path of this source directory
     */
    String getPath();

}
