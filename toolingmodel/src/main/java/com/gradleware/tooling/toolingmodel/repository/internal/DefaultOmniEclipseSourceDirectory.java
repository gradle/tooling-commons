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

import com.gradleware.tooling.toolingmodel.OmniEclipseSourceDirectory;
import org.gradle.tooling.model.eclipse.EclipseSourceDirectory;

import java.io.File;

/**
 * Default implementation of the {@link OmniEclipseSourceDirectory} interface.
 */
public final class DefaultOmniEclipseSourceDirectory implements OmniEclipseSourceDirectory {

    private final File directory;
    private final String path;

    private DefaultOmniEclipseSourceDirectory(File directory, String path) {
        this.directory = directory;
        this.path = path;
    }

    @Override
    public File getDirectory() {
        return this.directory;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    public static DefaultOmniEclipseSourceDirectory from(EclipseSourceDirectory sourceDirectory) {
        return new DefaultOmniEclipseSourceDirectory(sourceDirectory.getDirectory(), sourceDirectory.getPath());
    }

}
