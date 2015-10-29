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

import org.gradle.tooling.model.eclipse.EclipseProjectNature;

import com.gradleware.tooling.toolingmodel.OmniEclipseProjectNature;

/**
 * Default implementation of the {@link OmniEclipseProjectNature} interface.
 *
 * @author Donát Csikós
 */
public final class DefaultOmniEclipseProjectNature implements OmniEclipseProjectNature {

    private final String id;

    private DefaultOmniEclipseProjectNature(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public static DefaultOmniEclipseProjectNature from(EclipseProjectNature projectNature) {
        return new DefaultOmniEclipseProjectNature(projectNature.getId());
    }

}
