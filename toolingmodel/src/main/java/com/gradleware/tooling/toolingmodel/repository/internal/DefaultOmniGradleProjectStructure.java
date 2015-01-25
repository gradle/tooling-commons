package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingmodel.OmniGradleProjectStructure;
import com.gradleware.tooling.toolingmodel.Path;
import com.gradleware.tooling.toolingmodel.util.Maybe;
import org.gradle.api.specs.Spec;
import org.gradle.tooling.model.gradle.BasicGradleProject;

import java.io.File;
import java.util.Comparator;

/**
 * Default implementation of the {@link OmniGradleProjectStructure} interface.
 */
public final class DefaultOmniGradleProjectStructure implements OmniGradleProjectStructure {

    private final HierarchyHelper<OmniGradleProjectStructure> hierarchyHelper;
    private String name;
    private Path path;
    private Maybe<File> projectDirectory;

    private DefaultOmniGradleProjectStructure(Comparator<? super OmniGradleProjectStructure> comparator) {
        this.hierarchyHelper = new HierarchyHelper<OmniGradleProjectStructure>(this, Preconditions.checkNotNull(comparator));
    }

    @Override
    public String getName() {
        return this.name;
    }

    private void setName(String name) {
        this.name = name;
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
    public OmniGradleProjectStructure getParent() {
        return this.hierarchyHelper.getParent();
    }

    private void setParent(DefaultOmniGradleProjectStructure parent) {
        this.hierarchyHelper.setParent(parent);
    }

    @Override
    public ImmutableList<OmniGradleProjectStructure> getChildren() {
        return this.hierarchyHelper.getChildren();
    }

    private void addChild(DefaultOmniGradleProjectStructure child) {
        child.setParent(this);
        this.hierarchyHelper.addChild(child);
    }

    @Override
    public ImmutableList<OmniGradleProjectStructure> getAll() {
        return this.hierarchyHelper.getAll();
    }

    @Override
    public ImmutableList<OmniGradleProjectStructure> filter(Spec<? super OmniGradleProjectStructure> predicate) {
        return this.hierarchyHelper.filter(predicate);
    }

    @Override
    public Optional<OmniGradleProjectStructure> tryFind(Spec<? super OmniGradleProjectStructure> predicate) {
        return this.hierarchyHelper.tryFind(predicate);
    }

    public static DefaultOmniGradleProjectStructure from(BasicGradleProject project) {
        DefaultOmniGradleProjectStructure projectStructure = new DefaultOmniGradleProjectStructure(OmniGradleProjectStructureComparator.INSTANCE);
        projectStructure.setName(project.getName());
        projectStructure.setPath(Path.from((project.getPath())));
        setProjectDirectory(projectStructure, project);

        for (BasicGradleProject child : project.getChildren()) {
            DefaultOmniGradleProjectStructure basicGradleProjectChild = from(child);
            projectStructure.addChild(basicGradleProjectChild);
        }

        return projectStructure;
    }

    /**
     * BasicGradleProject#getProjectDirectory is only available in Gradle versions >= 1.8.
     *
     * @param projectStructure the project to populate
     * @param project the project model
     */
    private static void setProjectDirectory(DefaultOmniGradleProjectStructure projectStructure, BasicGradleProject project) {
        try {
            File projectDirectory = project.getProjectDirectory();
            projectStructure.setProjectDirectory(Maybe.of(projectDirectory));
        } catch (Exception ignore) {
            projectStructure.setProjectDirectory(Maybe.<File>absent());
        }
    }

    /**
     * Compares OmniGradleProjectStructures by their project path.
     */
    private static final class OmniGradleProjectStructureComparator implements Comparator<OmniGradleProjectStructure> {

        public static final OmniGradleProjectStructureComparator INSTANCE = new OmniGradleProjectStructureComparator();

        @Override
        public int compare(OmniGradleProjectStructure o1, OmniGradleProjectStructure o2) {
            return o1.getPath().compareTo(o2.getPath());
        }

    }

}

