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

import com.gradleware.tooling.toolingmodel.OmniEclipseProjectDependency;
import com.gradleware.tooling.toolingmodel.Path;
import org.gradle.internal.reflect.JavaReflectionUtil;
import org.gradle.internal.reflect.PropertyAccessor;
import org.gradle.tooling.internal.adapter.ProtocolToModelAdapter;
import org.gradle.tooling.model.eclipse.EclipseProjectDependency;

/**
 * Default implementation of the {@link OmniEclipseProjectDependency} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniEclipseProjectDependency implements OmniEclipseProjectDependency {

    private final Path targetProjectPath;
    private final String path;
    private final boolean exported;

    private DefaultOmniEclipseProjectDependency(Path targetProjectPath, String path, boolean exported) {
        this.targetProjectPath = targetProjectPath;
        this.path = path;
        this.exported = exported;
    }

    @Override
    public Path getTargetProjectPath() {
        return this.targetProjectPath;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public boolean isExported() {
        return this.exported;
    }

    public static DefaultOmniEclipseProjectDependency from(EclipseProjectDependency projectDependency) {
        String targetEclipseProjectPath = inspectTargetEclipseProjectPath(projectDependency);
        return new DefaultOmniEclipseProjectDependency(
                Path.from(targetEclipseProjectPath),
                projectDependency.getPath(),
                getIsExported(projectDependency));
    }

    @SuppressWarnings("unchecked")
    private static String inspectTargetEclipseProjectPath(EclipseProjectDependency projectDependency) {
        // cannot cast due to class in proxy delegate is loaded by a different class loader
        Object targetEclipseProject = new ProtocolToModelAdapter().unpack(projectDependency.getTargetProject());
        PropertyAccessor<Object, String> pathProperty = (PropertyAccessor<Object, String>) JavaReflectionUtil.readableProperty(targetEclipseProject.getClass(), String.class, "path");
        return pathProperty.getValue(targetEclipseProject);
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
