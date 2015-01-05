package com.gradleware.tooling.domain;

import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingapi.Request;
import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.ProgressListener;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Container to hold those attributes of a {@link com.gradleware.tooling.toolingapi.Request} that do change between request invocations.
 */
public final class TransientRequestAttributes {

    private final boolean colorOutput;
    private final OutputStream standardOutput;
    private final OutputStream standardError;
    private final InputStream standardInput;
    private final ImmutableList<ProgressListener> progressListeners;
    private final CancellationToken cancellationToken;

    public TransientRequestAttributes(boolean colorOutput, OutputStream standardOutput, OutputStream standardError, InputStream standardInput, List<ProgressListener> progressListeners, CancellationToken cancellationToken) {
        this.colorOutput = colorOutput;
        this.standardOutput = standardOutput;
        this.standardError = standardError;
        this.standardInput = standardInput;
        this.progressListeners = ImmutableList.copyOf(progressListeners);
        this.cancellationToken = cancellationToken;
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean isColorOutput() {
        return colorOutput;
    }

    @SuppressWarnings("UnusedDeclaration")
    public OutputStream getStandardOutput() {
        return standardOutput;
    }

    @SuppressWarnings("UnusedDeclaration")
    public OutputStream getStandardError() {
        return standardError;
    }

    @SuppressWarnings("UnusedDeclaration")
    public InputStream getStandardInput() {
        return standardInput;
    }

    @SuppressWarnings("UnusedDeclaration")
    public ImmutableList<ProgressListener> getProgressListeners() {
        return progressListeners;
    }

    @SuppressWarnings("UnusedDeclaration")
    public CancellationToken getCancellationToken() {
        return cancellationToken;
    }

    public void apply(Request<?> request) {
        request.colorOutput(colorOutput);
        request.standardOutput(standardOutput);
        request.standardError(standardError);
        request.standardInput(standardInput);
        request.progressListeners(progressListeners.toArray(new ProgressListener[progressListeners.size()]));
        request.cancellationToken(cancellationToken);
    }

}
