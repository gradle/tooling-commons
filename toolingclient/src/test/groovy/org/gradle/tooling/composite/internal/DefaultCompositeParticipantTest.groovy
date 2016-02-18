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
package org.gradle.tooling.composite.internal

import org.gradle.api.Action
import org.gradle.tooling.composite.CompositeParticipant
import org.gradle.tooling.composite.internal.dist.InstalledGradleDistribution
import org.gradle.tooling.composite.internal.dist.URILocatedGradleDistribution
import org.gradle.tooling.composite.internal.dist.VersionBasedGradleDistribution
import spock.lang.Specification
import spock.lang.Unroll

class DefaultCompositeParticipantTest extends Specification {

    def "is instantiated with non-null constructor parameter values"() {
        given:
        File projectDir = new File('/dev/myProject')

        when:
        DefaultCompositeParticipant compositeParticipant = new DefaultCompositeParticipant(projectDir)

        then:
        compositeParticipant.rootProjectDirectory == projectDir
        !compositeParticipant.distribution
    }

    @Unroll
    def "can compare with other instance (#projectDir, #distribution)"() {
        given:
        DefaultCompositeParticipant participant1 = new DefaultCompositeParticipant(new File('/dev/myProject'))
        participant1.useGradleVersion('2.8')
        DefaultCompositeParticipant participant2 = new DefaultCompositeParticipant(projectDir)
        participant2.gradleDistribution = distribution

        expect:
        (participant1 == participant2) == equality
        (participant1.hashCode() == participant2.hashCode()) == hashCode
        (participant1.toString() == participant2.toString()) == stringRepresentation

        where:
        projectDir                 | distribution                                                       | equality | hashCode | stringRepresentation
        new File('/dev/myProject') | new VersionBasedGradleDistribution('2.8')                          | true     | true     | true
        new File('/dev/myProject') | new InstalledGradleDistribution(new File('.'))                     | false    | false    | false
        new File('/dev/myProject') | new URILocatedGradleDistribution(new URI('http://www.google.com')) | false    | false    | false
        new File('/dev/other')     | new VersionBasedGradleDistribution('2.8')                          | false    | false    | false
        new File('/dev/other')     | new InstalledGradleDistribution(new File('.'))                     | false    | false    | false
        new File('/dev/other')     | new URILocatedGradleDistribution(new URI('http://www.google.com')) | false    | false    | false
    }

    def "can add Gradle distribution"(distribution, Action<CompositeParticipant> configurer) {
        when:
        DefaultCompositeParticipant participant = new DefaultCompositeParticipant(new File('/dev/myProject'))
        configurer.execute(participant)

        then:
        participant.distribution == distribution

        where:
        distribution                                                       | configurer
        new InstalledGradleDistribution(new File('.'))                     | { it.useInstallation(new File('.')) }
        new VersionBasedGradleDistribution('2.8')                          | { it.useGradleVersion('2.8') }
        new URILocatedGradleDistribution(new URI('http://www.google.com')) | { it.useDistribution(new URI('http://www.google.com')) }
    }
}
