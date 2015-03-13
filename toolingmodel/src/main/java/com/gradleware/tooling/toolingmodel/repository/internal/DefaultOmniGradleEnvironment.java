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

import com.gradleware.tooling.toolingmodel.OmniGradleEnvironment;
import com.gradleware.tooling.toolingmodel.util.Maybe;
import org.gradle.tooling.model.build.GradleEnvironment;

import java.io.File;

/**
 * Default implementation of the {@link OmniGradleEnvironment} interface.
 */
public final class DefaultOmniGradleEnvironment implements OmniGradleEnvironment {

    private final Maybe<File> gradleUserHome;
    private final String gradleVersion;

    private DefaultOmniGradleEnvironment(Maybe<File> gradleUserHome, String gradleVersion) {
        this.gradleUserHome = gradleUserHome;
        this.gradleVersion = gradleVersion;
    }

    @Override
    public Maybe<File> getGradleUserHome() {
        return this.gradleUserHome;
    }

    @Override
    public String getGradleVersion() {
        return this.gradleVersion;
    }

    public static DefaultOmniGradleEnvironment from(GradleEnvironment gradleEnvironment) {
        return new DefaultOmniGradleEnvironment(getGradleUserHome(gradleEnvironment), gradleEnvironment.getGradleVersion());
    }

    /**
     * GradleEnvironment#getGradleUserHome is only available in Gradle versions >= 2.4.
     *
     * @param gradleEnvironment the Gradle environment model
     */
    private static Maybe<File> getGradleUserHome(GradleEnvironment gradleEnvironment) {
        try {
            File gradleUserHome = gradleEnvironment.getGradleUserHome();
            return Maybe.of(gradleUserHome);
        } catch (Exception ignore) {
            return Maybe.absent();
        }
    }

}
