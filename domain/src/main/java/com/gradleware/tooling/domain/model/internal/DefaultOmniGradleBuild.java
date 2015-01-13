package com.gradleware.tooling.domain.model.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.gradleware.tooling.domain.BuildInvocationsContainer;
import com.gradleware.tooling.domain.internal.BuildInvocationsContainerFactory;
import com.gradleware.tooling.domain.internal.SyntheticTaskSelector;
import com.gradleware.tooling.domain.model.GradleProjectFields;
import com.gradleware.tooling.domain.model.GradleScriptFields;
import com.gradleware.tooling.domain.model.OmniGradleBuild;
import com.gradleware.tooling.domain.model.ProjectTaskFields;
import com.gradleware.tooling.domain.model.TaskSelectorsFields;
import com.gradleware.tooling.domain.model.generic.DefaultDomainObject;
import com.gradleware.tooling.domain.model.generic.DefaultHierarchicalDomainObject;
import com.gradleware.tooling.domain.model.generic.DomainObject;
import com.gradleware.tooling.domain.model.generic.DomainObjectField;
import com.gradleware.tooling.domain.model.generic.HierarchicalDomainObject;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;
import org.gradle.tooling.model.TaskSelector;
import org.gradle.tooling.model.gradle.BuildInvocations;
import org.gradle.tooling.model.gradle.GradleScript;

import java.io.File;
import java.util.Comparator;

/**
 * Default implementation of the {@link OmniGradleBuild} interface.
 */
public final class DefaultOmniGradleBuild implements OmniGradleBuild {

    private final HierarchicalDomainObject<GradleProjectFields> rootProjectModel;

    private DefaultOmniGradleBuild(HierarchicalDomainObject<GradleProjectFields> rootProjectModel) {
        this.rootProjectModel = rootProjectModel;
    }

    @Override
    public HierarchicalDomainObject<GradleProjectFields> getRootProjectModel() {
        return this.rootProjectModel;
    }

    public static DefaultOmniGradleBuild from(GradleProject gradleProject, boolean enforceAllTasksPublic) {
        BuildInvocationsContainer buildInvocationsContainer = BuildInvocationsContainerFactory.createFrom(gradleProject);
        DefaultHierarchicalDomainObject<GradleProjectFields> convertedGradleProject = convert(gradleProject, buildInvocationsContainer, enforceAllTasksPublic);

        return new DefaultOmniGradleBuild(convertedGradleProject);
    }

    private static DefaultHierarchicalDomainObject<GradleProjectFields> convert(GradleProject project, BuildInvocationsContainer buildInvocations, boolean enforceAllTasksPublic) {
        DefaultHierarchicalDomainObject<GradleProjectFields> gradleProject = new DefaultHierarchicalDomainObject<GradleProjectFields>(ProjectPathComparator.INSTANCE);
        gradleProject.put(GradleProjectFields.NAME, project.getName());
        gradleProject.put(GradleProjectFields.DESCRIPTION, project.getDescription());
        gradleProject.put(GradleProjectFields.PATH, project.getPath());
        setProjectDirectory(gradleProject, GradleProjectFields.PROJECT_DIRECTORY, project);
        setBuildDirectory(gradleProject, GradleProjectFields.BUILD_DIRECTORY, project);
        setBuildScript(gradleProject, GradleProjectFields.BUILD_SCRIPT, project);
        gradleProject.put(GradleProjectFields.PROJECT_TASKS, getProjectTasks(project, enforceAllTasksPublic));
        gradleProject.put(GradleProjectFields.TASK_SELECTORS, getTaskSelectors(buildInvocations.asMap().get(project.getPath()), enforceAllTasksPublic));

        for (GradleProject child : project.getChildren()) {
            DefaultHierarchicalDomainObject<GradleProjectFields> gradleProjectChild = convert(child, buildInvocations, enforceAllTasksPublic);
            gradleProject.addChild(gradleProjectChild);
        }

        return gradleProject;
    }

    /**
     * GradleProject#getProjectDirectory is only available in Gradle versions >= 2.4.
     *
     * @param gradleProject the project to populate
     * @param projectDirectoryField the field from which to derive the default project directory in case it is not available on the project model
     * @param project the project model
     */
    private static void setProjectDirectory(DefaultHierarchicalDomainObject<GradleProjectFields> gradleProject, DomainObjectField<File, GradleProjectFields> projectDirectoryField, GradleProject project) {
        try {
            File projectDirectory = project.getProjectDirectory();
            gradleProject.put(projectDirectoryField, projectDirectory);
        } catch (Exception ignore) {
            // do not store if field value is not present
        }
    }

