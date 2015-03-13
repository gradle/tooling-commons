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

import java.io.File;
import java.util.List;

/**
 * Provides information about the Java environment.
 */
public interface OmniJavaEnvironment {

    /**
     * Returns the Java home used for Gradle operations (for example running tasks or acquiring model information).
     *
     * @return the Java home location
     */
    File getJavaHome();

    /**
     * Returns the JVM arguments used to start the Java process that handles Gradle operations (for example running tasks or acquiring model information). The returned arguments do
     * not include system properties passed as -Dfoo=bar. They may include implicitly immutable system properties like "file.encoding".
     *
     * @return the JVM arguments
     */
    List<String> getJvmArguments();

}
