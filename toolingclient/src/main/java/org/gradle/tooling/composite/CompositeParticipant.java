/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.tooling.composite;

import java.io.File;

/**
 * Represents a participating build for a composite.
 * <p>
 * A participating build can consist of a single or multiple Gradle projects.
 *
 * @author Benjamin Muschko
 */
public interface CompositeParticipant extends GradleDistributionAware {

    /**
     * Returns the root project directory of a participating Gradle build.
     *
     * @return the root project directory
     */
    File getRootProjectDirectory();
}
