/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gradleware.tooling.toolingmodel.repository.internal;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.gradle.api.JavaVersion;
import org.gradle.api.specs.Spec;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.eclipse.EclipseBuildCommand;
import org.gradle.tooling.model.eclipse.EclipseExternalDependency;
import org.gradle.tooling.model.eclipse.EclipseJavaSourceSettings;
import org.gradle.tooling.model.eclipse.EclipseLinkedResource;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.eclipse.EclipseProjectDependency;
import org.gradle.tooling.model.eclipse.EclipseProjectIdentifier;
import org.gradle.tooling.model.eclipse.EclipseProjectNature;
import org.gradle.tooling.model.eclipse.EclipseSourceDirectory;
import org.gradle.tooling.model.java.InstalledJdk;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import com.gradleware.tooling.toolingmodel.OmniEclipseBuildCommand;
import com.gradleware.tooling.toolingmodel.OmniEclipseLinkedResource;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniEclipseProjectDependency;
import com.gradleware.tooling.toolingmodel.OmniEclipseProjectNature;
import com.gradleware.tooling.toolingmodel.OmniEclipseSourceDirectory;
import com.gradleware.tooling.toolingmodel.OmniExternalDependency;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import com.gradleware.tooling.toolingmodel.OmniJavaRuntime;
import com.gradleware.tooling.toolingmodel.OmniJavaSourceSettings;
import com.gradleware.tooling.toolingmodel.OmniJavaVersion;
import com.gradleware.tooling.toolingmodel.Path;

