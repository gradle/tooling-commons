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

import com.gradleware.tooling.toolingmodel.OmniAccessRule;
import com.gradleware.tooling.toolingmodel.OmniClasspathAttribute;
import com.gradleware.tooling.toolingmodel.OmniEclipseSourceDirectory;
import org.gradle.tooling.model.eclipse.EclipseSourceDirectory;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of the {@link OmniEclipseSourceDirectory} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniEclipseSourceDirectory extends AbstractOmniClasspathEntry implements OmniEclipseSourceDirectory {

    private final File directory;
    private final String path;
    private final List<String> excludes;
    private final List<String> includes;
    private final String output;

    private DefaultOmniEclipseSourceDirectory(File directory, String path, List<String> excludes, List<String> includes, String output, List<OmniClasspathAttribute> attributes, List<OmniAccessRule> accessRules) {
        super(attributes, accessRules);
        this.directory = directory;
        this.path = path;
        this.excludes = excludes;
        this.includes = includes;
        this.output = output;
    }

    @Override
    public File getDirectory() {
        return this.directory;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public List<String> getExcludes() {
        return this.excludes;
    }

    @Override
    public List<String> getIncludes() {
        return this.includes;
    }

    @Override
    public String getOutput() {
        return this.output;
    }

    public static DefaultOmniEclipseSourceDirectory from(EclipseSourceDirectory sourceDirectory) {  

        return new DefaultOmniEclipseSourceDirectory(
                sourceDirectory.getDirectory(),
                sourceDirectory.getPath(),
                getExcludes(sourceDirectory),
                getIncludes(sourceDirectory),
                getOutput(sourceDirectory),
                getClasspathAttributes(sourceDirectory),
                getAccessRules(sourceDirectory));
    }

    private static List<String> getExcludes(EclipseSourceDirectory sourceDirectory) {
        List<String> excludes;
        try {
            excludes = sourceDirectory.getExcludes();
        } catch(Exception ignore) {
            excludes = Collections.emptyList();
        }
        return excludes;
    }

    private static List<String> getIncludes(EclipseSourceDirectory sourceDirectory) {
        List<String> includes;
        try {
            includes = sourceDirectory.getIncludes();
        } catch(Exception ignore) {
            includes = Collections.emptyList();
        }
        return includes;
    }

    private static String getOutput(EclipseSourceDirectory sourceDirectory) {
        try {
            return sourceDirectory.getOutput();
        } catch (Exception ignore) {
            return null;
        }
    }

}
