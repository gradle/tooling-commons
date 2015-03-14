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

package com.gradleware.tooling.toolingmodel;

import com.gradleware.tooling.toolingmodel.util.Maybe;

import java.io.File;

/**
 * Describes an external artifact dependency.
 *
 * @author Etienne Studer
 */
public interface OmniExternalDependency {

    /**
     * Returns the file for this dependency.
     *
     * @return the file for this dependency
     */
    File getFile();

    /**
     * Returns the source directory or archive for this dependency, or {@code null} if no source is available.
     *
     * @return the source directory or archive for this dependency, or {@code null} if no source is available
     */
    File getSource();

    /**
     * Returns the Javadoc directory or archive for this dependency, or {@code null} if no Javadoc is available.
     *
     * @return the Javadoc directory or archive for this dependency, or {@code null} if no Javadoc is available
     */
    File getJavadoc();

    /**
     * Returns the Gradle module information for this dependency, or {@code null} if the dependency does not originate from a remote repository.
     *
     * @return the Gradle module information for this dependency, or {@code null} if the dependency does not originate from a remote repository
     */
    Maybe<OmniGradleModuleVersion> getGradleModuleVersion();

}
