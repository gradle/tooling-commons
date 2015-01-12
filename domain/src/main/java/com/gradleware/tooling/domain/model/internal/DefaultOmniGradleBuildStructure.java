package com.gradleware.tooling.domain.model.internal;

import com.gradleware.tooling.domain.model.BasicGradleProjectFields;
import com.gradleware.tooling.domain.model.OmniGradleBuildStructure;
import com.gradleware.tooling.domain.model.generic.DefaultHierarchicalDomainObject;
import com.gradleware.tooling.domain.model.generic.DomainObject;
import com.gradleware.tooling.domain.model.generic.DomainObjectField;
import com.gradleware.tooling.domain.model.generic.HierarchicalDomainObject;
import org.gradle.tooling.model.gradle.BasicGradleProject;
import org.gradle.tooling.model.gradle.GradleBuild;

import java.io.File;
import java.util.Comparator;

/**
 * Default implementation of the {@link OmniGradleBuildStructure} interface.
 */
public final class DefaultOmniGradleBuildStructure implements OmniGradleBuildStructure {

    private final HierarchicalDomainObject<BasicGradleProjectFields> rootProject;

    private DefaultOmniGradleBuildStructure(HierarchicalDomainObject<BasicGradleProjectFields> rootProject) {
        this.rootProject = rootProject;
    }

    @Override
    public HierarchicalDomainObject<BasicGradleProjectFields> getRootProjectModel() {
        return this.rootProject;
    }

    public static DefaultOmniGradleBuildStructure from(GradleBuild gradleBuild) {
        BasicGradleProject rootProject = gradleBuild.getRootProject();
        DefaultHierarchicalDomainObject<BasicGradleProjectFields> basicGradleProject = convert(rootProject);

        return new DefaultOmniGradleBuildStructure(basicGradleProject);
    }

    private static DefaultHierarchicalDomainObject<BasicGradleProjectFields> convert(BasicGradleProject project) {
        DefaultHierarchicalDomainObject<BasicGradleProjectFields> basicGradleProject = new DefaultHierarchicalDomainObject<BasicGradleProjectFields>(ProjectPathComparator.INSTANCE);
        basicGradleProject.put(BasicGradleProjectFields.NAME, project.getName());
        basicGradleProject.put(BasicGradleProjectFields.PATH, project.getPath());
        setProjectDirectory(basicGradleProject, BasicGradleProjectFields.PROJECT_DIRECTORY, project);

        for (BasicGradleProject child : project.getChildren()) {
            DefaultHierarchicalDomainObject<BasicGradleProjectFields> basicGradleProjectChild = convert(child);
            basicGradleProject.addChild(basicGradleProjectChild);
        }

        return basicGradleProject;
    }

    /**
     * BasicGradleProject#getProjectDirectory is only available in Gradle versions >= 1.8.
     *
     * @param basicGradleProject the project to populate
     * @param projectDirectoryField the field from which to derive the default project directory in case it is not available on the project model
     * @param project the project model
     */
    private static void setProjectDirectory(DefaultHierarchicalDomainObject<BasicGradleProjectFields> basicGradleProject, DomainObjectField<File, BasicGradleProjectFields> projectDirectoryField, BasicGradleProject project) {
        try {
            File projectDirectory = project.getProjectDirectory();
            basicGradleProject.put(projectDirectoryField, projectDirectory);
        } catch (Exception ignore) {
            // do not store if field value is not present
        }
    }

    /**
     * Compares BasicGradleProjects by their path.
     */
    private static final class ProjectPathComparator implements Comparator<DomainObject<BasicGradleProjectFields>> {

        public static final ProjectPathComparator INSTANCE = new ProjectPathComparator();

        @Override
        public int compare(DomainObject<BasicGradleProjectFields> o1, DomainObject<BasicGradleProjectFields> o2) {
            return PathComparator.INSTANCE.compare(o1.get(BasicGradleProjectFields.PATH), o2.get(BasicGradleProjectFields.PATH));
        }

    }

}
