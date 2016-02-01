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

package org.gradle.tooling.composite.internal.deduplication;

import org.gradle.tooling.model.HierarchicalElement;

/**
 * Adapts any type of {@link HierarchicalElement} to the {@link HierarchicalElementDeduplicator}.
 *
 * Clients only need to implement {@link #handleRename(String)}, which is called when the
 * de-duplicator assigns a new name to the element.
 *
 * @author Stefan Oehme
 */
public abstract class RenamableElement {

    private final HierarchicalElement original;
    private String newName;

    public RenamableElement(HierarchicalElement original) {
        this.original = original;
    }

    public final HierarchicalElement getOriginal() {
        return this.original;
    }

    public final String getName() {
        return hasBeenRenamed() ? this.newName : this.original.getName();
    }

    public final void renameTo(String newName) {
        this.newName = newName;
        handleRename(newName);
    }

    public final boolean hasBeenRenamed() {
        return this.newName != null;
    }

    protected abstract void handleRename(String newName);

}
