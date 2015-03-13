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

package com.gradleware.tooling.toolingclient;

import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressEvent;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.TestProgressEvent;
import org.gradle.tooling.TestProgressListener;
import org.gradle.tooling.model.build.BuildEnvironment;

import java.io.File;
import java.net.URISyntaxException;

import static com.gradleware.tooling.toolingclient.ToolingClient.CleanUpStrategy;

public final class ToolingClientSample {

    public static void main(String[] args) throws URISyntaxException {
        final File pathToProjectDir = new File(args[0]);

        ToolingClient client = ToolingClient.newClient();

        try {
            CancellationTokenSource tokenSource = GradleConnector.newCancellationTokenSource();

            ModelRequest<BuildEnvironment> modelRequest = client.newModelRequest(BuildEnvironment.class).
                    projectDir(pathToProjectDir).
                    gradleUserHomeDir(new File("~/.gradle")).
                    gradleDistribution(GradleDistribution.fromBuild()).
                    colorOutput(true).
                    standardOutput(System.out).
                    standardError(System.err).
                    standardInput(System.in).
                    javaHomeDir(new File(".")).
                    jvmArguments("-Xmx64M").
                    tasks(new String[0]).
                    arguments("-q").
                    cancellationToken(tokenSource.token()).
                    progressListeners(new ProgressListener() {
                        @Override
                        public void statusChanged(ProgressEvent event) {
                            System.out.println("Progress: " + event.getDescription());
                        }
                    }).
                    testProgressListeners(new TestProgressListener() {
                        @Override
                        public void statusChanged(TestProgressEvent event) {
                            System.out.println("Test progress: " + event.getDescriptor().getName());
                        }
                    });
            LongRunningOperationPromise<BuildEnvironment> operation = modelRequest.execute();
            operation.onComplete(new Consumer<BuildEnvironment>() {
                @Override
                public void accept(BuildEnvironment input) {
                    System.out.println("input = " + input);
                }
            }).onFailure(new Consumer<GradleConnectionException>() {
                @Override
                public void accept(GradleConnectionException e) {
                    System.out.println("e = " + e);
                }
            });

            tokenSource.cancel();
        } finally {
            client.stop(CleanUpStrategy.GRACEFULLY);
        }
    }

}
