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

package org.gradle.tooling.composite.internal;

import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.eclipse.HierarchicalEclipseProject;

/**
 * Keeps track of {@link EclipseProject}s to present a consistent hierarchy.
 *
 * De-duplication and dependency substitution require wrapping the original {@link EclipseProject}
 * tree. The new tree must not point back to the unwrapped projects when calling methods like
 * {@link EclipseProject#getParent()}. This lookup is passed to the wrapper objects to keep track of
 * other wrapped instances.
 *
 * @author Stefan Oehme
 */
public interface RedirectedProjectLookup {

    EclipseProject getRedirectedProject(HierarchicalEclipseProject original);
}
