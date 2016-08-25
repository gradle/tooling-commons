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

package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.OmniEclipseOutputLocation;
import com.gradleware.tooling.toolingmodel.repository.internal.compatibility.ForwardCompatibilityEclipseOutputLocation;

/**
 * Default implementation of {@link OmniEclipseOutputLocation}.
 *
 * @author Donat Csikos
 */
final class DefaultOmniEclipseOutputLocation implements OmniEclipseOutputLocation {

    private final String path;

    DefaultOmniEclipseOutputLocation(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    public static DefaultOmniEclipseOutputLocation from(ForwardCompatibilityEclipseOutputLocation outputLocation) {
        return new DefaultOmniEclipseOutputLocation(outputLocation.getPath());
    }
}

