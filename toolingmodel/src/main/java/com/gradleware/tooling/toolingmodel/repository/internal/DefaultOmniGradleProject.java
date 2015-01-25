package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingmodel.OmniBuildInvocations;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import com.gradleware.tooling.toolingmodel.OmniGradleScript;
import com.gradleware.tooling.toolingmodel.OmniProjectTask;
import com.gradleware.tooling.toolingmodel.OmniTaskSelector;
import com.gradleware.tooling.toolingmodel.Path;
import com.gradleware.tooling.toolingmodel.util.Maybe;
import org.gradle.api.specs.Spec;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.gradle.GradleScript;

import java.io.File;
import java.util.Comparator;
import java.util.List;

/**
 * Default implementation of the {@link OmniGradleProject} interface.
 */
public final class DefaultOmniGradleProject implements OmniGradleProject {

    private final HierarchyHelper<OmniGradleProject> hierarchyHelper;
    private String name;
    private String description;
    private Path path;
    private Maybe<File> projectDirectory;
    private Maybe<File> buildDirectory;
    private Maybe<OmniGradleScript> buildScript;
    private ImmutableList<OmniProjectTask> projectTasks;
    private ImmutableList<OmniTaskSelector> taskSelectors;

    private DefaultOmniGradleProject(Comparator<? super OmniGradleProject> comparator) {
        this.hierarchyHelper = new HierarchyHelper<OmniGradleProject>(this, Preconditions.checkNotNull(comparator));
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
    public Path getPath() {
        return this.path;
    }

    private void setPath(Path path) {
        this.path = path;
    }

    @Override
    public Maybe<File> getProjectDirectory() {
        return this.projectDirectory;
    }

    private void setProjectDirectory(Maybe<File> projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    @Override
    public Maybe<File> getBuildDirectory() {
        return this.buildDirectory;
    }

    public void setBuildDirectory(Maybe<File> buildDirectory) {
        this.buildDirectory = buildDirectory;
    }

    @Override
    public Maybe<OmniGradleScript> getBuildScript() {
        return this.buildScript;
    }

    public void setBuildScript(Maybe<OmniGradleScript> buildScript) {
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
        return this.hierarchyHelper.getParent();
    }

    private void setParent(DefaultOmniGradleProject parent) {
        this.hierarchyHelper.setParent(parent);
    }

    @Override
    public ImmutableList<OmniGradleProject> getChildren() {
        return this.hierarchyHelper.getChildren();
    }

    private void addChild(DefaultOmniGradleProject child) {
        child.setParent(this);
        this.hierarchyHelper.addChild(child);
    }

    @Override
    public ImmutableList<OmniGradleProject> getAll() {
        return this.hierarchyHelper.getAll();
    }

    @Override
    public ImmutableList<OmniGradleProject> filter(Spec<? super OmniGradleProject> predicate) {
        return this.hierarchyHelper.filter(predicate);
    }

    @Override
    public Optional<OmniGradleProject> tryFind(Spec<? super OmniGradleProject> predicate) {
        return this.hierarchyHelper.tryFind(predicate);
    }

    public static DefaultOmniGradleProject from(GradleProject project, boolean enforceAllTasksPublic) {
        BuildInvocationsContainer buildInvocationsContainer = BuildInvocationsContainer.from(project, enforceAllTasksPublic);
        return convert(project, buildInvocationsContainer);
    }

    private static DefaultOmniGradleProject convert(GradleProject project, BuildInvocationsContainer buildInvocationsContainer) {
        DefaultOmniGradleProject gradleProject = new DefaultOmniGradleProject(OmniGradleProjectComparator.INSTANCE);
        gradleProject.setName(project.getName());
        gradleProject.setDescription(project.getDescription());
        gradleProject.setPath(Path.from(project.getPath()));
        setProjectDirectory(gradleProject, project);
        setBuildDirectory(gradleProject, project);
        setBuildScript(gradleProject, project);
        OmniBuildInvocations buildInvocations = buildInvocationsContainer.asMap().get(Path.from(project.getPath()));
        gradleProject.setProjectTasks(buildInvocations.getProjectTasks());
        gradleProject.setTaskSelectors(buildInvocations.getTaskSelectors());

        for (GradleProject child : project.getChildren()) {
            DefaultOmniGradleProject gradleProjectChild = convert(child, buildInvocationsContainer);
            gradleProject.addChild(gradleProjectChild);
        }

        return gradleProject;
    }

    /**
     * GradleProject#getProjectDirectory is only available in Gradle versions >= 2.4.
     *
     * @param gradleProject the project to populate
     * @param project the project model
     */
    private static void setProjectDirectory(DefaultOmniGradleProject gradleProject, GradleProject project) {
        try {
            File projectDirectory = project.getProjectDirectory();
            gradleProject.setProjectDirectory(Maybe.of(projectDirectory));
        } catch (Exception ignore) {
            gradleProject.setProjectDirectory(Maybe.<File>absent());
        }
    }

    /**
     * GradleProject#getBuildDirectory is only available in Gradle versions >= 2.0.
     *
     * @param gradleProject the project to populate
     * @param project the project model
     */
    private static void setBuildDirectory(DefaultOmniGradleProject gradleProject, GradleProject project) {
        try {
            File buildDirectory = project.getBuildDirectory();
            gradleProject.setBuildDirectory(Maybe.of(buildDirectory));
        } catch (Exception ignore) {
            gradleProject.setBuildDirectory(Maybe.<File>absent());
        }
    }

    /**
     * GradleProject#getBuildScript is only available in Gradle versions >= 1.8.
     *
     * @param gradleProject the project to populate
     * @param project the project model
     */
    private static void setBuildScript(DefaultOmniGradleProject gradleProject, GradleProject project) {
        try {
            GradleScript buildScript = project.getBuildScript();
            gradleProject.setBuildScript(Maybe.<OmniGradleScript>of(DefaultOmniGradleScript.from(buildScript)));
        } catch (Exception ignore) {
            gradleProject.setBuildScript(Maybe.<OmniGradleScript>absent());
        }
    }

    /**
     * Compares OmniGradleProjects by their project path.
     */
    private static final class OmniGradleProjectComparator implements Comparator<OmniGradleProject> {

        public static final OmniGradleProjectComparator INSTANCE = new OmniGradleProjectComparator();

        @Override
        public int compare(OmniGradleProject o1, OmniGradleProject o2) {
            return o1.getPath().compareTo(o2.getPath());
        }

    }

}

