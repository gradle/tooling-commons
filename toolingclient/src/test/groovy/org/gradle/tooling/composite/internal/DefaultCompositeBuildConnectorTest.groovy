package org.gradle.tooling.composite.internal

import spock.lang.Specification

class DefaultCompositeBuildConnectorTest extends Specification {
    DefaultCompositeBuildConnector connector = new DefaultCompositeBuildConnector()

    def "can add multiple, different participant"() {
        given:
        File projectDir1 = new File('/dev/project1')
        File projectDir2 = new File('/dev/project2')
        File projectDir3 = new File('/dev/project3')

        when:
        connector.addParticipant(projectDir1)
        connector.addParticipant(projectDir2)
        connector.addParticipant(projectDir3)

        then:
        connector.participants.size() == 3
        connector.participants.contains(new DefaultCompositeParticipant(projectDir1))
        connector.participants.contains(new DefaultCompositeParticipant(projectDir2))
        connector.participants.contains(new DefaultCompositeParticipant(projectDir3))
    }

    def "adding a second participant with the same project directory is a no-op"() {
        given:
        File projectDir = new File('/dev/project')

        when:
        connector.addParticipant(projectDir)
        connector.addParticipant(projectDir)

        then:
        connector.participants.size() == 1
        connector.participants.contains(new DefaultCompositeParticipant(projectDir))
    }
}
