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
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import org.gradle.tooling.model.eclipse.EclipseProject;

/**
 * Default implementation of the {@link OmniEclipseGradleBuild} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniEclipseGradleBuild implements OmniEclipseGradleBuild {

    private final OmniEclipseProject rootEclipseProject;
    private final OmniGradleProject rootProject;

    private DefaultOmniEclipseGradleBuild(OmniEclipseProject rootEclipseProject, OmniGradleProject rootProject) {
        this.rootEclipseProject = rootEclipseProject;
        this.rootProject = rootProject;
    }

    @Override
    public OmniEclipseProject getRootEclipseProject() {
        return this.rootEclipseProject;
    }

    @Override
    public OmniGradleProject getRootProject() {
        return this.rootProject;
    }

    public static DefaultOmniEclipseGradleBuild from(EclipseProject eclipseRootProject, boolean enforceAllTasksPublic, boolean buildCommandsAndNaturesAvailable) {
        Preconditions.checkState(eclipseRootProject.getParent() == null, "Provided Eclipse project is not the root project.");
        return new DefaultOmniEclipseGradleBuild(
                DefaultOmniEclipseProject.from(eclipseRootProject, buildCommandsAndNaturesAvailable),
                DefaultOmniGradleProject.from(eclipseRootProject.getGradleProject(), enforceAllTasksPublic));
    }

}
