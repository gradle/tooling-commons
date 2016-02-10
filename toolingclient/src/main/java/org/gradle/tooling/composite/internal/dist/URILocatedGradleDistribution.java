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

package org.gradle.tooling.composite.internal.dist;

import java.net.URI;

/**
 * Represents a Gradle distribution locatable by URI.
 *
 * @author Benjamin Muschko
 */
public final class URILocatedGradleDistribution implements GradleDistribution {

    private final URI location;

    public URILocatedGradleDistribution(URI location) {
        this.location = location;
    }

    public URI getLocation() {
        return this.location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        URILocatedGradleDistribution that = (URILocatedGradleDistribution) o;

        return this.location != null ? this.location.equals(that.location) : that.location == null;

    }

    @Override
    public int hashCode() {
        return this.location != null ? this.location.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("URI-located Gradle distribution: %s", this.location.toString());
    }
}