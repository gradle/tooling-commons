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

import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure;
import com.gradleware.tooling.toolingmodel.OmniGradleProjectStructure;
import org.gradle.tooling.model.gradle.GradleBuild;

/**
 * Default implementation of the {@link OmniGradleBuildStructure} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniGradleBuildStructure implements OmniGradleBuildStructure {

    private final OmniGradleProjectStructure rootProject;

    private DefaultOmniGradleBuildStructure(OmniGradleProjectStructure rootProject) {
        this.rootProject = rootProject;
    }

    @Override
    public OmniGradleProjectStructure getRootProject() {
        return this.rootProject;
    }

    public static DefaultOmniGradleBuildStructure from(GradleBuild gradleBuild) {
        return new DefaultOmniGradleBuildStructure(DefaultOmniGradleProjectStructure.from(gradleBuild.getRootProject()));
    }

}
