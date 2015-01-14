package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.gradleware.tooling.toolingmodel.EclipseGradleProjectFields;
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleProject;
import com.gradleware.tooling.toolingmodel.generic.DefaultHierarchicalModel;
import com.gradleware.tooling.toolingmodel.generic.HierarchicalModel;
import com.gradleware.tooling.toolingmodel.generic.Model;
import org.gradle.tooling.model.eclipse.EclipseProject;

import java.io.File;
import java.util.Comparator;
import java.util.List;

/**
 * Default implementation of the {@link OmniEclipseGradleProject} interface.
 */
public final class DefaultOmniEclipseGradleProject implements OmniEclipseGradleProject {

    private final Comparator<? super OmniEclipseGradleProject> comparator;
    private final List<DefaultOmniEclipseGradleProject> children;
    private DefaultOmniEclipseGradleProject parent;
    private String name;
    private String description;
    private String path;
    private File projectDirectory;

    private DefaultOmniEclipseGradleProject(Comparator<? super OmniEclipseGradleProject> comparator) {
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
    public OmniEclipseGradleProject getParent() {
        return this.parent;
    }

    private void setParent(DefaultOmniEclipseGradleProject parent) {
        this.parent = parent;
    }

    @Override
    public ImmutableList<OmniEclipseGradleProject> getChildren() {
        return ImmutableList.<OmniEclipseGradleProject>copyOf(sort(this.children));
    }

    private void addChild(DefaultOmniEclipseGradleProject child) {
        child.setParent(this);
        this.children.add(child);
    }

    @Override
    public ImmutableList<OmniEclipseGradleProject> getAll() {
        ImmutableList.Builder<OmniEclipseGradleProject> all = ImmutableList.builder();
        addRecursively(this, all);
        return sort(all.build());
    }

    private void addRecursively(OmniEclipseGradleProject node, ImmutableList.Builder<OmniEclipseGradleProject> nodes) {
        nodes.add(node);
        for (OmniEclipseGradleProject child : node.getChildren()) {
            addRecursively(child, nodes);
        }
    }

    private <E extends OmniEclipseGradleProject> ImmutableList<E> sort(List<E> elements) {
        return Ordering.from(this.comparator).immutableSortedCopy(elements);
    }

    @Override
    public ImmutableList<OmniEclipseGradleProject> filter(Predicate<? super OmniEclipseGradleProject> predicate) {
        return FluentIterable.from(getAll()).filter(predicate).toList();
    }

    @Override
    public Optional<OmniEclipseGradleProject> tryFind(Predicate<? super OmniEclipseGradleProject> predicate) {
        return Iterables.tryFind(getAll(), predicate);
    }

    public static DefaultOmniEclipseGradleProject from(HierarchicalModel<EclipseGradleProjectFields> project) {
        DefaultOmniEclipseGradleProject gradleProject = new DefaultOmniEclipseGradleProject(OmniEclipseGradleProjectComparator.INSTANCE);
        gradleProject.setName(project.get(EclipseGradleProjectFields.NAME));
        gradleProject.setDescription(project.get(EclipseGradleProjectFields.DESCRIPTION));
        gradleProject.setPath(project.get(EclipseGradleProjectFields.PATH));
        gradleProject.setProjectDirectory(project.get(EclipseGradleProjectFields.PROJECT_DIRECTORY));

        for (HierarchicalModel<EclipseGradleProjectFields> child : project.getChildren()) {
            DefaultOmniEclipseGradleProject gradleProjectChild = from(child);
            gradleProject.addChild(gradleProjectChild);
        }

        return gradleProject;
    }

    public static DefaultHierarchicalModel<EclipseGradleProjectFields> from(EclipseProject project) {
        DefaultHierarchicalModel<EclipseGradleProjectFields> gradleProject = new DefaultHierarchicalModel<EclipseGradleProjectFields>(EclipseGradleProjectComparator.INSTANCE);
        gradleProject.put(EclipseGradleProjectFields.NAME, project.getName());
        gradleProject.put(EclipseGradleProjectFields.DESCRIPTION, project.getDescription());
        gradleProject.put(EclipseGradleProjectFields.PATH, project.getGradleProject().getPath());
        gradleProject.put(EclipseGradleProjectFields.PROJECT_DIRECTORY, project.getProjectDirectory());

        for (EclipseProject child : project.getChildren()) {
            DefaultHierarchicalModel<EclipseGradleProjectFields> gradleProjectChild = from(child);
            gradleProject.addChild(gradleProjectChild);
        }

        return gradleProject;
    }

    /**
     * Compares OmniEclipseGradleProjects by their project path.
     */
    private static final class OmniEclipseGradleProjectComparator implements Comparator<OmniEclipseGradleProject> {

        public static final OmniEclipseGradleProjectComparator INSTANCE = new OmniEclipseGradleProjectComparator();

        @Override
        public int compare(OmniEclipseGradleProject o1, OmniEclipseGradleProject o2) {
            return PathComparator.INSTANCE.compare(o1.getPath(), o2.getPath());
        }

    }

    /**
     * Compares EclipseGradleProjects by their project path.
     */
    private static final class EclipseGradleProjectComparator implements Comparator<Model<EclipseGradleProjectFields>> {

        public static final EclipseGradleProjectComparator INSTANCE = new EclipseGradleProjectComparator();

        @Override
        public int compare(Model<EclipseGradleProjectFields> o1, Model<EclipseGradleProjectFields> o2) {
            return PathComparator.INSTANCE.compare(o1.get(EclipseGradleProjectFields.PATH), o2.get(EclipseGradleProjectFields.PATH));
        }

    }

}

