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

import com.google.common.base.Optional;

import java.io.File;

/**
 * Describes a dependency on another Eclipse project.
 *
 * @author Etienne Studer
 */
public interface OmniEclipseProjectDependency extends OmniClasspathEntry {

    /**
     * Returns the directory of the target project of this dependency.
     * <p>
     * This method returns {@link Optional#absent()} if the dependency points to project from a different build in the
     * composite.
     *
     * @return the directory of the target project of this dependency
     */
    Optional<File> getTargetProjectDir();

    /**
     * Returns the path to use for this project dependency.
     *
     * @return the path to use for this project dependency
     */
    String getPath();

    /**
     * Returns whether this project dependency should be exported.
     *
     * @return {@code true} if this project dependency should be exported
     */
    boolean isExported();

}
