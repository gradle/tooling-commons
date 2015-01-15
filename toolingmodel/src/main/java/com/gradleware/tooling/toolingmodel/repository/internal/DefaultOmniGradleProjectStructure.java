package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingmodel.BasicGradleProjectFields;
import com.gradleware.tooling.toolingmodel.OmniGradleProjectStructure;
import com.gradleware.tooling.toolingmodel.generic.DefaultHierarchicalModel;
import com.gradleware.tooling.toolingmodel.generic.HierarchicalModel;
import com.gradleware.tooling.toolingmodel.generic.Model;
import com.gradleware.tooling.toolingmodel.generic.ModelField;
import org.gradle.tooling.model.gradle.BasicGradleProject;

import java.io.File;
import java.util.Comparator;

/**
 * Default implementation of the {@link OmniGradleProjectStructure} interface.
 */
public final class DefaultOmniGradleProjectStructure implements OmniGradleProjectStructure {

    private final HierarchyHelper<OmniGradleProjectStructure> hierarchyHelper;
    private String name;
    private String path;
    private File projectDirectory;

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
    public ImmutableList<OmniGradleProjectStructure> filter(Predicate<? super OmniGradleProjectStructure> predicate) {
        return this.hierarchyHelper.filter(predicate);
    }

    @Override
    public Optional<OmniGradleProjectStructure> tryFind(Predicate<? super OmniGradleProjectStructure> predicate) {
        return this.hierarchyHelper.tryFind(predicate);
    }

    public static DefaultOmniGradleProjectStructure from(HierarchicalModel<BasicGradleProjectFields> project) {
        DefaultOmniGradleProjectStructure gradleProjectStructure = new DefaultOmniGradleProjectStructure(OmniGradleProjectStructureComparator.INSTANCE);
        gradleProjectStructure.setName(project.get(BasicGradleProjectFields.NAME));
        gradleProjectStructure.setPath(project.get(BasicGradleProjectFields.PATH));
        gradleProjectStructure.setProjectDirectory(project.get(BasicGradleProjectFields.PROJECT_DIRECTORY));

        for (HierarchicalModel<BasicGradleProjectFields> child : project.getChildren()) {
            DefaultOmniGradleProjectStructure gradleProjectStructureChild = from(child);
            gradleProjectStructure.addChild(gradleProjectStructureChild);
        }

        return gradleProjectStructure;
    }

    public static DefaultHierarchicalModel<BasicGradleProjectFields> from(BasicGradleProject project) {
        DefaultHierarchicalModel<BasicGradleProjectFields> basicGradleProject = new DefaultHierarchicalModel<BasicGradleProjectFields>(BasicGradleProjectComparator.INSTANCE);
        basicGradleProject.put(BasicGradleProjectFields.NAME, project.getName());
        basicGradleProject.put(BasicGradleProjectFields.PATH, project.getPath());
        setProjectDirectory(basicGradleProject, BasicGradleProjectFields.PROJECT_DIRECTORY, project);

        for (BasicGradleProject child : project.getChildren()) {
            DefaultHierarchicalModel<BasicGradleProjectFields> basicGradleProjectChild = from(child);
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
    private static void setProjectDirectory(DefaultHierarchicalModel<BasicGradleProjectFields> basicGradleProject, ModelField<File, BasicGradleProjectFields> projectDirectoryField, BasicGradleProject project) {
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
    private static final class BasicGradleProjectComparator implements Comparator<Model<BasicGradleProjectFields>> {

        public static final BasicGradleProjectComparator INSTANCE = new BasicGradleProjectComparator();

        @Override
        public int compare(Model<BasicGradleProjectFields> o1, Model<BasicGradleProjectFields> o2) {
            return PathComparator.INSTANCE.compare(o1.get(BasicGradleProjectFields.PATH), o2.get(BasicGradleProjectFields.PATH));
        }

    }

}

