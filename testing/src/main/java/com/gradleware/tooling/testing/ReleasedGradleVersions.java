/*
 * Copyright 2012 the original author or authors.
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

package com.gradleware.tooling.testing;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import org.gradle.util.GradleVersion;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.Set;

/**
 * Provides released versions of Gradle. Typically, the list of provided Gradle versions is identical with the set of Gradle versions supported by the application.
 */
public final class ReleasedGradleVersions {

    public static final String RELEASED_GRADLE_VERSIONS_PROPERTIES_FILENAME = "/all-released-versions.properties";
    public static final String VERSIONS_PROPERTY_KEY = "versions";

    private final ImmutableList<GradleVersion> versions;

    private ReleasedGradleVersions(ImmutableList<GradleVersion> versions) {
        this.versions = versions;
    }

    public ImmutableList<GradleVersion> getAll() {
        return this.versions;
    }

    public GradleVersion getLatest() {
        return getAll().get(0);
    }

    /**
     * Creates a new instances from a properties file called <i>all-released-versions.properties</i> that is expected to be on the classpath. The properties file must have the
     * comma-separated version strings listed under the property key named <i>versions</i>.
     *
     * @return the new instance
     */
    public static ReleasedGradleVersions create() {
        Properties properties = loadFromPropertiesFile(RELEASED_GRADLE_VERSIONS_PROPERTIES_FILENAME);
        String[] versionStrings = getProperty(properties).split("\\s+");
        return createFrom(ImmutableSet.copyOf(versionStrings));
    }

    /**
     * Creates a new instances from the given version strings.
     *
     * @param versionStrings the version strings
     * @return the new instance
     */
    public static ReleasedGradleVersions createFrom(Set<String> versionStrings) {
        Preconditions.checkNotNull(versionStrings);
        Preconditions.checkState(!versionStrings.isEmpty());

        // transform version strings to GradleVersion instances and order them from newest to oldest
        ImmutableList<GradleVersion> versions = Ordering.natural().reverse().immutableSortedCopy(Iterables.transform(versionStrings, new Function<String, GradleVersion>() {
            @Override
            public GradleVersion apply(String version) {
                return GradleVersion.version(version);
            }
        }));

        return new ReleasedGradleVersions(versions);
    }

    public static Properties loadFromPropertiesFile(String filename) {
        URL resource = ReleasedGradleVersions.class.getResource(filename);
        if (resource == null) {
            throw new RuntimeException("Cannot find resource '" + filename + "' on the classpath");
        }

        Properties properties = null;
        try {
            properties = new Properties();
            InputStream stream = resource.openStream();
            try {
                properties.load(stream);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            Throwables.propagate(e);
        }
        return properties;
    }

    private static String getProperty(Properties properties) {
        if (!properties.containsKey(VERSIONS_PROPERTY_KEY)) {
            throw new RuntimeException("Properties file must contain key: " + VERSIONS_PROPERTY_KEY);
        }

        return properties.getProperty(VERSIONS_PROPERTY_KEY);
    }

}

