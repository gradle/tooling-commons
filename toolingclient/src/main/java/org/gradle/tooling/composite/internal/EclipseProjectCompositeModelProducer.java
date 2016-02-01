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

import java.io.File;
import java.util.Set;

import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.collect.Sets;

/**
 * Produces a model for a Eclipse composite.
 *
 * @author Benjamin Muschko
 */
public class EclipseProjectCompositeModelProducer implements CompositeModelProducer<EclipseProject> {

    private final Set<ProjectConnection> connections;

    public EclipseProjectCompositeModelProducer(Set<ProjectConnection> connections) {
        this.connections = connections;
    }

    @Override
    public Set<EclipseProject> getModel() {
        Set<File> processedBuilds = Sets.newHashSet();
        Set<EclipseProject> eclipseProjects = Sets.newHashSet();

        for (ProjectConnection participant : this.connections) {
            EclipseProject rootProject = determineRootProject(participant.getModel(EclipseProject.class));

            // Only collect the root project once
            File rootProjectDirectory = rootProject.getProjectDirectory();
            if (processedBuilds.add(rootProjectDirectory)) {
                addWithChildren(rootProject, eclipseProjects);
            }
        }

        return Sets.newHashSet(eclipseProjects);
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
}
