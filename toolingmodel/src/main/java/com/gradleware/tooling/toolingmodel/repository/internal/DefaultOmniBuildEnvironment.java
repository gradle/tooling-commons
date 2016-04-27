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

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniGradleEnvironment;
import com.gradleware.tooling.toolingmodel.OmniJavaEnvironment;

import org.gradle.tooling.model.BuildIdentifier;
import org.gradle.tooling.model.build.BuildEnvironment;

/**
 * Default implementation of the {@link OmniBuildEnvironment} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniBuildEnvironment implements OmniBuildEnvironment {

    private final OmniGradleEnvironment gradle;
    private final OmniJavaEnvironment java;
    private final BuildIdentifier buildIdentifier;

    private DefaultOmniBuildEnvironment(OmniGradleEnvironment gradle, OmniJavaEnvironment java, BuildIdentifier buildIdentifier) {
        this.gradle = gradle;
        this.java = java;
        this.buildIdentifier = buildIdentifier;
    }

    @Override
    public OmniGradleEnvironment getGradle() {
        return this.gradle;
    }

    @Override
    public OmniJavaEnvironment getJava() {
        return this.java;
    }

    @Override
    public BuildIdentifier getBuildIdentifier() {
        return this.buildIdentifier;
    }

    public static DefaultOmniBuildEnvironment from(BuildEnvironment buildEnvironment) {
        return new DefaultOmniBuildEnvironment(
                DefaultOmniGradleEnvironment.from(buildEnvironment.getGradle()),
                DefaultOmniJavaEnvironment.from(buildEnvironment.getJava()),
                buildEnvironment.getBuildIdentifier());
    }

}
