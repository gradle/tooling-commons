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

package org.gradle.tooling.composite.internal;

import com.google.common.collect.Sets;
import org.gradle.api.Transformer;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.composite.CompositeBuildConnection;
import org.gradle.tooling.composite.CompositeBuildConnector;
import org.gradle.tooling.composite.CompositeParticipant;
import org.gradle.tooling.composite.internal.dist.GradleDistribution;
import org.gradle.tooling.composite.internal.dist.InstalledGradleDistribution;
import org.gradle.tooling.composite.internal.dist.URILocatedGradleDistribution;
import org.gradle.tooling.composite.internal.dist.VersionBasedGradleDistribution;
import org.gradle.util.CollectionUtils;

import java.io.File;
import java.util.Set;

/**
 * The default implementation of a composite build connector.
 * 
 * @author Benjamin Muschko
 */
public class DefaultCompositeBuildConnector extends CompositeBuildConnector {
    private final Set<DefaultCompositeParticipant> participants = Sets.newLinkedHashSet();

    @Override
    public CompositeParticipant addParticipant(File rootProjectDirectory) {
        DefaultCompositeParticipant participant = new DefaultCompositeParticipant(rootProjectDirectory);
        participants.add(participant);
        return participant;
    }

    @Override
    public CompositeBuildConnection connect() throws GradleConnectionException {
        Set<ProjectConnection> projectConnections = transformParticipantsToProjectConnections();
        return new DefaultCompositeBuildConnection(projectConnections);
    }

    private Set<ProjectConnection> transformParticipantsToProjectConnections() {
        return CollectionUtils.collect(participants, new Transformer<ProjectConnection, DefaultCompositeParticipant>() {
            @Override
            public ProjectConnection transform(DefaultCompositeParticipant participant) {
                GradleConnector gradleConnector = GradleConnector.newConnector().forProjectDirectory(participant.getRootProjectDirectory());
                useGradleDistribution(gradleConnector, participant.getDistribution());
                return gradleConnector.connect();
            }
        });
    }

    private void useGradleDistribution(GradleConnector gradleConnector, GradleDistribution gradleDistribution) {
        if (gradleDistribution == null) {
            gradleConnector.useBuildDistribution();
        } else if (gradleDistribution instanceof InstalledGradleDistribution) {
            gradleConnector.useInstallation(((InstalledGradleDistribution) gradleDistribution).getGradleHome());
        } else if (gradleDistribution instanceof URILocatedGradleDistribution) {
            gradleConnector.useDistribution(((URILocatedGradleDistribution) gradleDistribution).getLocation());
        } else if (gradleDistribution instanceof VersionBasedGradleDistribution) {
            gradleConnector.useGradleVersion(((VersionBasedGradleDistribution) gradleDistribution).getGradleVersion());
        }
    }

    Set<DefaultCompositeParticipant> getParticipants() {
        return participants;
    }
}