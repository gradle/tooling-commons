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

package com.gradleware.tooling.toolingmodel;

import com.google.common.base.Optional;
import com.gradleware.tooling.toolingutils.ImmutableCollection;

import java.util.SortedMap;

/**
 * Holds the {@link OmniBuildInvocations} for a given set of projects. Each project is identified by its unique full path.
 *
 * The primary advantage of this container is that it allows to work with a generics-free type compared to <code>Map&lt;String, OmniBuildInvocations&gt;</code>.
 */
public interface OmniBuildInvocationsContainer {

    /**
     * Returns the {@code OmniBuildInvocations} for the given project, where the project is identified by its unique full path.
     *
     * @param projectPath the full path of the project for which to get the build invocations
     * @return the build invocations, if present
     */
    Optional<OmniBuildInvocations> get(Path projectPath);

    /**
     * A {@code Map} of {@code OmniBuildInvocations} per project, where each project is identified by its unique full path.
     *
     * @return the mapping of projects to build invocations
     */
    @ImmutableCollection
    SortedMap<Path, OmniBuildInvocations> asMap();

}
