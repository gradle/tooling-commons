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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.eclipse.HierarchicalEclipseProject;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * De-duplicates {@link EclipseProject} names.
 * @author Stefan Oehme
 */
class EclipseProjectDeduplicator {

    public Set<EclipseProject> deduplicate(Set<EclipseProject> eclipseProjects) {
        RenamedEclipseProjectTracker renamedProjectTracker = new RenamedEclipseProjectTracker(eclipseProjects);
        deduplicate(eclipseProjects, renamedProjectTracker);
        return renamedProjectTracker.getRenamedProjects();
    }

    private void deduplicate(Set<EclipseProject> eclipseProjects, RenamedEclipseProjectTracker renamedElementTracker) {
        List<EclipseProject> projectsSortedByDir = Lists.newArrayList(eclipseProjects);
        Collections.sort(projectsSortedByDir, new Comparator<EclipseProject>() {
            @Override
            public int compare(EclipseProject left, EclipseProject right) {
                return left.getProjectDirectory().compareTo(right.getProjectDirectory());
            }
        });
        Map<EclipseProject, String> newNames = new HierarchicalElementDeduplicator<EclipseProject>(new EclipseProjectNameDeduplicationStrategy()).deduplicate(projectsSortedByDir);
        for (Entry<EclipseProject, String> nameChange : newNames.entrySet()) {
            renamedElementTracker.renameTo(nameChange.getKey(), nameChange.getValue());
        }
    }

    /**
     * Adapts {@link EclipseProject}s to the generic de-duplication algorithm.
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
     * Keeps track of renamed {@link EclipseProject}s, so that their hierarchy and dependencies stay consistent under renaming.
     */
    private static class RenamedEclipseProjectTracker implements RedirectedProjectLookup {

        private final Map<HierarchicalEclipseProject, EclipseProject> originalToRenamed;

        public RenamedEclipseProjectTracker(Set<EclipseProject> originals) {
            this.originalToRenamed = Maps.newHashMap();
            for (EclipseProject eclipseProject : originals) {
                this.originalToRenamed.put(eclipseProject, new RedirectionAwareEclipseProject(eclipseProject, this));
            }
        }

        @Override
        public EclipseProject getRedirectedProject(HierarchicalEclipseProject original) {
            return this.originalToRenamed.get(original);
        }

        public void renameTo(EclipseProject original, String newName) {
            if (!this.originalToRenamed.containsKey(original)) {
                throw new IllegalArgumentException("Project " + original.getName() + " was not one of the projects to be renamed.");
            } else {
                this.originalToRenamed.put(original, new RenamedEclipseProject(original, newName, this));
            }
        }

        public Set<EclipseProject> getRenamedProjects() {
            return ImmutableSet.copyOf(this.originalToRenamed.values());
        }
    }
}
