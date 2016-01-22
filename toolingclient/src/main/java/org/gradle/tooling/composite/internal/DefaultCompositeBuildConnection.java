/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.tooling.composite.internal;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import org.gradle.api.Transformer;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.composite.CompositeBuildConnection;
import org.gradle.tooling.composite.ModelResult;
import org.gradle.tooling.composite.ProjectIdentity;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.util.CollectionUtils;

import java.io.File;
import java.util.Set;

public class DefaultCompositeBuildConnection implements CompositeBuildConnection {
    private final Set<ProjectConnection> participants;

    public DefaultCompositeBuildConnection(Set<ProjectConnection> participants) {
        if (participants.isEmpty()) {
            throw new IllegalStateException("A composite build requires at least one participating project.");
        }

        this.participants = participants;
    }

    @Override
    public <T> Set<ModelResult<T>> getModels(Class<T> modelType) {
        if (!modelType.isInterface()) {
            throw new IllegalArgumentException(String.format("Cannot fetch a model of type '%s' as this type is not an interface.", modelType.getName()));
        }

        if (!modelType.equals(EclipseProject.class)) {
            throw new IllegalArgumentException(String.format("The only supported model for a Gradle composite is %s.class.", EclipseProject.class.getSimpleName()));
        }

        return toModelResults(getEclipseProjects());
    }

    @Override
    public void close() {
        Throwable failure = null;
        for (ProjectConnection projectConnection : participants) {
            try {
                projectConnection.close();
            } catch (Exception e) {
                if (failure == null) {
                    failure = e;
                }
            }
        }
        if (failure != null) {
            Throwables.propagate(failure);
        }
    }

    private <T> Set<ModelResult<T>> toModelResults(Set<EclipseProject> eclipseProjects) {
        return CollectionUtils.collect(eclipseProjects, new Transformer<ModelResult<T>, EclipseProject>() {
            @SuppressWarnings("unchecked")
            @Override
            public ModelResult<T> transform(EclipseProject eclipseProject) {
                ProjectIdentity projectIdentity = new DefaultProjectIdentity(eclipseProject.getProjectDirectory());
                return new DefaultModelResult<T>((T) eclipseProject, projectIdentity);
            }
        });
    }

    private Set<EclipseProject> getEclipseProjects() {
        Set<File> processedBuilds = Sets.newLinkedHashSet();
        Set<EclipseProject> eclipseProjects = Sets.newLinkedHashSet();

        for (ProjectConnection participant : this.participants) {
            EclipseProject rootProject = determineRootProject(participant.getModel(EclipseProject.class));

            // Only collect the root project once
            File rootProjectDirectory = rootProject.getProjectDirectory();
            if (processedBuilds.add(rootProjectDirectory)) {
                addWithChildren(rootProject, eclipseProjects);
            }
        }

        return eclipseProjects;
    }

    private EclipseProject determineRootProject(EclipseProject eclipseProject) {
        if (eclipseProject.getParent() == null) {
            return eclipseProject;
        }
        return determineRootProject(eclipseProject.getParent());
    }

    private void addWithChildren(EclipseProject project, Set<EclipseProject> collectedProjects) {
        collectedProjects.add(project);

        for (EclipseProject childProject : project.getChildren()) {
            addWithChildren(childProject, collectedProjects);
        }
    }

    private static final class DefaultModelResult<T> implements ModelResult<T> {
        private final T model;
        private final ProjectIdentity projectIdentity;

        private DefaultModelResult(T model, ProjectIdentity projectIdentity) {
            this.model = model;
            this.projectIdentity = projectIdentity;
        }

        @Override
        public T getModel() {
            return model;
        }

        @Override
        public ProjectIdentity getProject() {
            return projectIdentity;
        }
    }
}