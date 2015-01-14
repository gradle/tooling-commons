package com.gradleware.tooling.toolingmodel.repository;

import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingclient.Request;
import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.ProgressListener;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Container to hold those attributes of a {@link com.gradleware.tooling.toolingclient.Request} that do change between request invocations.
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
        return this.colorOutput;
    }

    @SuppressWarnings("UnusedDeclaration")
    public OutputStream getStandardOutput() {
        return this.standardOutput;
    }

    @SuppressWarnings("UnusedDeclaration")
    public OutputStream getStandardError() {
        return this.standardError;
    }

    @SuppressWarnings("UnusedDeclaration")
    public InputStream getStandardInput() {
        return this.standardInput;
    }

    @SuppressWarnings("UnusedDeclaration")
    public ImmutableList<ProgressListener> getProgressListeners() {
        return this.progressListeners;
    }

    @SuppressWarnings("UnusedDeclaration")
    public CancellationToken getCancellationToken() {
        return this.cancellationToken;
    }

    public void apply(Request<?> request) {
        request.colorOutput(this.colorOutput);
        request.standardOutput(this.standardOutput);
        request.standardError(this.standardError);
        request.standardInput(this.standardInput);
        request.progressListeners(this.progressListeners.toArray(new ProgressListener[this.progressListeners.size()]));
        request.cancellationToken(this.cancellationToken);
    }

}
