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
import com.gradleware.tooling.domain.model.generic.DefaultHierarchicalDomainObject;
import com.gradleware.tooling.domain.model.generic.DomainObject;
import com.gradleware.tooling.domain.model.generic.DomainObjectField;
import com.gradleware.tooling.domain.model.generic.HierarchicalDomainObject;
import org.gradle.tooling.model.gradle.BasicGradleProject;

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

    public static DefaultOmniGradleProjectStructure from(HierarchicalDomainObject<BasicGradleProjectFields> project) {
        DefaultOmniGradleProjectStructure gradleProjectStructure = new DefaultOmniGradleProjectStructure(OmniGradleProjectStructureComparator.INSTANCE);
        gradleProjectStructure.setName(project.get(BasicGradleProjectFields.NAME));
        gradleProjectStructure.setPath(project.get(BasicGradleProjectFields.PATH));
        gradleProjectStructure.setProjectDirectory(project.get(BasicGradleProjectFields.PROJECT_DIRECTORY));

        for (HierarchicalDomainObject<BasicGradleProjectFields> child : project.getChildren()) {
            DefaultOmniGradleProjectStructure gradleProjectStructureChild = from(child);
            gradleProjectStructure.addChild(gradleProjectStructureChild);
        }

        return gradleProjectStructure;
    }

    public static DefaultHierarchicalDomainObject<BasicGradleProjectFields> from(BasicGradleProject project) {
        DefaultHierarchicalDomainObject<BasicGradleProjectFields> basicGradleProject = new DefaultHierarchicalDomainObject<BasicGradleProjectFields>(BasicGradleProjectComparator.INSTANCE);
        basicGradleProject.put(BasicGradleProjectFields.NAME, project.getName());
        basicGradleProject.put(BasicGradleProjectFields.PATH, project.getPath());
        setProjectDirectory(basicGradleProject, BasicGradleProjectFields.PROJECT_DIRECTORY, project);

        for (BasicGradleProject child : project.getChildren()) {
            DefaultHierarchicalDomainObject<BasicGradleProjectFields> basicGradleProjectChild = from(child);
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
     * Compares OmniGradleProjectStructures by their project path.
     */
    private static final class OmniGradleProjectStructureComparator implements Comparator<OmniGradleProjectStructure> {

        public static final OmniGradleProjectStructureComparator INSTANCE = new OmniGradleProjectStructureComparator();

        @Override
        public int compare(OmniGradleProjectStructure o1, OmniGradleProjectStructure o2) {
            return PathComparator.INSTANCE.compare(o1.getPath(), o2.getPath());
        }

    }

    /**
     * Compares BasicGradleProjects by their project path.
     */
    private static final class BasicGradleProjectComparator implements Comparator<DomainObject<BasicGradleProjectFields>> {

        public static final BasicGradleProjectComparator INSTANCE = new BasicGradleProjectComparator();

        @Override
        public int compare(DomainObject<BasicGradleProjectFields> o1, DomainObject<BasicGradleProjectFields> o2) {
            return PathComparator.INSTANCE.compare(o1.get(BasicGradleProjectFields.PATH), o2.get(BasicGradleProjectFields.PATH));
        }

    }

}

