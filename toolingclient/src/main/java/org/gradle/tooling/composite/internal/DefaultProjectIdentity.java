/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.tooling.composite.internal;

import org.gradle.tooling.composite.ProjectIdentity;

import java.io.File;
import java.util.Set;

/**
 * TODO add javadoc.
 * 
 * @author Benjamin Muschko
 */
public class DefaultProjectIdentity implements ProjectIdentity {
    private final File projectDirectory;
    private final Set<ProjectIdentity> children;
    private ProjectIdentity parent;

    public DefaultProjectIdentity(File projectDirectory, Set<ProjectIdentity> children) {
        assert projectDirectory != null : "project directory cannot be null";
        assert children != null : "children cannot be null";
        this.projectDirectory = projectDirectory;
        this.children = children;
    }

    public File getProjectDirectory() {
        return projectDirectory;
    }

    @Override
    public ProjectIdentity getParent() {
        return parent;
    }

    @Override
    public Set<ProjectIdentity> getChildren() {
        return children;
    }

    public void setParent(ProjectIdentity parent) {
        this.parent = parent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultProjectIdentity that = (DefaultProjectIdentity) o;

        return projectDirectory.equals(that.projectDirectory);

    }

    @Override
    public int hashCode() {
        return projectDirectory.hashCode();
    }

    @Override
    public String toString() {
        return String.format("project dir %s", projectDirectory);
    }
}
