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

package org.gradle.tooling.composite.internal.deduplication;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gradle.tooling.composite.internal.RedirectedProjectLookup;
import org.gradle.tooling.composite.internal.RedirectionAwareEclipseProject;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.eclipse.HierarchicalEclipseProject;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;

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
        List<RenamableElement> renamableProjects = Lists.newArrayList();
        for (EclipseProject eclipseProject : eclipseProjects) {
            renamableProjects.add(new RenambleEclipseProject(eclipseProject, renamedElementTracker));
        }
        sortByDepth(renamableProjects);
        new HierarchicalElementDeduplicator().deduplicate(renamableProjects);
    }

    private void sortByDepth(List<RenamableElement> renamableProjects) {
        Collections.sort(renamableProjects, new Comparator<RenamableElement>() {

            @Override
            public int compare(RenamableElement o1, RenamableElement o2) {
                return Ints.compare(depth((EclipseProject) o1.getOriginal()), depth((EclipseProject) o2.getOriginal()));
            }

            private int depth(EclipseProject p) {
                if (p.getParent() == null) {
                    return 0;
                }
                return CharMatcher.is(':').countIn(p.getGradleProject().getPath());
            }
        });
    }

    /**
     * Adapts {@link EclipseProject}s to the generic de-duplication infrastructure.
     */
    private static class RenambleEclipseProject extends RenamableElement {

        private final RenamedEclipseProjectTracker renamedElementTracker;

        public RenambleEclipseProject(EclipseProject project, RenamedEclipseProjectTracker renamedElementTracker) {
            super(project);
            this.renamedElementTracker = renamedElementTracker;
        }

        @Override
        public void handleRename(String newName) {
            this.renamedElementTracker.renameTo((EclipseProject) getOriginal(), newName);
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
