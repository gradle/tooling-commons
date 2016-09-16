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

/**
 * Request to be issued on a single Gradle build.
 *
 * @author Stefan Oehme
 *
 * @param <T> the result type
 */
public interface BuildRequest<T> extends Request<T> {

    /**
     * Specifies the working directory to use.
     *
     * @param projectDir the working directory
     * @return this
     */
    Request<T> projectDir(File projectDir);

    /**
     * Specifies the user's Gradle home directory to use. Defaults to {@code ~/.gradle}.
     *
     * @param gradleUserHomeDir the user's Gradle home directory to use
     * @return this
     */
    Request<T> gradleUserHomeDir(File gradleUserHomeDir);

    /**
     * Specifies the Gradle distribution to use. Defaults to a project-specific Gradle version.
     *
     * @param gradleDistribution the Gradle distribution to use
     * @return this
     */
    Request<T> gradleDistribution(GradleDistribution gradleDistribution);

}
