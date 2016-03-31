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
package org.gradle.tooling.composite

import com.google.common.collect.ImmutableList
import org.gradle.tooling.model.eclipse.EclipseProject

class CompositeBuildConnectorPerParticipantArgumentsIntegrationTest extends AbstractCompositeBuildConnectorIntegrationTest {

    def "can specify per-participant jvm arguments"() {
        given:
        File projectDir = directoryProvider.createDir('project')
        createBuildFile(projectDir) << """apply plugin: 'eclipse'
eclipse {
    project {
        buildCommand  System.getProperty('foo')
    }
}"""

        when:
        CompositeBuildConnection connection
        Set<ModelResult<EclipseProject>> compositeModel

        try {
            CompositeBuildConnector compositeBuildConnector = CompositeBuildConnector.newComposite()
            CompositeParticipant compositeParticipant = compositeBuildConnector.addParticipant(projectDir)
            compositeParticipant.useJvmArguments(ImmutableList.of("-Dfoo=bar"))
            CompositeBuildConnection compositeBuildConnection = compositeBuildConnector.connect()
            compositeModel = compositeBuildConnection.models(EclipseProject.class).get()
        } finally {
            connection?.close()
        }

        then:
        compositeModel[0].model.buildCommands[0].name == "bar"
    }
}
