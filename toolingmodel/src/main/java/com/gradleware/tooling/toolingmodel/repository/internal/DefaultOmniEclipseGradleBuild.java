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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import org.gradle.tooling.model.eclipse.EclipseProject;

import java.util.Collections;
import java.util.List;

/**
 * Default implementation of the {@link OmniEclipseGradleBuild} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniEclipseGradleBuild implements OmniEclipseGradleBuild {

    private final OmniEclipseProject rootEclipseProject;

    private final ImmutableList<OmniEclipseProject> includedRootEclipseProjects;

    private DefaultOmniEclipseGradleBuild(OmniEclipseProject rootEclipseProject, List<OmniEclipseProject> includedRootEclipseProjects) {
        this.rootEclipseProject = rootEclipseProject;
        this.includedRootEclipseProjects = ImmutableList.copyOf(includedRootEclipseProjects);
    }

    @Override
    public OmniEclipseProject getRootEclipseProject() {
        return this.rootEclipseProject;
    }

    @Override
    public List<OmniEclipseProject> getIncludedRootProjects() {
        return this.includedRootEclipseProjects;
    }

    public static DefaultOmniEclipseGradleBuild from(EclipseProject eclipseRootProject) {
        Preconditions.checkState(eclipseRootProject.getParent() == null, "Provided Eclipse project is not the root project.");
        return new DefaultOmniEclipseGradleBuild(DefaultOmniEclipseProject.from(eclipseRootProject), Collections.<OmniEclipseProject>emptyList());
    }

    public static DefaultOmniEclipseGradleBuild from(EclipseProject eclipseRootProject, List<EclipseProject> includedRootProjects) {
        Preconditions.checkState(eclipseRootProject.getParent() == null, "Provided Eclipse project is not the root project.");
        DefaultOmniEclipseProject root = DefaultOmniEclipseProject.from(eclipseRootProject);
        List<OmniEclipseProject> included = Lists.newArrayList();
        for (EclipseProject includedRootProject : includedRootProjects) {
            Preconditions.checkState(includedRootProject.getParent() == null, "Included Gradle project is not a root project.");
            included.add(DefaultOmniEclipseProject.from(includedRootProject));
        }
        return new DefaultOmniEclipseGradleBuild(root, included);
    }

}
