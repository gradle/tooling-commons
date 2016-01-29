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

import org.gradle.tooling.BuildCancelledException
import org.gradle.tooling.CancellationTokenSource
import org.gradle.tooling.ProgressEvent
import org.gradle.tooling.ProgressListener
import org.gradle.tooling.internal.consumer.DefaultCancellationTokenSource
import org.gradle.tooling.model.eclipse.EclipseProject

class CompositeBuildConnectorModelBuilderIntegrationTest extends AbstractCompositeBuildConnectorIntegrationTest {

    def "can create composite, provide cancellation token but not cancel the operation"() {
        given:
        File projectDir = directoryProvider.createDir('project')
        createBuildFile(projectDir)

        when:
        CancellationTokenSource tokenSource = new DefaultCancellationTokenSource()
        Set<ModelResult<EclipseProject>> compositeModel

        withCompositeConnection([projectDir]) { connection ->
            compositeModel = connection.models(EclipseProject).withCancellationToken(tokenSource.token()).get()
        }

        then:
        compositeModel.size() == 1
    }

    def "can create composite and cancel model creation"() {
        given:
        File projectDir = directoryProvider.createDir('project')
        createBuildFile(projectDir)

        when:
        CancellationTokenSource tokenSource = new DefaultCancellationTokenSource()
        tokenSource.cancel()

        Set<ModelResult<EclipseProject>> compositeModel

        withCompositeConnection([projectDir]) { connection ->
            compositeModel = connection.models(EclipseProject).withCancellationToken(tokenSource.token()).get()
        }

        then:
        Throwable t = thrown(BuildCancelledException)
        t.message == 'Build cancelled'
    }

    def "can create composite and provide progress listener"() {
        given:
        File projectDir = directoryProvider.createDir('project')
        createBuildFile(projectDir)

        when:
        List<String> progressEventDescriptions = []
        Set<ModelResult<EclipseProject>> compositeModel

        withCompositeConnection([projectDir]) { connection ->
            compositeModel = connection.models(EclipseProject).addProgressListener(new ProgressListener() {
                @Override
                void statusChanged(ProgressEvent event) {
                    progressEventDescriptions << event.description
                }
            }).get()
        }

        then:
        compositeModel.size() == 1
        progressEventDescriptions.size() == 2
        progressEventDescriptions[0] == 'Build'
        progressEventDescriptions[1] == ''
    }
}
