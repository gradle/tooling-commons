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
    def "can compare with other instance (#projectDir)"() {
        expect:
        DefaultCompositeParticipant participant1 = new DefaultCompositeParticipant(new File('/dev/myProject'))
        DefaultCompositeParticipant participant2 = new DefaultCompositeParticipant(projectDir)
        (participant1 == participant2) == equality
        (participant1.hashCode() == participant2.hashCode()) == hashCode
        (participant1.toString() == participant2.toString()) == stringRepresentation

        where:
        projectDir                 | equality | hashCode | stringRepresentation
        new File('/dev/myProject') | true     | true     | true
        new File('/dev/other')     | false    | false    | false
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