    /**
     * GradleProject#getBuildDirectory is only available in Gradle versions >= 2.0.
     *
     * @param gradleProject the project to populate
     * @param buildDirectoryField the field from which to derive the default build directory in case it is not available on the project model
     * @param project the project model
     */
    private static void setBuildDirectory(DefaultHierarchicalDomainObject<GradleProjectFields> gradleProject, DomainObjectField<File, GradleProjectFields> buildDirectoryField, GradleProject project) {
        try {
            File buildDirectory = project.getBuildDirectory();
            gradleProject.put(buildDirectoryField, buildDirectory);
        } catch (Exception ignore) {
            // do not store if field value is not present
        }
    }

    /**
     * GradleProject#getBuildScript is only available in Gradle versions >= 1.8.
     *
     * @param gradleProject the project to populate
     * @param buildScriptField the field from which to derive the default build script in case it is not available on the project model
     * @param project the project model
     */
    private static void setBuildScript(DefaultHierarchicalDomainObject<GradleProjectFields> gradleProject, DomainObjectField<DomainObject<GradleScriptFields>, GradleProjectFields> buildScriptField, GradleProject project) {
        try {
            GradleScript buildScriptOrigin = project.getBuildScript();
            DefaultDomainObject<GradleScriptFields> buildScript = new DefaultDomainObject<GradleScriptFields>();
            buildScript.put(GradleScriptFields.SOURCE_FILE, buildScriptOrigin.getSourceFile());
            gradleProject.put(buildScriptField, buildScript);
        } catch (Exception ignore) {
            // do not store if field value is not present
        }
    }

    private static ImmutableList<DomainObject<ProjectTaskFields>> getProjectTasks(GradleProject project, boolean enforceAllTasksPublic) {
        ImmutableList.Builder<DomainObject<ProjectTaskFields>> projectTasks = ImmutableList.builder();
        for (GradleTask projectTaskOrigin : project.getTasks()) {
            DefaultDomainObject<ProjectTaskFields> projectTask = new DefaultDomainObject<ProjectTaskFields>();
            projectTask.put(ProjectTaskFields.NAME, projectTaskOrigin.getName());
            projectTask.put(ProjectTaskFields.DESCRIPTION, projectTaskOrigin.getDescription());
            projectTask.put(ProjectTaskFields.PATH, projectTaskOrigin.getPath());
            setIsPublic(projectTask, ProjectTaskFields.IS_PUBLIC, projectTaskOrigin, enforceAllTasksPublic);
            projectTasks.add(projectTask);
        }
        return Ordering.from(TaskPathComparator.INSTANCE).immutableSortedCopy(projectTasks.build());
    }

    /**
     * GradleTask#isPublic is only available in Gradle versions >= 2.1.
     * <p/>
     * For versions 2.1 and 2.2.x, GradleTask#isPublic always returns {@code false} and needs to be corrected to {@code true}.
     *
     * @param projectTask the task to populate
     * @param isPublicField the field from which to derive the default isPublic value in case it is not available on the task model
     * @param task the task model
     * @param enforceAllTasksPublic flag to signal whether all tasks should be treated as public regardless of what the model says
     */
    private static void setIsPublic(DefaultDomainObject<ProjectTaskFields> projectTask, DomainObjectField<Boolean, ProjectTaskFields> isPublicField, GradleTask task, boolean enforceAllTasksPublic) {
        try {
            boolean isPublic = task.isPublic();
            projectTask.put(isPublicField, enforceAllTasksPublic || isPublic);
        } catch (Exception ignore) {
            // do not store if field value is not present
        }
    }

    private static ImmutableList<DomainObject<TaskSelectorsFields>> getTaskSelectors(BuildInvocations buildInvocations, boolean enforceAllTasksPublic) {
        ImmutableList.Builder<DomainObject<TaskSelectorsFields>> taskSelectors = ImmutableList.builder();
        for (TaskSelector taskSelectorOrigin : buildInvocations.getTaskSelectors()) {
            SyntheticTaskSelector syntheticTaskSelector = (SyntheticTaskSelector) taskSelectorOrigin;
            DefaultDomainObject<TaskSelectorsFields> taskSelector = new DefaultDomainObject<TaskSelectorsFields>();
            taskSelector.put(TaskSelectorsFields.NAME, syntheticTaskSelector.getName());
            taskSelector.put(TaskSelectorsFields.DESCRIPTION, syntheticTaskSelector.getDescription());
            taskSelector.put(TaskSelectorsFields.IS_PUBLIC, enforceAllTasksPublic || syntheticTaskSelector.isPublic());
            taskSelector.put(TaskSelectorsFields.SELECTED_TASK_PATHS, syntheticTaskSelector.getTaskNames());
            taskSelectors.add(taskSelector);
        }
        return taskSelectors.build();
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
