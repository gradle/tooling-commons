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

import com.google.common.base.Optional;
import com.gradleware.tooling.toolingmodel.OmniAccessRule;
import com.gradleware.tooling.toolingmodel.OmniClasspathAttribute;
import com.gradleware.tooling.toolingmodel.OmniEclipseProjectDependency;
import com.gradleware.tooling.toolingmodel.repository.internal.compatibility.ForwardCompatibilityClasspathEntry;
import org.gradle.tooling.model.eclipse.EclipseProjectDependency;

import java.io.File;
import java.util.List;

/**
 * Default implementation of the {@link OmniEclipseProjectDependency} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniEclipseProjectDependency extends AbstractOmniClasspathEntry implements OmniEclipseProjectDependency {

    private final File targetProjectDir;
    private final String path;
    private final boolean exported;

    private DefaultOmniEclipseProjectDependency(File targetProjectDir, String path, boolean exported, Optional<List<OmniClasspathAttribute>> attributes, Optional<List<OmniAccessRule>> accessRules) {
        super(attributes, accessRules);
        this.targetProjectDir = targetProjectDir;
        this.path = path;
        this.exported = exported;
    }

    @Override
    public File getTargetProjectDir() {
        return this.targetProjectDir;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public boolean isExported() {
        return this.exported;
    }

    @SuppressWarnings("deprecation")
    public static DefaultOmniEclipseProjectDependency from(EclipseProjectDependency projectDependency) {
        ForwardCompatibilityClasspathEntry compatibilityDependency = ForwardCompatibilityConverter.convert(projectDependency, ForwardCompatibilityClasspathEntry.class);
        return new DefaultOmniEclipseProjectDependency(
                projectDependency.getTargetProject().getProjectDirectory(),
                projectDependency.getPath(),
                getIsExported(projectDependency),
                getClasspathAttributes(projectDependency),
                getAccessRules(compatibilityDependency));
    }

    /**
     * EclipseProjectDependency#isExported is only available in Gradle versions >= 2.5.
     *
     * @param projectDependency the project dependency model
     */
    private static boolean getIsExported(EclipseProjectDependency projectDependency) {
        try {
            return projectDependency.isExported();
        } catch (Exception ignore) {
            return true;
        }
    }

}
