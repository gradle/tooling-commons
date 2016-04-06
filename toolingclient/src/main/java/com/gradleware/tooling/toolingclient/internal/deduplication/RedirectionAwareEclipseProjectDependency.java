/*
 * Copyright 2016 the original author or authors.
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

package com.gradleware.tooling.toolingclient.internal.deduplication;

import org.gradle.tooling.model.eclipse.EclipseProjectDependency;
import org.gradle.tooling.model.eclipse.HierarchicalEclipseProject;

/**
 * Redirects the {@link #getTargetProject()} method to point to a renamed or dependency-substituted project.
 * @author Stefan Oehme
 */
class RedirectionAwareEclipseProjectDependency implements EclipseProjectDependency {

    private final EclipseProjectDependency delegate;
    private final RedirectedProjectLookup renamedProjectLookup;

    public RedirectionAwareEclipseProjectDependency(EclipseProjectDependency delegate, RedirectedProjectLookup renamedProjectLookup) {
        this.delegate = delegate;
        this.renamedProjectLookup = renamedProjectLookup;
    }

    @Override
    public HierarchicalEclipseProject getTargetProject() {
        HierarchicalEclipseProject target = this.delegate.getTargetProject();
        return this.renamedProjectLookup.getRedirectedProject(target.getProjectDirectory());
    }

    @Override
    public String getPath() {
        return this.delegate.getPath();
    }

    @Override
    public boolean isExported() {
        return this.delegate.isExported();
    }

    @Override
    public String toString() {
        return this.delegate.toString();
    }

}
