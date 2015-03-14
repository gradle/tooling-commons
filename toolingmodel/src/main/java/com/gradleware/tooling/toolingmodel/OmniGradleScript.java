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

/**
 * Represents a Gradle script. A Gradle script may be a build script, a settings script, or an initialization script.
 *
 * @author Etienne Studer
 */
public interface OmniGradleScript {

    /**
     * Returns the source file for this script, or {@code null} if this script has no associated source file. If this method returns a non-null value, the given source file will
     * exist.
     *
     * @return the source file, null if the script has no associated source file
     */
    File getSourceFile();

}
