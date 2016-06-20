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

package com.gradleware.tooling.toolingmodel;

/**
 * A classpath container associated to an Eclipse project.
 *
 * @author Donat Csikos
 */
public interface OmniEclipseClasspathContainer extends OmniClasspathEntry {

    /**
     * Returns the path of the classpath container.
     *
     * @return the classpath container path, never null
     */
    String getPath();
}
