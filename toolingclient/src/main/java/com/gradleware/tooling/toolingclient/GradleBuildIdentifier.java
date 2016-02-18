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

package com.gradleware.tooling.toolingclient;

import java.io.File;

import com.google.common.base.Preconditions;

/**
 * Encapsulates the information required to identify a Gradle build in a composite build.
 *
 * @author Stefan Oehme
 */
public final class GradleBuildIdentifier {

    private final File projectDir;
    private final GradleDistribution gradleDistribution;

    public GradleBuildIdentifier(File projectDir, GradleDistribution gradleDistribution) {
        this.projectDir = Preconditions.checkNotNull(projectDir);
        this.gradleDistribution = Preconditions.checkNotNull(gradleDistribution);
    }

    public File getProjectDir() {
        return this.projectDir;
    }

    public GradleDistribution getGradleDistribution() {
        return this.gradleDistribution;
    }
}
