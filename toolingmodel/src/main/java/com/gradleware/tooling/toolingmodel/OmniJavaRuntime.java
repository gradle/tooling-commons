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
 * Describes the Java Runtime for a Java project.
 *
 * @author Donát Csikós
 */
public interface OmniJavaRuntime {

    /**
     * Returns the Java language level supported by the current runtime.
     *
     * @return the supported Java language level
     */
    OmniJavaVersion getJavaVersion();

    /**
     * Returns the Java Runtime installation directory.
     *
     * @return the installation directory
     */
    File getHomeDirectory();
}
