package com.gradleware.tooling.domain.model.internal;

import com.gradleware.tooling.domain.model.BasicGradleProjectFields;
import com.gradleware.tooling.domain.model.OmniGradleBuildStructure;
import com.gradleware.tooling.domain.model.OmniGradleProjectStructure;
import com.gradleware.tooling.domain.model.generic.HierarchicalModel;
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
