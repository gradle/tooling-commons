package com.gradleware.tooling.domain.model.internal;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.gradleware.tooling.domain.model.BasicGradleProjectFields;
import com.gradleware.tooling.domain.model.OmniGradleProjectStructure;
import com.gradleware.tooling.domain.model.generic.HierarchicalDomainObject;

import java.io.File;
import java.util.Comparator;
import java.util.List;

/**
 * Default implementation of the {@link OmniGradleProjectStructure} interface.
 */
public final class DefaultOmniGradleProjectStructure implements OmniGradleProjectStructure {

    private final Comparator<? super OmniGradleProjectStructure> comparator;
    private final List<DefaultOmniGradleProjectStructure> children;
    private DefaultOmniGradleProjectStructure parent;
    private String name;
    private String path;
    private File projectDirectory;

    private DefaultOmniGradleProjectStructure(Comparator<? super OmniGradleProjectStructure> comparator) {
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
    public OmniGradleProjectStructure getParent() {
        return this.parent;
    }

    private void setParent(DefaultOmniGradleProjectStructure parent) {
        this.parent = parent;
    }

    @Override
    public ImmutableList<OmniGradleProjectStructure> getChildren() {
        return ImmutableList.<OmniGradleProjectStructure>copyOf(this.children);
    }

    private void addChild(DefaultOmniGradleProjectStructure child) {
        child.setParent(this);
        this.children.add(child);
    }

    @Override
    public ImmutableList<OmniGradleProjectStructure> getAll() {
        ImmutableList.Builder<OmniGradleProjectStructure> all = ImmutableList.builder();
        addRecursively(this, all);
        return sort(all.build());
    }

    private void addRecursively(OmniGradleProjectStructure node, ImmutableList.Builder<OmniGradleProjectStructure> nodes) {
        nodes.add(node);
        for (OmniGradleProjectStructure child : node.getChildren()) {
            addRecursively(child, nodes);
        }
    }

    private <E extends OmniGradleProjectStructure> ImmutableList<E> sort(List<E> elements) {
        return Ordering.from(this.comparator).immutableSortedCopy(elements);
    }

    @Override
    public ImmutableList<OmniGradleProjectStructure> filter(Predicate<? super OmniGradleProjectStructure> predicate) {
        return FluentIterable.from(getAll()).filter(predicate).toList();
    }

    @Override
    public Optional<OmniGradleProjectStructure> tryFind(Predicate<? super OmniGradleProjectStructure> predicate) {
        return Iterables.tryFind(getAll(), predicate);
    }

    public static DefaultOmniGradleProjectStructure from(HierarchicalDomainObject<BasicGradleProjectFields> basicGradleProject) {
        return convert(basicGradleProject);
    }

    private static DefaultOmniGradleProjectStructure convert(HierarchicalDomainObject<BasicGradleProjectFields> project) {
        DefaultOmniGradleProjectStructure gradleProjectStructure = new DefaultOmniGradleProjectStructure(ProjectPathComparator.INSTANCE);
        gradleProjectStructure.setName(project.get(BasicGradleProjectFields.NAME));
        gradleProjectStructure.setPath(project.get(BasicGradleProjectFields.PATH));
        gradleProjectStructure.setProjectDirectory(project.get(BasicGradleProjectFields.PROJECT_DIRECTORY));

        for (HierarchicalDomainObject<BasicGradleProjectFields> child : project.getChildren()) {
            DefaultOmniGradleProjectStructure gradleProjectStructureChild = from(child);
            gradleProjectStructure.addChild(gradleProjectStructureChild);
        }

        return gradleProjectStructure;
    }

    /**
     * Compares OmniGradleProjectStructures by their project path.
     */
    private static final class ProjectPathComparator implements Comparator<OmniGradleProjectStructure> {

        public static final ProjectPathComparator INSTANCE = new ProjectPathComparator();

        @Override
        public int compare(OmniGradleProjectStructure o1, OmniGradleProjectStructure o2) {
            return PathComparator.INSTANCE.compare(o1.getPath(), o2.getPath());
        }

    }

}

