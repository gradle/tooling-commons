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
import com.google.common.base.Supplier;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingclient.GradleDistribution;
import org.gradle.api.specs.Spec;
import org.gradle.util.DistributionLocator;
import org.gradle.util.GradleVersion;

import java.net.URI;
import java.util.List;

/**
 * Provides parameterization for Spock that includes one or more Gradle distributions on the first dimension of the data table.
 * <p/>
 * The provided Gradle version pattern supports '&gt;=nnn', '&lt;=nnn', '&gt;nnn', '&lt;nnn', '=nnn', 'current', '!current', or a space-separated list of these patterns.
 * <p/>
 * See https://code.google.com/p/spock/wiki/Parameterizations
 *
 * @author Etienne Studer
 */
public final class GradleVersionParameterization {

    private final GradleVersionProvider gradleVersionProvider;

    public GradleVersionParameterization(GradleVersionProvider gradleVersionProvider) {
        this.gradleVersionProvider = gradleVersionProvider;
    }

    /**
     * Returns a list of triplets of all permutations of the matching Gradle versions and the provided data values, where the matching Gradle versions also fall into the supplied
     * version range.
     *
     * @param gradleVersionPattern the Gradle versions to match, must not be null
     * @param dataValues the second dimension of data values, must not be null
     * @param moreDataValues the third dimension of data values, must not be null
     * @return the list of triplets, the first element of each triplet is of type {@code GradleDistribution}, never null
     */
    public ImmutableList<List<Object>> getPermutations(String gradleVersionPattern, List<Object> dataValues, List<Object> moreDataValues) {
        Preconditions.checkNotNull(gradleVersionPattern);
        Preconditions.checkNotNull(dataValues);
        Preconditions.checkNotNull(moreDataValues);

        return Combinations.getCombinations(getGradleDistributions(gradleVersionPattern), dataValues, moreDataValues);
    }

    /**
     * Returns a list of pairs of all permutations of the matching Gradle versions and the provided data values, where the matching Gradle versions also fall into the supplied
     * version range.
     *
     * @param gradleVersionPattern the Gradle versions to match, must not be null
     * @param dataValues the second dimension of data values, must not be null
     * @return the list of pairs, the first element of each pair is of type {@code GradleDistribution}, never null
     */
    public ImmutableList<List<Object>> getPermutations(String gradleVersionPattern, List<Object> dataValues) {
        Preconditions.checkNotNull(gradleVersionPattern);
        Preconditions.checkNotNull(dataValues);

        return Combinations.getCombinations(getGradleDistributions(gradleVersionPattern), dataValues);
    }

    /**
     * Returns a list of {@code GradleDistribution} that match the given Gradle version pattern, where the matching Gradle versions also fall into the supplied version range.
     *
     * @param gradleVersionPattern the pattern of Gradle versions to match, must not be null
     * @return the matching Gradle distributions falling into the configured range, never null
     */
    public ImmutableList<GradleDistribution> getGradleDistributions(String gradleVersionPattern) {
        Preconditions.checkNotNull(gradleVersionPattern);

        ImmutableList<GradleVersion> gradleVersions = getGradleVersions(gradleVersionPattern);
        return convertGradleVersionsToGradleDistributions(gradleVersions);
    }

    private ImmutableList<GradleDistribution> convertGradleVersionsToGradleDistributions(ImmutableList<GradleVersion> gradleVersions) {
        return FluentIterable.from(gradleVersions).transform(new Function<GradleVersion, GradleDistribution>() {
            @Override
            public GradleDistribution apply(GradleVersion input) {
                if (input.isSnapshot()) {
                    URI distributionLocation = new DistributionLocator().getDistributionFor(input);
                    return GradleDistribution.forRemoteDistribution(distributionLocation);
                } else {
                    return GradleDistribution.forVersion(input.getBaseVersion().getVersion());
                }
            }
        }).toList();
    }

    /**
     * Returns a list of {@code GradleVersion} that match the given Gradle version pattern, where the matching Gradle versions also fall into the supplied version range.
     *
     * @param gradleVersionPattern the pattern of Gradle versions to match, must not be null
     * @return the matching Gradle versions falling into the configured range, never null
     */
    ImmutableList<GradleVersion> getGradleVersions(String gradleVersionPattern) {
        Preconditions.checkNotNull(gradleVersionPattern);

        final Spec<GradleVersion> versionSpec = GradleVersionSpec.toSpec(gradleVersionPattern);

        ImmutableList<GradleVersion> configuredGradleVersions = this.gradleVersionProvider.getConfiguredGradleVersions();
        return FluentIterable.from(configuredGradleVersions).filter(new Predicate<GradleVersion>() {
            @Override
            public boolean apply(GradleVersion input) {
                return versionSpec.isSatisfiedBy(input);
            }
        }).toList();
    }

    /**
     * Provides a default implementation that derives the Gradle version range from the system property <i>com.gradleware.tooling.integtest.versions</i>. <p> Implemented as an
     * inner class to defer instantiation until the first time the class is referenced at runtime.
     *
     * @author Etienne Studer
     */
    public static final class Default {

        /**
         * Contains the name of the system property that is queried to get the applicable version range.
         */
        public static final String CROSS_VERSION_SYSTEM_PROPERTY_NAME = "com.gradleware.tooling.integtest.versions";

        /**
         * A {@code GradleVersionParameterization} instance that reads the applicable version range from a properties file <i>/all-released-versions.properties</i>. If the system
         * property <i>com.gradleware.tooling.integtest.versions</i> is not set, the default value <i>latest</i> is used, meaning the current Gradle version and the latest Gradle
         * version are applied.
         */
        public static final GradleVersionParameterization INSTANCE = new GradleVersionParameterization(new GradleVersionProvider(new Supplier<String>() {
            @Override
            public String get() {
                return System.getProperty(CROSS_VERSION_SYSTEM_PROPERTY_NAME, GradleVersionProvider.LATEST);
            }
        }));

    }

}
