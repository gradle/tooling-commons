package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.gradleware.tooling.toolingmodel.EclipseProjectFields;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.generic.DefaultHierarchicalModel;
import com.gradleware.tooling.toolingmodel.generic.HierarchicalModel;
import com.gradleware.tooling.toolingmodel.generic.Model;
import org.gradle.tooling.model.eclipse.EclipseProject;

import java.io.File;
import java.util.Comparator;
import java.util.List;

/**
 * Default implementation of the {@link OmniEclipseProject} interface.
 */
public final class DefaultOmniEclipseProject implements OmniEclipseProject {

    private final Comparator<? super OmniEclipseProject> comparator;
    private final List<DefaultOmniEclipseProject> children;
    private DefaultOmniEclipseProject parent;
    private String name;
    private String description;
    private String path;
    private File projectDirectory;

    private DefaultOmniEclipseProject(Comparator<? super OmniEclipseProject> comparator) {
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
    public OmniEclipseProject getParent() {
        return this.parent;
    }

    private void setParent(DefaultOmniEclipseProject parent) {
        this.parent = parent;
    }

    @Override
    public ImmutableList<OmniEclipseProject> getChildren() {
        return ImmutableList.<OmniEclipseProject>copyOf(sort(this.children));
    }

    private void addChild(DefaultOmniEclipseProject child) {
        child.setParent(this);
        this.children.add(child);
    }

    @Override
    public ImmutableList<OmniEclipseProject> getAll() {
        ImmutableList.Builder<OmniEclipseProject> all = ImmutableList.builder();
        addRecursively(this, all);
        return sort(all.build());
    }

    private void addRecursively(OmniEclipseProject node, ImmutableList.Builder<OmniEclipseProject> nodes) {
        nodes.add(node);
        for (OmniEclipseProject child : node.getChildren()) {
            addRecursively(child, nodes);
        }
    }

    private <E extends OmniEclipseProject> ImmutableList<E> sort(List<E> elements) {
        return Ordering.from(this.comparator).immutableSortedCopy(elements);
    }

    @Override
    public ImmutableList<OmniEclipseProject> filter(Predicate<? super OmniEclipseProject> predicate) {
        return FluentIterable.from(getAll()).filter(predicate).toList();
    }

    @Override
    public Optional<OmniEclipseProject> tryFind(Predicate<? super OmniEclipseProject> predicate) {
        return Iterables.tryFind(getAll(), predicate);
    }

    public static DefaultOmniEclipseProject from(HierarchicalModel<EclipseProjectFields> project) {
        DefaultOmniEclipseProject eclipseProject = new DefaultOmniEclipseProject(OmniEclipseProjectComparator.INSTANCE);
        eclipseProject.setName(project.get(EclipseProjectFields.NAME));
        eclipseProject.setDescription(project.get(EclipseProjectFields.DESCRIPTION));
        eclipseProject.setPath(project.get(EclipseProjectFields.PATH));
        eclipseProject.setProjectDirectory(project.get(EclipseProjectFields.PROJECT_DIRECTORY));

        for (HierarchicalModel<EclipseProjectFields> child : project.getChildren()) {
            DefaultOmniEclipseProject eclipseChildProject = from(child);
            eclipseProject.addChild(eclipseChildProject);
        }

        return eclipseProject;
    }

    public static DefaultHierarchicalModel<EclipseProjectFields> from(EclipseProject project) {
        DefaultHierarchicalModel<EclipseProjectFields> eclipseProject = new DefaultHierarchicalModel<EclipseProjectFields>(EclipseProjectComparator.INSTANCE);
        eclipseProject.put(EclipseProjectFields.NAME, project.getName());
        eclipseProject.put(EclipseProjectFields.DESCRIPTION, project.getDescription());
        eclipseProject.put(EclipseProjectFields.PATH, project.getGradleProject().getPath());
        eclipseProject.put(EclipseProjectFields.PROJECT_DIRECTORY, project.getProjectDirectory());

        for (EclipseProject child : project.getChildren()) {
            DefaultHierarchicalModel<EclipseProjectFields> eclipseChildProject = from(child);
            eclipseProject.addChild(eclipseChildProject);
        }

        return eclipseProject;
    }

    /**
     * Compares OmniEclipseProjects by their project path.
     */
    private static final class OmniEclipseProjectComparator implements Comparator<OmniEclipseProject> {

        public static final OmniEclipseProjectComparator INSTANCE = new OmniEclipseProjectComparator();

        @Override
        public int compare(OmniEclipseProject o1, OmniEclipseProject o2) {
            return PathComparator.INSTANCE.compare(o1.getPath(), o2.getPath());
        }

    }

    /**
     * Compares EclipseProjects by their project path.
     */
    private static final class EclipseProjectComparator implements Comparator<Model<EclipseProjectFields>> {

        public static final EclipseProjectComparator INSTANCE = new EclipseProjectComparator();

        @Override
        public int compare(Model<EclipseProjectFields> o1, Model<EclipseProjectFields> o2) {
            return PathComparator.INSTANCE.compare(o1.get(EclipseProjectFields.PATH), o2.get(EclipseProjectFields.PATH));
        }

    }

}

