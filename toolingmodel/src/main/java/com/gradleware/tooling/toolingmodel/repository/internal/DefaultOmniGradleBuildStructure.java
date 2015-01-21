package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure;
import com.gradleware.tooling.toolingmodel.OmniGradleProjectStructure;
import org.gradle.tooling.model.gradle.GradleBuild;

/**
 * Default implementation of the {@link OmniGradleBuildStructure} interface.
 */
public final class DefaultOmniGradleBuildStructure implements OmniGradleBuildStructure {

    private final OmniGradleProjectStructure rootProject;

    private DefaultOmniGradleBuildStructure(OmniGradleProjectStructure rootProject) {
        this.rootProject = rootProject;
    }

    @Override
    public OmniGradleProjectStructure getRootProject() {
        return this.rootProject;
    }

    public static DefaultOmniGradleBuildStructure from(GradleBuild gradleBuild) {
        return new DefaultOmniGradleBuildStructure(DefaultOmniGradleProjectStructure.from(gradleBuild.getRootProject()));
    }

}
