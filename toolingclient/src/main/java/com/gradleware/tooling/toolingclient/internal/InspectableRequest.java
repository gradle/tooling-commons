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
     * @see org.gradle.tooling.internal.consumer.parameters.ConsumerOperationParameters.Builder#setJvmArguments(String...)
     */
    String[] getJvmArguments();

    /**
     * @return never null, ConsumerOperationParameters builder takes care of converting null and empty arrays to null
     * @see org.gradle.tooling.internal.consumer.parameters.ConsumerOperationParameters.Builder#setArguments(String[])
     */
    String[] getArguments();

    /**
     * @return never null, facilitates iteration when adding the listeners to AbstractLongRunningOperation
     * @see org.gradle.tooling.internal.consumer.AbstractLongRunningOperation#addProgressListener(org.gradle.tooling.ProgressListener)
     */
    ProgressListener[] getProgressListeners();

    /**
     * @return never null, AbstractLongRunningOperation does not accept null token
     * @see org.gradle.tooling.internal.consumer.AbstractLongRunningOperation#withCancellationToken(org.gradle.tooling.CancellationToken)
     */
    CancellationToken getCancellationToken();

}
