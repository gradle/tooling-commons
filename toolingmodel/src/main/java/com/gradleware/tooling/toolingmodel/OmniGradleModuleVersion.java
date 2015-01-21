package com.gradleware.tooling.toolingmodel;

/**
 * Provides information about a module version, i.e. group, name, version.
 */
public interface OmniGradleModuleVersion {

    /**
     * Returns the group of the module, for example 'org.gradle'.
     *
     * @return the group of the module
     */
    String getGroup();

    /**
     * Returns the name of the module, for example 'gradle-tooling-api'.
     *
     * @return the name of the module
     */
    String getName();

    /**
     * Returns the version of the module, for example '1.0'.
     *
     * @return the version of the module
     */
    String getVersion();

}
