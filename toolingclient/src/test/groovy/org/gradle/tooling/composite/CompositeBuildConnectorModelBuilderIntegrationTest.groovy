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

import org.gradle.tooling.*
import org.gradle.tooling.internal.consumer.DefaultCancellationTokenSource
import org.gradle.tooling.model.eclipse.EclipseProject

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class CompositeBuildConnectorModelBuilderIntegrationTest extends AbstractCompositeBuildConnectorIntegrationTest {

    def "can provide cancellation token but not cancel the operation"() {
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

    def "can provide progress listener"() {
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

    def "event progress listener does not capture any events as no tasks are executed"() {
        given:
        File projectDir = directoryProvider.createDir('project')
        createBuildFile(projectDir)

        when:
        List<String> progressEventDisplayNames = []
        Set<ModelResult<EclipseProject>> compositeModel

        withCompositeConnection([projectDir]) { connection ->
            compositeModel = connection.models(EclipseProject).addProgressListener(new org.gradle.tooling.events.ProgressListener() {
                @Override
                void statusChanged(org.gradle.tooling.events.ProgressEvent event) {
                    progressEventDisplayNames << event.descriptor.displayName
                }
            }).get()
        }

        then:
        compositeModel.size() == 1
        progressEventDisplayNames.size() == 0
    }

    def "can capture completed request with result handler"() {
        given:
        File projectDir = directoryProvider.createDir('project')
        createBuildFile(projectDir)

        when:
        TestResultHandler<Set<ModelResult<EclipseProject>>> resultHandler = new TestResultHandler()

        withCompositeConnection([projectDir]) { connection ->
            connection.models(EclipseProject).get(resultHandler)
            resultHandler.waitForResult()
        }

        then:
        resultHandler.result.size() == 1
        !resultHandler.failure
    }

    def "can capture failure with result handler"() {
        given:
        File projectDir = new File('someDir')

        when:
        TestResultHandler resultHandler = new TestResultHandler()

        withCompositeConnection([projectDir]) { connection ->
            connection.models(EclipseProject).get(resultHandler)
            resultHandler.waitForResult()
        }

        then:
        !resultHandler.result
        resultHandler.failure.message.contains("Could not fetch model of type 'EclipseProject'")
        resultHandler.failure.cause.message == "Project directory '$projectDir.absolutePath' does not exist."
    }

    private class TestResultHandler implements ResultHandler<Set<ModelResult<EclipseProject>>> {
        private final CountDownLatch latch = new CountDownLatch(1)
        private Set<ModelResult<EclipseProject>> result
        private GradleConnectionException failure

        @Override
        void onComplete(Set<ModelResult<EclipseProject>> result) {
            this.result = result
            latch.countDown()
        }

        @Override
        void onFailure(GradleConnectionException failure) {
            this.failure = failure
            latch.countDown()
        }

        void waitForResult() {
            latch.await(20, TimeUnit.SECONDS)
        }

        Set<ModelResult<EclipseProject>> getResult() {
            result
        }

        GradleConnectionException getFailure() {
            failure
        }
    }
}
