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

import org.gradle.tooling.model.eclipse.EclipseProject;

/**
 * An {@link EclipseProject} that has been assigned a new name.
 * @author Stefan Oehme
 */
class RenamedEclipseProject extends RedirectionAwareEclipseProject {

    private final String newName;

    public RenamedEclipseProject(EclipseProject original, String newName, RedirectedProjectLookup redirectedProjectLookup) {
        super(original,redirectedProjectLookup);
        this.newName = newName;
    }

    @Override
    public String getName() {
        return this.newName;
    }

    @Override
    public String toString() {
        return String.format("%s renamed to '%s'", super.toString(), getName());
    }
}