/**
 * Default implementation of the {@link OmniEclipseProject} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniEclipseProject implements OmniEclipseProject {

    private final HierarchyHelper<OmniEclipseProject> hierarchyHelper;
    private EclipseProjectIdentifier identifier;
    private String name;
    private String description;
    private Path path;
    private File projectDirectory;
    private ImmutableList<OmniEclipseProjectDependency> projectDependencies;
    private ImmutableList<OmniExternalDependency> externalDependencies;
    private ImmutableList<OmniEclipseLinkedResource> linkedResources;
    private ImmutableList<OmniEclipseSourceDirectory> sourceDirectories;
    private Optional<List<OmniEclipseProjectNature>> projectNatures;
    private Optional<List<OmniEclipseBuildCommand>> buildCommands;
    private Optional<OmniJavaSourceSettings> javaSourceSettings;
    private OmniGradleProject gradleProject;

    private DefaultOmniEclipseProject(Comparator<? super OmniEclipseProject> comparator) {
        this.hierarchyHelper = new HierarchyHelper<OmniEclipseProject>(this, Preconditions.checkNotNull(comparator));
    }

    @Override
    public EclipseProjectIdentifier getIdentifier() {
        return this.identifier;
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

    private void setDescription(String description) {
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
    public File getProjectDirectory() {
        return this.projectDirectory;
    }

    private void setProjectDirectory(File projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    @Override
    public ImmutableList<OmniEclipseProjectDependency> getProjectDependencies() {
        return this.projectDependencies;
    }

    private void setProjectDependencies(List<OmniEclipseProjectDependency> projectDependencies) {
        this.projectDependencies = ImmutableList.copyOf(projectDependencies);
    }

    @Override
    public ImmutableList<OmniExternalDependency> getExternalDependencies() {
        return this.externalDependencies;
    }

    private void setExternalDependencies(List<OmniExternalDependency> externalDependencies) {
        this.externalDependencies = ImmutableList.copyOf(externalDependencies);
    }

    @Override
    public ImmutableList<OmniEclipseLinkedResource> getLinkedResources() {
        return this.linkedResources;
    }

    private void setLinkedResources(List<OmniEclipseLinkedResource> linkedResources) {
        this.linkedResources = ImmutableList.copyOf(linkedResources);
    }

    @Override
    public ImmutableList<OmniEclipseSourceDirectory> getSourceDirectories() {
        return this.sourceDirectories;
    }

    private void setSourceDirectories(List<OmniEclipseSourceDirectory> sourceDirectories) {
        this.sourceDirectories = ImmutableList.copyOf(sourceDirectories);
    }

    @Override
    public Optional<List<OmniEclipseProjectNature>> getProjectNatures() {
        return this.projectNatures;
    }

    private void setProjectNatures(Optional<List<OmniEclipseProjectNature>> projectNatures) {
        if (projectNatures.isPresent()) {
            this.projectNatures = Optional.<List<OmniEclipseProjectNature>>of(ImmutableList.copyOf(projectNatures.get()));
        } else {
            this.projectNatures = Optional.absent();
        }
    }

    @Override
    public Optional<List<OmniEclipseBuildCommand>> getBuildCommands() {
        return this.buildCommands;
    }

    private void setBuildCommands(Optional<List<OmniEclipseBuildCommand>> buildCommands) {
        if (buildCommands.isPresent()) {
            this.buildCommands = Optional.<List<OmniEclipseBuildCommand>>of(ImmutableList.copyOf(buildCommands.get()));
        } else {
            this.buildCommands = Optional.absent();
        }
    }

    @Override
    public Optional<OmniJavaSourceSettings> getJavaSourceSettings() {
        return this.javaSourceSettings;
    }

    private void setJavaSourceSettings(Optional<OmniJavaSourceSettings> javaSourceSettings) {
        this.javaSourceSettings = javaSourceSettings;
    }

    @Override
    public OmniGradleProject getGradleProject() {
        return this.gradleProject;
    }

    private void setGradleProject(OmniGradleProject gradleProject) {
        this.gradleProject = gradleProject;
    }

    @Override
    public OmniEclipseProject getRoot() {
        return this.hierarchyHelper.getRoot();
    }

    @Override
    public OmniEclipseProject getParent() {
        return this.hierarchyHelper.getParent();
    }

    private void setParent(DefaultOmniEclipseProject parent) {
        this.hierarchyHelper.setParent(parent);
    }

    @Override
    public ImmutableList<OmniEclipseProject> getChildren() {
        return this.hierarchyHelper.getChildren();
    }

    private void addChild(DefaultOmniEclipseProject child) {
        child.setParent(this);
        this.hierarchyHelper.addChild(child);
    }

    @Override
    public ImmutableList<OmniEclipseProject> getAll() {
        return this.hierarchyHelper.getAll();
    }

    @Override
    public ImmutableList<OmniEclipseProject> filter(Spec<? super OmniEclipseProject> predicate) {
        return this.hierarchyHelper.filter(predicate);
    }

    @Override
    public Optional<OmniEclipseProject> tryFind(Spec<? super OmniEclipseProject> predicate) {
        return this.hierarchyHelper.tryFind(predicate);
    }

    public static DefaultOmniEclipseProject from(EclipseProject project) {
        return from(project, Maps.<Path, DefaultOmniEclipseProject>newHashMap());
    }

    private static DefaultOmniEclipseProject from(EclipseProject project, Map<Path, DefaultOmniEclipseProject> knownProjects) {
        Path path = Path.from(project.getGradleProject().getPath());
        if (knownProjects.containsKey(path)) {
            return knownProjects.get(path);
        }

        DefaultOmniEclipseProject eclipseProject = new DefaultOmniEclipseProject(OmniEclipseProjectComparator.INSTANCE);
        knownProjects.put(path, eclipseProject);

        eclipseProject.identifier = project.getIdentifier();
        eclipseProject.setName(project.getName());
        eclipseProject.setDescription(project.getDescription());
        eclipseProject.setPath(path);
        eclipseProject.setProjectDirectory(project.getProjectDirectory());
        eclipseProject.setProjectDependencies(toProjectDependencies(project.getProjectDependencies()));
        eclipseProject.setExternalDependencies(toExternalDependencies(project.getClasspath()));
        eclipseProject.setLinkedResources(toLinkedResources(project.getLinkedResources()));
        eclipseProject.setSourceDirectories(toSourceDirectories(project.getSourceDirectories()));
        eclipseProject.setGradleProject(DefaultOmniGradleProject.from(project.getGradleProject()));
        if (project.getParent() != null) {
            eclipseProject.setParent(from(project.getParent(), knownProjects));
        }
        setProjectNatures(eclipseProject, project);
        setBuildCommands(eclipseProject, project);
        setJavaSourceSettings(eclipseProject, project);

        for (EclipseProject child : project.getChildren()) {
            DefaultOmniEclipseProject eclipseChildProject = from(child, knownProjects);
            eclipseProject.addChild(eclipseChildProject);
        }

        return eclipseProject;
    }

    private static ImmutableList<OmniEclipseProjectDependency> toProjectDependencies(DomainObjectSet<? extends EclipseProjectDependency> projectDependencies) {
        return FluentIterable.from(projectDependencies).transform(new Function<EclipseProjectDependency, OmniEclipseProjectDependency>() {
            @Override
            public OmniEclipseProjectDependency apply(EclipseProjectDependency input) {
                return DefaultOmniEclipseProjectDependency.from(input);
            }
        }).toList();
    }

    private static ImmutableList<OmniExternalDependency> toExternalDependencies(DomainObjectSet<? extends EclipseExternalDependency> externalDependencies) {
        // filter out invalid external dependencies
        // Gradle versions <= 1.10 return external dependencies from dependent projects that are not valid, i.e. all fields are null except the file with name 'unresolved dependency...'
        return FluentIterable.from(externalDependencies).filter(new Predicate<EclipseExternalDependency>() {
            @Override
            public boolean apply(EclipseExternalDependency input) {
                return input.getFile().exists();
            }
        }).transform(new Function<EclipseExternalDependency, OmniExternalDependency>() {
            @Override
            public OmniExternalDependency apply(EclipseExternalDependency input) {
                return DefaultOmniExternalDependency.from(input);
            }
        }).toList();
    }

    private static ImmutableList<OmniEclipseLinkedResource> toLinkedResources(DomainObjectSet<? extends EclipseLinkedResource> linkedResources) {
        return FluentIterable.from(linkedResources).transform(new Function<EclipseLinkedResource, OmniEclipseLinkedResource>() {
            @Override
            public OmniEclipseLinkedResource apply(EclipseLinkedResource input) {
                return DefaultOmniEclipseLinkedResource.from(input);
            }
        }).toList();
    }

    private static ImmutableList<OmniEclipseSourceDirectory> toSourceDirectories(DomainObjectSet<? extends EclipseSourceDirectory> sourceDirectories) {
        return FluentIterable.from(sourceDirectories).transform(new Function<EclipseSourceDirectory, OmniEclipseSourceDirectory>() {
            @Override
            public OmniEclipseSourceDirectory apply(EclipseSourceDirectory input) {
                return DefaultOmniEclipseSourceDirectory.from(input);
            }
        }).toList();
    }

    /**
     * EclipseProject#getProjectNatures is only available in Gradle versions >= 2.9.
     *
     * @param eclipseProject the project to populate
     * @param project the project model
     */
    private static void setProjectNatures(DefaultOmniEclipseProject eclipseProject, EclipseProject project) {
        try {
            List<OmniEclipseProjectNature> projectNatures = toProjectNatures(project.getProjectNatures());
            eclipseProject.setProjectNatures(Optional.of(projectNatures));
        } catch (Exception ignore) {
            eclipseProject.setProjectNatures(Optional.<List<OmniEclipseProjectNature>>absent());
        }
    }

    private static ImmutableList<OmniEclipseProjectNature> toProjectNatures(DomainObjectSet<? extends EclipseProjectNature> projectNatures) {
        return FluentIterable.from(projectNatures).transform(new Function<EclipseProjectNature, OmniEclipseProjectNature>() {

            @Override
            public OmniEclipseProjectNature apply(EclipseProjectNature input) {
                return DefaultOmniEclipseProjectNature.from(input);
            }
        }).toList();
    }

    /**
     * EclipseProject#getBuildCommands is only available in Gradle versions >= 2.9.
     *
     * @param eclipseProject the project to populate
     * @param project the project model
     */
    private static void setBuildCommands(DefaultOmniEclipseProject eclipseProject, EclipseProject project) {
        try {
            List<OmniEclipseBuildCommand> buildCommands = toBuildCommands(project.getBuildCommands());
            eclipseProject.setBuildCommands(Optional.of(buildCommands));
        } catch (Exception ignore) {
            eclipseProject.setBuildCommands(Optional.<List<OmniEclipseBuildCommand>>absent());
        }
    }

    private static ImmutableList<OmniEclipseBuildCommand> toBuildCommands(DomainObjectSet<? extends EclipseBuildCommand> buildCommands) {
        return FluentIterable.from(buildCommands).transform(new Function<EclipseBuildCommand, OmniEclipseBuildCommand>() {
            @Override
            public OmniEclipseBuildCommand apply(EclipseBuildCommand input) {
                return DefaultOmniEclipseBuildCommand.from(input);
            }
        }).toList();
    }

    /**
     * EclipseProject#getJavaSourceSettings is only available in Gradle versions >= 2.10, JavaSourceSettings#getTargetBytecodeLevel is is only available in Gradle versions >= 2.11,
     * JavaSourceSettings#getTargetRuntime is is only available in Gradle versions >= 2.11.
     *
     * @param eclipseProject the project to populate
     * @param project the project model
     */
    private static void setJavaSourceSettings(DefaultOmniEclipseProject eclipseProject, EclipseProject project) {
        try {
            EclipseJavaSourceSettings sourceSettings = project.getJavaSourceSettings();
            Optional<OmniJavaSourceSettings> javaSourceSettings = sourceSettings != null ? Optional.of(toOmniJavaSourceSettings(sourceSettings)) : Optional.<OmniJavaSourceSettings>absent();
            eclipseProject.setJavaSourceSettings(javaSourceSettings);
        } catch (Exception ignore) {
            setCompatibilityJavaSourceSettings(eclipseProject);
        }
    }

    private static OmniJavaSourceSettings toOmniJavaSourceSettings(final EclipseJavaSourceSettings javaSourceSettings) {
        // the source language level is always present on the source settings
        OmniJavaVersion sourceLanguageLevel = toOmniJavaVersion(javaSourceSettings.getSourceLanguageLevel());

        OmniJavaVersion targetBytecodeLevel;
        try {
            targetBytecodeLevel = toOmniJavaVersion(javaSourceSettings.getTargetBytecodeVersion());
        } catch (Exception ignore) {
            // if the target bytecode level is not available, then fall back to the current source language level
            targetBytecodeLevel = sourceLanguageLevel;
        }

        OmniJavaRuntime targetRuntime;
        try {
            targetRuntime = toOmniJavaRuntime(javaSourceSettings.getJdk());
        } catch (Exception ignore) {
            // if the target runtime is not available, then fall back to the current JVM settings
            targetRuntime = getCompatibilityJavaRuntime();
        }

        return DefaultOmniJavaSourceSettings.from(sourceLanguageLevel, targetBytecodeLevel, targetRuntime);
    }

    private static void setCompatibilityJavaSourceSettings(DefaultOmniEclipseProject eclipseProject) {
        if (eclipseProject.getSourceDirectories().isEmpty()) {
            eclipseProject.setJavaSourceSettings(Optional.<OmniJavaSourceSettings>absent());
        } else {
            OmniJavaVersion languageLevel = getCompatibilityLanguageLevel();
            OmniJavaRuntime javaRuntime = getCompatibilityJavaRuntime();
            OmniJavaSourceSettings javaSourceSettings = DefaultOmniJavaSourceSettings.from(languageLevel, languageLevel, javaRuntime);
            eclipseProject.setJavaSourceSettings(Optional.of(javaSourceSettings));
        }
    }

    private static DefaultOmniJavaVersion getCompatibilityLanguageLevel() {
        return DefaultOmniJavaVersion.from(JavaVersion.current());
    }

    private static OmniJavaRuntime getCompatibilityJavaRuntime() {
        return DefaultOmniJavaRuntime.from(JavaVersion.current(), new File(System.getProperty("java.home")).getAbsoluteFile());
    }

    private static OmniJavaRuntime toOmniJavaRuntime(InstalledJdk jdk) {
        return DefaultOmniJavaRuntime.from(jdk.getJavaVersion(), jdk.getJavaHome());
    }

    private static OmniJavaVersion toOmniJavaVersion(JavaVersion javaVersion) {
        return DefaultOmniJavaVersion.from(javaVersion);
    }

    /**
     * Singleton comparator to compare {@code OmniEclipseProject} instances by their project path.
     */
    private enum OmniEclipseProjectComparator implements Comparator<OmniEclipseProject> {

        INSTANCE;

        @Override
        public int compare(OmniEclipseProject o1, OmniEclipseProject o2) {
            return o1.getPath().compareTo(o2.getPath());
        }

    }

}

