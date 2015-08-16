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

package com.gradleware.tooling.toolingclient.internal;

import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingclient.Request;
import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.ProgressListener;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Internal interface that describes the configurable attributes of a request.
 *
 * @param <T> the result type
 * @author Etienne Studer
 */
interface InspectableRequest<T> extends Request<T> {

    File getProjectDir();

    File getGradleUserHomeDir();

    /**
     * @return never null, DefaultToolingClient requires a distribution to execute the request
     * @see DefaultToolingClient#openConnection(com.gradleware.tooling.toolingclient.internal.InspectableRequest)
     */
    GradleDistribution getGradleDistribution();

    boolean isColorOutput();

    OutputStream getStandardOutput();

    OutputStream getStandardError();

    InputStream getStandardInput();

    File getJavaHomeDir();

    /**
     * @return never null, ConsumerOperationParameters builder takes care of converting null and empty arrays to null
     * @see org.gradle.tooling.internal.consumer.parameters.ConsumerOperationParameters.Builder#setJvmArguments(java.util.List)
     */
    String[] getJvmArguments();

    /**
     * @return never null, ConsumerOperationParameters builder takes care of converting null and empty arrays to null
     * @see org.gradle.tooling.internal.consumer.parameters.ConsumerOperationParameters.Builder#setJvmArguments(java.util.List)
     */
    String[] getArguments();

    /**
     * @return never null, facilitates iteration when adding the listeners to AbstractLongRunningOperation
     * @see org.gradle.tooling.internal.consumer.AbstractLongRunningOperation#addProgressListener(org.gradle.tooling.ProgressListener)
     */
    ProgressListener[] getProgressListeners();

    /**
     * @return never null, facilitates iteration when adding the listeners to AbstractLongRunningOperation
     * @see org.gradle.tooling.internal.consumer.AbstractLongRunningOperation#addProgressListener(org.gradle.tooling.events.ProgressListener)
     */
    org.gradle.tooling.events.ProgressListener[] getTypedProgressListeners();

    /**
     * @return never null, AbstractLongRunningOperation does not accept null token
     * @see org.gradle.tooling.internal.consumer.AbstractLongRunningOperation#withCancellationToken(org.gradle.tooling.CancellationToken)
     */
    CancellationToken getCancellationToken();

}
