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

package com.gradleware.tooling.testing;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.gradleware.tooling.toolingutils.distribution.PublishedGradleVersions;
import org.gradle.util.GradleVersion;

import java.util.Set;

/**
 * Provides released versions of Gradle. Typically, the list of provided Gradle versions is identical with the set of Gradle versions supported by the application.
 *
 * @author Etienne Studer
 */
public final class ReleasedGradleVersions {

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
     * Creates a new instances from {@code PublishedGradleVersions}.
     *
     * @return the new instance
     */
    public static ReleasedGradleVersions create() {
        PublishedGradleVersions publishedGradleVersions = PublishedGradleVersions.create(PublishedGradleVersions.LookupStrategy.REMOTE);
        ImmutableSet<GradleVersion> nonMilestoneReleases = FluentIterable.from(publishedGradleVersions.getVersions()).filter(new Predicate<GradleVersion>() {
            @Override
            public boolean apply(GradleVersion version) {
                return !version.getVersion().contains("-milestone-");
            }
        }).filter(new Predicate<GradleVersion>() {
            @Override
            public boolean apply(GradleVersion version) {
                // TODO (donat) Gradle 3.1 removed the composite builder API. Delete this predicate once composite build models are again accessible
                return version.compareTo(GradleVersion.version("3.0")) <= 0;
            }
        }).toSet();
        return createFromGradleVersions(nonMilestoneReleases);
    }

    /**
     * Creates a new instances from the given version strings.
     *
     * @param versionStrings the version strings
     * @return the new instance
     */
    public static ReleasedGradleVersions createFrom(Set<String> versionStrings) {
        return createFromGradleVersions(FluentIterable.from(versionStrings).transform(new Function<String, GradleVersion>() {
            @Override
            public GradleVersion apply(String version) {
                return GradleVersion.version(version);
            }
        }).toSet());
    }

    private static ReleasedGradleVersions createFromGradleVersions(Set<GradleVersion> versionStrings) {
        Preconditions.checkNotNull(versionStrings);
        Preconditions.checkState(!versionStrings.isEmpty());

        // order version strings from newest to oldest
        ImmutableList<GradleVersion> versions = Ordering.natural().reverse().immutableSortedCopy(versionStrings);

        return new ReleasedGradleVersions(versions);
    }

}

