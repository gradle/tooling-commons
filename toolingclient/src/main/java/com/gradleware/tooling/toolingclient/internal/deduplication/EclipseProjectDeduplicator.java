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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.eclipse.EclipseProjectIdentifier;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * De-duplicates {@link EclipseProject} names.
 *
 * @author Stefan Oehme
 */
class EclipseProjectDeduplicator {

    public Set<EclipseProject> deduplicate(Set<EclipseProject> eclipseProjects) {
        RenamedEclipseProjectTracker renamedProjectTracker = new RenamedEclipseProjectTracker(eclipseProjects);
        deduplicate(eclipseProjects, renamedProjectTracker);
        return renamedProjectTracker.getRenamedProjects();
    }

    private void deduplicate(Set<EclipseProject> eclipseProjects, RenamedEclipseProjectTracker renamedElementTracker) {
        Set<String> rootProjectNames = Sets.newHashSet();
        for (EclipseProject eclipseProject : eclipseProjects) {
            String name = eclipseProject.getName();
            if (eclipseProject.getParent() == null && !rootProjectNames.add(name)) {
                throw new IllegalArgumentException(String
                        .format("Duplicate root project name '%s'. Duplicate root project names are currently not supported. This will change in future Gradle versions.", name));
            }
        }
        Map<EclipseProject, String> newNames = new HierarchicalElementDeduplicator<EclipseProject>(new EclipseProjectNameDeduplicationStrategy()).deduplicate(eclipseProjects);
        for (Entry<EclipseProject, String> nameChange : newNames.entrySet()) {
            renamedElementTracker.renameTo(nameChange.getKey(), nameChange.getValue());
        }
    }

    /**
     * Adapts {@link EclipseProject}s to the generic de-duplication algorithm.
     *
     * @author Stefan Oehme
     */
    private static class EclipseProjectNameDeduplicationStrategy implements NameDeduplicationAdapter<EclipseProject> {

        @Override
        public String getName(EclipseProject element) {
            return element.getName();
        }

        @Override
        public EclipseProject getParent(EclipseProject element) {
            return element.getParent();
        }
    }

    /**
     * Keeps track of renamed {@link EclipseProject}s, so that their hierarchy and dependencies stay
     * consistent under renaming.
     */
    private static class RenamedEclipseProjectTracker implements RedirectedProjectLookup {

        private final Map<EclipseProjectIdentifier, EclipseProject> originalToRenamed;

        public RenamedEclipseProjectTracker(Set<EclipseProject> originals) {
            this.originalToRenamed = Maps.newHashMap();
            for (EclipseProject eclipseProject : originals) {
                this.originalToRenamed.put(eclipseProject.getIdentifier(), new RedirectionAwareEclipseProject(eclipseProject, this));
            }
        }

        @Override
        public EclipseProject getRedirectedProject(EclipseProjectIdentifier id) {
            return this.originalToRenamed.get(id);
        }

        public void renameTo(EclipseProject original, String newName) {
            EclipseProjectIdentifier id = original.getIdentifier();
            if (!this.originalToRenamed.containsKey(id)) {
                throw new IllegalArgumentException("Project " + original.getName() + " was not one of the projects to be renamed.");
            } else {
                this.originalToRenamed.put(id, new RenamedEclipseProject(original, newName, this));
            }
        }

        public Set<EclipseProject> getRenamedProjects() {
            return ImmutableSet.copyOf(this.originalToRenamed.values());
        }
    }
}
