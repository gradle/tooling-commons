package com.gradleware.tooling.domain.model;

import com.gradleware.tooling.domain.model.generic.HierarchicalDomainObject;

/**
 * Provides information about the Gradle build structure.
 *
 * @see org.gradle.tooling.model.gradle.GradleBuild
 */
public interface OmniGradleBuildStructure {

    /**
     * Returns the root project of the build.
     *
     * @return the root project
     */
    HierarchicalDomainObject<BasicGradleProjectFields> getRootProject();

}
