package com.gradleware.tooling.domain.model.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.gradleware.tooling.domain.model.GradleProjectFields;
import com.gradleware.tooling.domain.model.GradleScriptFields;
import com.gradleware.tooling.domain.model.OmniGradleBuild;
import com.gradleware.tooling.domain.model.ProjectTaskFields;
import com.gradleware.tooling.domain.model.generic.DefaultDomainObject;
import com.gradleware.tooling.domain.model.generic.DefaultHierarchicalDomainObject;
import com.gradleware.tooling.domain.model.generic.DomainObject;
import com.gradleware.tooling.domain.model.generic.HierarchicalDomainObject;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;
import org.gradle.tooling.model.gradle.GradleScript;

import java.io.File;
import java.util.Comparator;

/**
 * Default implementation of the {@link OmniGradleBuild} interface.
 */
public final class DefaultOmniGradleBuild implements OmniGradleBuild {

    private final HierarchicalDomainObject<GradleProjectFields> rootProject;

    private DefaultOmniGradleBuild(HierarchicalDomainObject<GradleProjectFields> rootProject) {
        this.rootProject = rootProject;
    }

    @Override
    public HierarchicalDomainObject<GradleProjectFields> getRootProject() {
        return this.rootProject;
    }

    public static DefaultOmniGradleBuild from(GradleProject gradleProject, boolean enforceAllTasksPublic) {
        DefaultHierarchicalDomainObject<GradleProjectFields> convertedGradleProject = convert(gradleProject, enforceAllTasksPublic);

        return new DefaultOmniGradleBuild(convertedGradleProject);
    }

    private static DefaultHierarchicalDomainObject<GradleProjectFields> convert(GradleProject project, boolean enforceAllTasksPublic) {
        DefaultHierarchicalDomainObject<GradleProjectFields> gradleProject = new DefaultHierarchicalDomainObject<GradleProjectFields>(ProjectPathComparator.INSTANCE);
        gradleProject.put(GradleProjectFields.NAME, project.getName());
        gradleProject.put(GradleProjectFields.DESCRIPTION, project.getDescription());
        gradleProject.put(GradleProjectFields.PATH, project.getPath());
        gradleProject.put(GradleProjectFields.BUILD_SCRIPT, getBuildScript(project));
        gradleProject.put(GradleProjectFields.BUILD_DIRECTORY, getBuildDirectory(project));
        gradleProject.put(GradleProjectFields.PROJECT_TASKS, getProjectTasks(project, enforceAllTasksPublic));

        for (GradleProject child : project.getChildren()) {
            DefaultHierarchicalDomainObject<GradleProjectFields> gradleProjectChild = convert(child, enforceAllTasksPublic);
            gradleProject.addChild(gradleProjectChild);
        }

        return gradleProject;
    }

    /**
     * GradleProject#getBuildScript is only available in Gradle versions >= 1.8.
     *
     * @param project the project model
     * @return the build script model or null if not available on the project model
     */
    private static DefaultDomainObject<GradleScriptFields> getBuildScript(GradleProject project) {
        GradleScript buildScriptOrigin;
        try {
            buildScriptOrigin = project.getBuildScript();
        } catch (Exception e) {
            buildScriptOrigin = null;
        }

        DefaultDomainObject<GradleScriptFields> buildScript = new DefaultDomainObject<GradleScriptFields>();
        buildScript.put(GradleScriptFields.SOURCE_FILE, buildScriptOrigin != null ? buildScriptOrigin.getSourceFile() : null);
        return buildScript;
    }

    /**
     * GradleProject#getBuildDirectory is only available in Gradle versions >= 2.0.
     *
     * @param project the project model
     * @return the build directory or null if not available on the project model
     */
    private static File getBuildDirectory(GradleProject project) {
        try {
            return project.getBuildDirectory();
        } catch (Exception e) {
            return null;
        }
    }

    private static ImmutableList<DomainObject<ProjectTaskFields>> getProjectTasks(GradleProject project, boolean enforceAllTasksPublic) {
        ImmutableList.Builder<DomainObject<ProjectTaskFields>> projectTasks = ImmutableList.builder();
        DomainObjectSet<? extends GradleTask> projectTasksOrigin = project.getTasks();
        for (GradleTask projectTaskOrigin : projectTasksOrigin) {
            DefaultDomainObject<ProjectTaskFields> projectTask = new DefaultDomainObject<ProjectTaskFields>();
            projectTask.put(ProjectTaskFields.NAME, projectTaskOrigin.getName());
            projectTask.put(ProjectTaskFields.DESCRIPTION, projectTaskOrigin.getDescription());
            projectTask.put(ProjectTaskFields.PATH, projectTaskOrigin.getPath());
            projectTask.put(ProjectTaskFields.IS_PUBLIC, enforceAllTasksPublic || getIsPublic(projectTaskOrigin));
            projectTasks.add(projectTask);
        }
        return Ordering.from(TaskPathComparator.INSTANCE).immutableSortedCopy(projectTasks.build());
    }

    /**
     * GradleTask#isPublic is only available in Gradle versions >= 2.1.
     *
     * @param projectTask the project task
     * @return {@code true } if the task is public or if not available on the task model
     */
    private static boolean getIsPublic(GradleTask projectTask) {
        try {
            return projectTask.isPublic();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Compares GradleProjects by their path.
     */
    private static final class ProjectPathComparator implements Comparator<DomainObject<GradleProjectFields>> {

        public static final ProjectPathComparator INSTANCE = new ProjectPathComparator();

        @Override
        public int compare(DomainObject<GradleProjectFields> o1, DomainObject<GradleProjectFields> o2) {
            return PathComparator.INSTANCE.compare(o1.get(GradleProjectFields.PATH), o2.get(GradleProjectFields.PATH));
        }

    }

    /**
     * Compares ProjectTasks by their path.
     */
    private static final class TaskPathComparator implements Comparator<DomainObject<ProjectTaskFields>> {

        public static final TaskPathComparator INSTANCE = new TaskPathComparator();

        @Override
        public int compare(DomainObject<ProjectTaskFields> o1, DomainObject<ProjectTaskFields> o2) {
            return PathComparator.INSTANCE.compare(o1.get(ProjectTaskFields.PATH), o2.get(ProjectTaskFields.PATH));
        }

    }

}
