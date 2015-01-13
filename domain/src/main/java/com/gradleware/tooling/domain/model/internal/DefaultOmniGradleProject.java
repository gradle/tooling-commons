package com.gradleware.tooling.domain.model.internal;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.gradleware.tooling.domain.BuildInvocationsContainer;
import com.gradleware.tooling.domain.internal.BuildInvocationsContainerFactory;
import com.gradleware.tooling.domain.internal.SyntheticTaskSelector;
import com.gradleware.tooling.domain.model.GradleProjectFields;
import com.gradleware.tooling.domain.model.GradleScriptFields;
import com.gradleware.tooling.domain.model.OmniGradleProject;
import com.gradleware.tooling.domain.model.OmniGradleScript;
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
import java.util.List;

/**
 * Default implementation of the {@link OmniGradleProject} interface.
 */
public final class DefaultOmniGradleProject implements OmniGradleProject {

    private final Comparator<? super OmniGradleProject> comparator;
    private final List<DefaultOmniGradleProject> children;
    private DefaultOmniGradleProject parent;
    private String name;
    private String description;
    private String path;
    private File projectDirectory;
    private File buildDirectory;
    private OmniGradleScript buildScript;
    private List<DomainObject<ProjectTaskFields>> projectTasks;
    private List<DomainObject<TaskSelectorsFields>> taskSelectors;

