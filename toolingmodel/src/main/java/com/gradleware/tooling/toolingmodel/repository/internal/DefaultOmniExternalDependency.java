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

import java.io.File;
import java.util.List;

import com.google.common.base.Optional;

import com.gradleware.tooling.toolingmodel.OmniAccessRule;
import com.gradleware.tooling.toolingmodel.repository.internal.compatibility.ForwardCompatibilityClasspathEntry;
import org.gradle.tooling.model.ExternalDependency;
import org.gradle.tooling.model.GradleModuleVersion;
import org.gradle.tooling.model.eclipse.EclipseExternalDependency;

import com.gradleware.tooling.toolingmodel.OmniClasspathAttribute;
import com.gradleware.tooling.toolingmodel.OmniExternalDependency;
import com.gradleware.tooling.toolingmodel.OmniGradleModuleVersion;
import com.gradleware.tooling.toolingmodel.util.Maybe;

/**
 * Default implementation of the {@link OmniExternalDependency} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniExternalDependency extends AbstractOmniClasspathEntry implements OmniExternalDependency {

    private final File file;
    private final File source;
    private final File javadoc;
    private final Maybe<OmniGradleModuleVersion> gradleModuleVersion;
    private final boolean exported;

    private DefaultOmniExternalDependency(File file, File source, File javadoc, Maybe<OmniGradleModuleVersion> gradleModuleVersion, boolean exported, Optional<List<OmniClasspathAttribute>> attributes, Optional<List<OmniAccessRule>> accessRules) {
        super(attributes, accessRules);
        this.file = file;
        this.source = source;
        this.javadoc = javadoc;
        this.gradleModuleVersion = gradleModuleVersion;
        this.exported = exported;
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public File getSource() {
        return this.source;
    }

    @Override
    public File getJavadoc() {
        return this.javadoc;
    }

    @Override
    public Maybe<OmniGradleModuleVersion> getGradleModuleVersion() {
        return this.gradleModuleVersion;
    }

    @Override
    public boolean isExported() {
        return this.exported;
    }

    public static DefaultOmniExternalDependency from(EclipseExternalDependency externalDependency) {
        ForwardCompatibilityClasspathEntry compatibilityDependency = ForwardCompatibilityConverter.convert(externalDependency, ForwardCompatibilityClasspathEntry.class);
        return new DefaultOmniExternalDependency(
                externalDependency.getFile(),
                externalDependency.getSource(),
                externalDependency.getJavadoc(),
                getGradleModuleVersion(externalDependency),
                getIsExported(externalDependency),
                getClasspathAttributes(externalDependency),
                getAccessRules(compatibilityDependency));
    }

    /**
     * ExternalDependency#getGradleModuleVersion is only available in Gradle versions >= 1.1.
     *
     * @param externalDependency the external dependency model
     */
    private static Maybe<OmniGradleModuleVersion> getGradleModuleVersion(ExternalDependency externalDependency) {
        try {
            GradleModuleVersion gav = externalDependency.getGradleModuleVersion();
            return Maybe.of((OmniGradleModuleVersion) DefaultOmniGradleModuleVersion.from(gav));
        } catch (Exception ignore) {
            return Maybe.absent();
        }
    }

    /**
     * ExternalDependency#isExported is only available in Gradle versions >= 2.5.
     *
     * @param externalDependency the external dependency model
     */
    private static boolean getIsExported(ExternalDependency externalDependency) {
        try {
            return externalDependency.isExported();
        } catch (Exception ignore) {
            return true;
        }
    }

}
