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

import com.gradleware.tooling.toolingmodel.OmniEclipseLinkedResource;
import org.gradle.tooling.model.eclipse.EclipseLinkedResource;

/**
 * Default implementation of the {@link OmniEclipseLinkedResource} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniEclipseLinkedResource implements OmniEclipseLinkedResource {

    private final String name;
    private final String type;
    private final String location;
    private final String locationUri;

    private DefaultOmniEclipseLinkedResource(String name, String type, String location, String locationUri) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.locationUri = locationUri;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public String getLocation() {
        return this.location;
    }

    @Override
    public String getLocationUri() {
        return this.locationUri;
    }

    public static DefaultOmniEclipseLinkedResource from(EclipseLinkedResource linkedResource) {
        return new DefaultOmniEclipseLinkedResource(
                linkedResource.getName(),
                linkedResource.getType(),
                linkedResource.getLocation(),
                linkedResource.getLocationUri());
    }

}