    private DefaultOmniGradleProject(Comparator<? super OmniGradleProject> comparator) {
        this.comparator = Preconditions.checkNotNull(comparator);
        this.children = Lists.newArrayList();
        this.parent = null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    private void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    private void setPath(String path) {
        this.path = path;
    }

    @Override
    public File getProjectDirectory() {
        return this.projectDirectory;
    }

    private void setProjectDirectory(File projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    @Override
    public File getBuildDirectory() {
        return this.buildDirectory;
    }

    public void setBuildDirectory(File buildDirectory) {
        this.buildDirectory = buildDirectory;
    }

    @Override
    public OmniGradleScript getBuildScript() {
        return this.buildScript;
    }

    public void setBuildScript(OmniGradleScript buildScript) {
        this.buildScript = buildScript;
    }

    @Override
    public List<DomainObject<ProjectTaskFields>> getProjectTasks() {
        return this.projectTasks;
    }

    public void setProjectTasks(List<DomainObject<ProjectTaskFields>> projectTasks) {
        this.projectTasks = projectTasks;
    }

    @Override
    public List<DomainObject<TaskSelectorsFields>> getTaskSelectors() {
        return this.taskSelectors;
    }

    public void setTaskSelectors(List<DomainObject<TaskSelectorsFields>> taskSelectors) {
        this.taskSelectors = taskSelectors;
    }

    @Override
    public OmniGradleProject getParent() {
        return this.parent;
    }

    private void setParent(DefaultOmniGradleProject parent) {
        this.parent = parent;
    }

    @Override
    public ImmutableList<OmniGradleProject> getChildren() {
        return ImmutableList.<OmniGradleProject>copyOf(this.children);
    }

    private void addChild(DefaultOmniGradleProject child) {
        child.setParent(this);
        this.children.add(child);
    }

    @Override
    public ImmutableList<OmniGradleProject> getAll() {
        ImmutableList.Builder<OmniGradleProject> all = ImmutableList.builder();
        addRecursively(this, all);
        return sort(all.build());
    }

    private void addRecursively(OmniGradleProject node, ImmutableList.Builder<OmniGradleProject> nodes) {
        nodes.add(node);
        for (OmniGradleProject child : node.getChildren()) {
            addRecursively(child, nodes);
        }
    }

    private <E extends OmniGradleProject> ImmutableList<E> sort(List<E> elements) {
        return Ordering.from(this.comparator).immutableSortedCopy(elements);
    }

    @Override
    public ImmutableList<OmniGradleProject> filter(Predicate<? super OmniGradleProject> predicate) {
        return FluentIterable.from(getAll()).filter(predicate).toList();
    }

    @Override
    public Optional<OmniGradleProject> tryFind(Predicate<? super OmniGradleProject> predicate) {
        return Iterables.tryFind(getAll(), predicate);
    }

    public static DefaultOmniGradleProject from(HierarchicalDomainObject<GradleProjectFields> project) {
        DefaultOmniGradleProject gradleProject = new DefaultOmniGradleProject(OmniGradleProjectComparator.INSTANCE);
        gradleProject.setName(project.get(GradleProjectFields.NAME));
        gradleProject.setDescription(project.get(GradleProjectFields.DESCRIPTION));
        gradleProject.setPath(project.get(GradleProjectFields.PATH));
        gradleProject.setProjectDirectory(project.get(GradleProjectFields.PROJECT_DIRECTORY));
        gradleProject.setBuildDirectory(project.get(GradleProjectFields.BUILD_DIRECTORY));
        gradleProject.setBuildScript(DefaultOmniGradleScript.from(project.get(GradleProjectFields.BUILD_SCRIPT)));
        gradleProject.setProjectTasks(project.get(GradleProjectFields.PROJECT_TASKS));
        gradleProject.setTaskSelectors(project.get(GradleProjectFields.TASK_SELECTORS));

        for (HierarchicalDomainObject<GradleProjectFields> child : project.getChildren()) {
            DefaultOmniGradleProject gradleProjectChild = from(child);
            gradleProject.addChild(gradleProjectChild);
        }

        return gradleProject;
    }

    public static DefaultHierarchicalDomainObject<GradleProjectFields> from(GradleProject gradleProject, boolean enforceAllTasksPublic) {
        BuildInvocationsContainer buildInvocationsContainer = BuildInvocationsContainerFactory.createFrom(gradleProject);
        return convert(gradleProject, buildInvocationsContainer, enforceAllTasksPublic);
    }

    public static DefaultHierarchicalDomainObject<GradleProjectFields> convert(GradleProject project, BuildInvocationsContainer buildInvocations, boolean enforceAllTasksPublic) {
        DefaultHierarchicalDomainObject<GradleProjectFields> gradleProject = new DefaultHierarchicalDomainObject<GradleProjectFields>(GradleProjectComparator.INSTANCE);
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
        return Ordering.from(TaskComparator.INSTANCE).immutableSortedCopy(projectTasks.build());
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
     * Compares OmniGradleProjects by their project path.
     */
    private static final class OmniGradleProjectComparator implements Comparator<OmniGradleProject> {

        public static final OmniGradleProjectComparator INSTANCE = new OmniGradleProjectComparator();

        @Override
        public int compare(OmniGradleProject o1, OmniGradleProject o2) {
            return PathComparator.INSTANCE.compare(o1.getPath(), o2.getPath());
        }

    }

    /**
     * Compares GradleProjects by their project path.
     */
    private static final class GradleProjectComparator implements Comparator<DomainObject<GradleProjectFields>> {

        public static final GradleProjectComparator INSTANCE = new GradleProjectComparator();

        @Override
        public int compare(DomainObject<GradleProjectFields> o1, DomainObject<GradleProjectFields> o2) {
            return PathComparator.INSTANCE.compare(o1.get(GradleProjectFields.PATH), o2.get(GradleProjectFields.PATH));
        }

    }

    /**
     * Compares ProjectTasks by their path.
     */
    private static final class TaskComparator implements Comparator<DomainObject<ProjectTaskFields>> {

        public static final TaskComparator INSTANCE = new TaskComparator();

        @Override
        public int compare(DomainObject<ProjectTaskFields> o1, DomainObject<ProjectTaskFields> o2) {
            return PathComparator.INSTANCE.compare(o1.get(ProjectTaskFields.PATH), o2.get(ProjectTaskFields.PATH));
        }

    }

}

