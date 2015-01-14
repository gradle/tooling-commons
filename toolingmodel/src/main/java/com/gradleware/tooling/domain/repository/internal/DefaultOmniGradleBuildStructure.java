package com.gradleware.tooling.domain.repository.internal;

import com.gradleware.tooling.domain.BasicGradleProjectFields;
import com.gradleware.tooling.domain.OmniGradleBuildStructure;
import com.gradleware.tooling.domain.OmniGradleProjectStructure;
import com.gradleware.tooling.domain.generic.HierarchicalModel;
import org.gradle.tooling.model.gradle.GradleBuild;

/**
 * Default implementation of the {@link OmniGradleBuildStructure} interface.
 */
public final class DefaultOmniGradleBuildStructure implements OmniGradleBuildStructure {

    private final HierarchicalModel<BasicGradleProjectFields> rootProjectModel;
    private final OmniGradleProjectStructure rootProject;

    private DefaultOmniGradleBuildStructure(HierarchicalModel<BasicGradleProjectFields> rootProjectModel, OmniGradleProjectStructure rootProject) {
        this.rootProjectModel = rootProjectModel;
        this.rootProject = rootProject;
    }

    @Override
    public OmniGradleProjectStructure getRootProject() {
        return this.rootProject;
    }

    @Override
    public HierarchicalModel<BasicGradleProjectFields> getRootProjectModel() {
        return this.rootProjectModel;
    }

    public static DefaultOmniGradleBuildStructure from(GradleBuild gradleBuild) {
        HierarchicalModel<BasicGradleProjectFields> rootProjectModel = DefaultOmniGradleProjectStructure.from(gradleBuild.getRootProject());
        OmniGradleProjectStructure rootProject = DefaultOmniGradleProjectStructure.from(rootProjectModel);
        return new DefaultOmniGradleBuildStructure(rootProjectModel, rootProject);
    }

}
