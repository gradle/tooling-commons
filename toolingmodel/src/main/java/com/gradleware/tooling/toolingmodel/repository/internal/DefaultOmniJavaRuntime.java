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

import com.gradleware.tooling.toolingmodel.OmniJavaRuntime;
import com.gradleware.tooling.toolingmodel.OmniJavaVersion;

import java.io.File;

/**
 * Default implementation of the {@link OmniJavaRuntime} interface.
 *
 * @author Donát Csikós
 */
public final class DefaultOmniJavaRuntime implements OmniJavaRuntime {

    private final OmniJavaVersion javaVersion;
    private final File homeDirectory;

    private DefaultOmniJavaRuntime(OmniJavaVersion javaVersion, File homeDirectory) {
        this.javaVersion = javaVersion;
        this.homeDirectory = homeDirectory;
    }

    @Override
    public OmniJavaVersion getJavaVersion() {
        return this.javaVersion;
    }

    @Override
    public File getHomeDirectory() {
        return this.homeDirectory;
    }

    public static DefaultOmniJavaRuntime from(OmniJavaVersion javaVersion, File homeDirectory) {
        return new DefaultOmniJavaRuntime(javaVersion, homeDirectory);
    }

}
