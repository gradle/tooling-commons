package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.gradleware.tooling.toolingmodel.BuildInvocationFields;
import com.gradleware.tooling.toolingmodel.GradleProjectFields;
import com.gradleware.tooling.toolingmodel.GradleScriptFields;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import com.gradleware.tooling.toolingmodel.OmniGradleScript;
import com.gradleware.tooling.toolingmodel.OmniProjectTask;
import com.gradleware.tooling.toolingmodel.OmniTaskSelector;
import com.gradleware.tooling.toolingmodel.ProjectTaskFields;
import com.gradleware.tooling.toolingmodel.TaskSelectorsFields;
import com.gradleware.tooling.toolingmodel.generic.DefaultHierarchicalModel;
import com.gradleware.tooling.toolingmodel.generic.DefaultModel;
import com.gradleware.tooling.toolingmodel.generic.HierarchicalModel;
import com.gradleware.tooling.toolingmodel.generic.Model;
import com.gradleware.tooling.toolingmodel.generic.ModelField;
import org.gradle.tooling.model.GradleProject;
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
    private ImmutableList<OmniProjectTask> projectTasks;
    private ImmutableList<OmniTaskSelector> taskSelectors;

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
    public ImmutableList<OmniProjectTask> getProjectTasks() {
        return this.projectTasks;
    }

    public void setProjectTasks(List<OmniProjectTask> projectTasks) {
        this.projectTasks = ImmutableList.copyOf(projectTasks);
    }

    @Override
    public ImmutableList<OmniTaskSelector> getTaskSelectors() {
        return this.taskSelectors;
    }

    public void setTaskSelectors(List<OmniTaskSelector> taskSelectors) {
        this.taskSelectors = ImmutableList.copyOf(taskSelectors);
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
        return ImmutableList.<OmniGradleProject>copyOf(sort(this.children));
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

    public static DefaultOmniGradleProject from(HierarchicalModel<GradleProjectFields> project) {
        DefaultOmniGradleProject gradleProject = new DefaultOmniGradleProject(OmniGradleProjectComparator.INSTANCE);
        gradleProject.setName(project.get(GradleProjectFields.NAME));
        gradleProject.setDescription(project.get(GradleProjectFields.DESCRIPTION));
        gradleProject.setPath(project.get(GradleProjectFields.PATH));
        gradleProject.setProjectDirectory(project.get(GradleProjectFields.PROJECT_DIRECTORY));
        gradleProject.setBuildDirectory(project.get(GradleProjectFields.BUILD_DIRECTORY));
        gradleProject.setBuildScript(DefaultOmniGradleScript.from(project.get(GradleProjectFields.BUILD_SCRIPT)));
        gradleProject.setProjectTasks(toProjectTasks(project.get(GradleProjectFields.PROJECT_TASKS)));
        gradleProject.setTaskSelectors(toSelectorTasks(project.get(GradleProjectFields.TASK_SELECTORS)));

        for (HierarchicalModel<GradleProjectFields> child : project.getChildren()) {
            DefaultOmniGradleProject gradleProjectChild = from(child);
            gradleProject.addChild(gradleProjectChild);
        }

        return gradleProject;
    }

    private static ImmutableList<OmniProjectTask> toProjectTasks(List<Model<ProjectTaskFields>> projectTasks) {
        return FluentIterable.from(projectTasks).transform(new Function<Model<ProjectTaskFields>, OmniProjectTask>() {
            @Override
            public DefaultOmniProjectTask apply(Model<ProjectTaskFields> input) {
                return DefaultOmniProjectTask.from(input);
            }
        }).toList();
    }

    private static ImmutableList<OmniTaskSelector> toSelectorTasks(List<Model<TaskSelectorsFields>> projectTasks) {
        return FluentIterable.from(projectTasks).transform(new Function<Model<TaskSelectorsFields>, OmniTaskSelector>() {
            @Override
            public DefaultOmniTaskSelector apply(Model<TaskSelectorsFields> input) {
                return DefaultOmniTaskSelector.from(input);
            }
        }).toList();
    }

    public static DefaultHierarchicalModel<GradleProjectFields> from(GradleProject gradleProject, boolean enforceAllTasksPublic) {
        BuildInvocationsContainer buildInvocationsContainer = BuildInvocationsContainerFactory.createFrom(gradleProject, enforceAllTasksPublic);
        return convert(gradleProject, buildInvocationsContainer);
    }

    public static DefaultHierarchicalModel<GradleProjectFields> convert(GradleProject project, BuildInvocationsContainer buildInvocationsContainer) {
        DefaultHierarchicalModel<GradleProjectFields> gradleProject = new DefaultHierarchicalModel<GradleProjectFields>(GradleProjectComparator.INSTANCE);
        gradleProject.put(GradleProjectFields.NAME, project.getName());
        gradleProject.put(GradleProjectFields.DESCRIPTION, project.getDescription());
        gradleProject.put(GradleProjectFields.PATH, project.getPath());
        setProjectDirectory(gradleProject, GradleProjectFields.PROJECT_DIRECTORY, project);
        setBuildDirectory(gradleProject, GradleProjectFields.BUILD_DIRECTORY, project);
        setBuildScript(gradleProject, GradleProjectFields.BUILD_SCRIPT, project);
        Model<BuildInvocationFields> buildInvocations = buildInvocationsContainer.asMap().get(project.getPath());
        gradleProject.put(GradleProjectFields.PROJECT_TASKS, buildInvocations.get(BuildInvocationFields.PROJECT_TASKS));
        gradleProject.put(GradleProjectFields.TASK_SELECTORS, buildInvocations.get(BuildInvocationFields.TASK_SELECTORS));

        for (GradleProject child : project.getChildren()) {
            DefaultHierarchicalModel<GradleProjectFields> gradleProjectChild = convert(child, buildInvocationsContainer);
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
    private static void setProjectDirectory(DefaultHierarchicalModel<GradleProjectFields> gradleProject, ModelField<File, GradleProjectFields> projectDirectoryField, GradleProject project) {
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
    private static void setBuildDirectory(DefaultHierarchicalModel<GradleProjectFields> gradleProject, ModelField<File, GradleProjectFields> buildDirectoryField, GradleProject project) {
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
    private static void setBuildScript(DefaultHierarchicalModel<GradleProjectFields> gradleProject, ModelField<Model<GradleScriptFields>, GradleProjectFields> buildScriptField, GradleProject project) {
        try {
            GradleScript buildScriptOrigin = project.getBuildScript();
            DefaultModel<GradleScriptFields> buildScript = new DefaultModel<GradleScriptFields>();
            buildScript.put(GradleScriptFields.SOURCE_FILE, buildScriptOrigin.getSourceFile());
            gradleProject.put(buildScriptField, buildScript);
        } catch (Exception ignore) {
            // do not store if field value is not present
        }
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
    private static final class GradleProjectComparator implements Comparator<Model<GradleProjectFields>> {

        public static final GradleProjectComparator INSTANCE = new GradleProjectComparator();

        @Override
        public int compare(Model<GradleProjectFields> o1, Model<GradleProjectFields> o2) {
            return PathComparator.INSTANCE.compare(o1.get(GradleProjectFields.PATH), o2.get(GradleProjectFields.PATH));
        }

    }

}

