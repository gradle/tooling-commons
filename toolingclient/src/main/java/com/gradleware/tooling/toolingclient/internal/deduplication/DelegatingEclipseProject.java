/*
 * Copyright 2016 the original author or authors.
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

package com.gradleware.tooling.toolingclient.internal.deduplication;

import java.io.File;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.UnsupportedMethodException;
import org.gradle.tooling.model.eclipse.EclipseBuildCommand;
import org.gradle.tooling.model.eclipse.EclipseExternalDependency;
import org.gradle.tooling.model.eclipse.EclipseJavaSourceSettings;
import org.gradle.tooling.model.eclipse.EclipseLinkedResource;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.eclipse.EclipseProjectDependency;
import org.gradle.tooling.model.eclipse.EclipseProjectIdentifier;
import org.gradle.tooling.model.eclipse.EclipseProjectNature;
import org.gradle.tooling.model.eclipse.EclipseSourceDirectory;

/**
 * @author Stefan Oehme
 *
 */
public class DelegatingEclipseProject implements EclipseProject {

    private final EclipseProject delegate;

    public DelegatingEclipseProject(EclipseProject delegate) {
        this.delegate = delegate;
    }

    @Override
    public EclipseProjectIdentifier getIdentifier() {
        return this.delegate.getIdentifier();
    }

    @Override
    public DomainObjectSet<? extends EclipseProjectDependency> getProjectDependencies() {
        return this.delegate.getProjectDependencies();
    }

    @Override
    public DomainObjectSet<? extends EclipseSourceDirectory> getSourceDirectories() {
        return this.delegate.getSourceDirectories();
    }

    @Override
    public DomainObjectSet<? extends EclipseLinkedResource> getLinkedResources() {
        return this.delegate.getLinkedResources();
    }

    @Override
    public File getProjectDirectory() throws UnsupportedMethodException {
        return this.delegate.getProjectDirectory();
    }

    @Override
    public String getName() {
        return this.delegate.getName();
    }

    @Override
    public String getDescription() {
        return this.delegate.getDescription();
    }

    @Override
    public EclipseProject getParent() {
        return this.delegate.getParent();
    }

    @Override
    public DomainObjectSet<? extends EclipseProject> getChildren() {
        return this.delegate.getChildren();
    }

    @Override
    public EclipseJavaSourceSettings getJavaSourceSettings() throws UnsupportedMethodException {
        return this.delegate.getJavaSourceSettings();
    }

    @Override
    public GradleProject getGradleProject() {
        return this.delegate.getGradleProject();
    }

    @Override
    public DomainObjectSet<? extends EclipseExternalDependency> getClasspath() {
        return this.delegate.getClasspath();
    }

    @Override
    public DomainObjectSet<? extends EclipseProjectNature> getProjectNatures() throws UnsupportedMethodException {
        return this.delegate.getProjectNatures();
    }

    @Override
    public DomainObjectSet<? extends EclipseBuildCommand> getBuildCommands() throws UnsupportedMethodException {
        return this.delegate.getBuildCommands();
    }

    @Override
    public String toString() {
        return this.delegate.toString();
    }

    public EclipseProject getDelegate() {
        return this.delegate;
    }

}
