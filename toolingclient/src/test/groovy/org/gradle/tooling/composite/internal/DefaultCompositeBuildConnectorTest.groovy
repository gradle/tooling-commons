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

import org.gradle.tooling.internal.consumer.ConnectionParameters
import org.gradle.tooling.internal.consumer.async.AsyncConsumerActionExecutor
import spock.lang.Specification

class DefaultCompositeBuildConnectorTest extends Specification {
    AsyncConsumerActionExecutor connection = Mock(AsyncConsumerActionExecutor)
    ConnectionParameters parameters = Mock(ConnectionParameters)
    DefaultCompositeBuildConnector connector = new DefaultCompositeBuildConnector(connection, parameters)

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
