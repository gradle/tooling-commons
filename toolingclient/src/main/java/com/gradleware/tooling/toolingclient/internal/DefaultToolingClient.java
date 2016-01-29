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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gradle.internal.Factory;
import org.gradle.jarjar.com.google.common.collect.Lists;
import org.gradle.jarjar.com.google.common.collect.Sets;
import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildActionExecuter;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.LongRunningOperation;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.TestLauncher;
import org.gradle.tooling.composite.CompositeBuildConnection;
import org.gradle.tooling.composite.CompositeBuildConnector;
import org.gradle.tooling.composite.CompositeParticipant;
import org.gradle.tooling.composite.ModelResult;
import org.gradle.tooling.internal.consumer.ConnectorServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import com.gradleware.tooling.toolingclient.BuildActionRequest;
import com.gradleware.tooling.toolingclient.BuildLaunchRequest;
import com.gradleware.tooling.toolingclient.CompositeModelRequest;
import com.gradleware.tooling.toolingclient.Consumer;
import com.gradleware.tooling.toolingclient.GradleBuildIdentifier;
import com.gradleware.tooling.toolingclient.LaunchableConfig;
import com.gradleware.tooling.toolingclient.LongRunningOperationPromise;
import com.gradleware.tooling.toolingclient.ModelRequest;
import com.gradleware.tooling.toolingclient.TestConfig;
import com.gradleware.tooling.toolingclient.TestLaunchRequest;
import com.gradleware.tooling.toolingclient.ToolingClient;

/**
 * Internal implementation of the {@link ToolingClient} API.
 *
 * @author Etienne Studer
 */
public final class DefaultToolingClient extends ToolingClient implements ExecutableToolingClient {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultToolingClient.class);

    private final Factory<GradleConnector> connectorFactory;
    private final Map<Integer, ProjectConnection> connections;
    private final Map<Integer, CompositeBuildConnection> compositeConnections;

    public DefaultToolingClient() {
        this(DefaultGradleConnectorFactory.INSTANCE);
    }

    public DefaultToolingClient(Factory<GradleConnector> connectorFactory) {
        this.connectorFactory = connectorFactory;
        this.connections = Maps.newHashMap();
        this.compositeConnections = Maps.newHashMap();
    }

    @Override
    public <T> ModelRequest<T> newModelRequest(Class<T> modelType) {
        Preconditions.checkNotNull(modelType);
        return new DefaultModelRequest<T>(this, modelType);
    }

    @Override
    public <T> BuildActionRequest<T> newBuildActionRequest(BuildAction<T> buildAction) {
        Preconditions.checkNotNull(buildAction);
        return new DefaultBuildActionRequest<T>(this, buildAction);
    }

    @Override
    public BuildLaunchRequest newBuildLaunchRequest(LaunchableConfig launchables) {
        Preconditions.checkNotNull(launchables);
        return new DefaultBuildLaunchRequest(this, launchables);
    }

    @Override
    public TestLaunchRequest newTestLaunchRequest(TestConfig tests) {
        Preconditions.checkNotNull(tests);
        return new DefaultTestLaunchRequest(this, tests);
    }

    @Override
    public <T> CompositeModelRequest<T> newCompositeModelRequest(Class<T> modelType) {
        Preconditions.checkNotNull(modelType);
        return new DefaultCompositeModelRequest<T>(this, modelType);
    }

    @Override
    public <T> T executeAndWait(InspectableModelRequest<T> modelRequest) {
        ProjectConnection connection = getProjectConnection(modelRequest);
        ModelBuilder<T> operation = mapToModelBuilder(modelRequest, connection);
        return operation.get();
    }

    @Override
    public <T> LongRunningOperationPromise<T> execute(InspectableModelRequest<T> modelRequest) {
        ProjectConnection connection = getProjectConnection(modelRequest);
        ModelBuilder<T> operation = mapToModelBuilder(modelRequest, connection);
        return LongRunningOperationPromise.forModelBuilder(operation);
    }

    @Override
    public <T> T executeAndWait(InspectableBuildActionRequest<T> buildActionRequest) {
        ProjectConnection connection = getProjectConnection(buildActionRequest);
        BuildActionExecuter<T> operation = mapToBuildActionExecuter(buildActionRequest, connection);
        return operation.run();
    }

    @Override
    public <T> LongRunningOperationPromise<T> execute(InspectableBuildActionRequest<T> buildActionRequest) {
        ProjectConnection connection = getProjectConnection(buildActionRequest);
        BuildActionExecuter<T> operation = mapToBuildActionExecuter(buildActionRequest, connection);
        return LongRunningOperationPromise.forBuildActionExecuter(operation);
    }

    @Override
    public Void executeAndWait(InspectableBuildLaunchRequest buildLaunchRequest) {
        ProjectConnection connection = getProjectConnection(buildLaunchRequest);
        BuildLauncher operation = mapToBuildLauncher(buildLaunchRequest, connection);
        operation.run();
        return null;
    }

    @Override
    public LongRunningOperationPromise<Void> execute(InspectableBuildLaunchRequest buildLaunchRequest) {
        ProjectConnection connection = getProjectConnection(buildLaunchRequest);
        BuildLauncher operation = mapToBuildLauncher(buildLaunchRequest, connection);
        return LongRunningOperationPromise.forBuildLauncher(operation);
    }

    @Override
    public Void executeAndWait(InspectableTestLaunchRequest testLaunchRequest) {
        ProjectConnection connection = getProjectConnection(testLaunchRequest);
        TestLauncher operation = mapToTestLauncher(testLaunchRequest, connection);
        operation.run();
        return null;
    }

    @Override
    public LongRunningOperationPromise<Void> execute(InspectableTestLaunchRequest testLaunchRequest) {
        ProjectConnection connection = getProjectConnection(testLaunchRequest);
        TestLauncher operation = mapToTestLauncher(testLaunchRequest, connection);
        return LongRunningOperationPromise.forTestLauncher(operation);
    }

    @Override
    public <T> LongRunningOperationPromise<Set<T>> execute(InspectableCompositeModelRequest<T> modelRequest) {
        CompositeBuildConnection connection = getOrCreateCompositeConnection(modelRequest);
        ModelBuilder<Set<ModelResult<T>>> modelBuilder = mapToModelBuilder(modelRequest, connection);
        return unwrapModelResults(LongRunningOperationPromise.forModelBuilder(modelBuilder));
    }

    @Override
    public <T> Set<T> executeAndWait(InspectableCompositeModelRequest<T> modelRequest) {
        CompositeBuildConnection connection = getOrCreateCompositeConnection(modelRequest);
        ModelBuilder<Set<ModelResult<T>>> modelBuilder = mapToModelBuilder(modelRequest, connection);
        Set<ModelResult<T>> modelResults = modelBuilder.get();
        return unwrapModelResults(modelResults);
    }

    private <T> LongRunningOperationPromise<Set<T>> unwrapModelResults(final LongRunningOperationPromise<Set<ModelResult<T>>> delegate) {
        return new LongRunningOperationPromise<Set<T>>() {

            @Override
            public LongRunningOperationPromise<Set<T>> onComplete(final Consumer<? super Set<T>> completeHandler) {
                Consumer<Set<ModelResult<T>>> unwrappingHandler = new Consumer<Set<ModelResult<T>>>() {
                    @Override
                    public void accept(Set<ModelResult<T>> input) {
                        completeHandler.accept(unwrapModelResults(input));
                    }
                };
                delegate.onComplete(unwrappingHandler);
                return this;
            }

            @Override
            public LongRunningOperationPromise<Set<T>> onFailure(Consumer<? super GradleConnectionException> failureHandler) {
                delegate.onFailure(failureHandler);
                return this;
            }
        };
    }

    private <T> Set<T> unwrapModelResults(Set<ModelResult<T>> modelResults) {
        Set<T> results = Sets.newLinkedHashSet();
        for (ModelResult<T> modelResult : modelResults) {
            results.add(modelResult.getModel());
        }
        return results;
    }

    private ProjectConnection getProjectConnection(InspectableSimpleRequest<?> request) {
        Preconditions.checkNotNull(request);
        return getOrCreateProjectConnection(request);
    }

    private ProjectConnection getOrCreateProjectConnection(InspectableSimpleRequest<?> modelRequest) {
        ProjectConnection connection;
        int connectionKey = calculateConnectionKey(modelRequest);
        synchronized (this.connections) {
            if (!this.connections.containsKey(connectionKey)) {
                connection = openConnection(modelRequest);
                this.connections.put(connectionKey, connection);
            } else {
                connection = this.connections.get(connectionKey);
            }
        }
        return connection;
    }


    private CompositeBuildConnection getOrCreateCompositeConnection(InspectableCompositeRequest<?> compositeRequest) {
        CompositeBuildConnection connection;
        int connectionKey = calculateCompositeConnectionKey(compositeRequest);
        synchronized (this.compositeConnections) {
            if (!this.compositeConnections.containsKey(connectionKey)) {
                connection = openCompositeConnection(compositeRequest);
                this.compositeConnections.put(connectionKey, connection);
            } else {
                connection = this.compositeConnections.get(connectionKey);
            }
        }
        return connection;
    }

    private int calculateConnectionKey(InspectableSimpleRequest<?> modelRequest) {
        return Objects.hashCode(
                modelRequest.getProjectDir(),
                modelRequest.getGradleUserHomeDir(),
                modelRequest.getGradleDistribution());
    }

    private int calculateCompositeConnectionKey(InspectableCompositeRequest<?> compositeRequest) {
        List<Object> connectionProperties = Lists.newArrayList();
        for (GradleBuildIdentifier identifier : compositeRequest.getParticipants()) {
            connectionProperties.add(identifier.getProjectDir());
            connectionProperties.add(identifier.getGradleDistribution());
        }
        return connectionProperties.hashCode();
    }

    private ProjectConnection openConnection(InspectableSimpleRequest<?> modelRequest) {
        GradleConnector connector = this.connectorFactory.create();
        connector.forProjectDirectory(modelRequest.getProjectDir());
        connector.useGradleUserHomeDir(modelRequest.getGradleUserHomeDir());
        modelRequest.getGradleDistribution().apply(connector);
        return connector.connect();
    }

    private CompositeBuildConnection openCompositeConnection(InspectableCompositeRequest<?> compositeRequest) {
        if (compositeRequest.getParticipants().length == 0) {
            throw new IllegalArgumentException("There must be at least one participant in a composite build");
        }
        CompositeBuildConnector connector = CompositeBuildConnector.newComposite();
        for (GradleBuildIdentifier identifier : compositeRequest.getParticipants()) {
            CompositeParticipant participant = connector.addParticipant(identifier.getProjectDir());
            identifier.getGradleDistribution().apply(participant);
        }
        return connector.connect();
    }

    private <T> ModelBuilder<T> mapToModelBuilder(InspectableModelRequest<T> modelRequest, ProjectConnection connection) {
        ModelBuilder<T> modelBuilder = connection.model(modelRequest.getModelType());
        modelBuilder.forTasks(modelRequest.getTasks());
        return mapToLongRunningOperation(modelRequest, modelBuilder);
    }

    private <T> ModelBuilder<Set<ModelResult<T>>> mapToModelBuilder(InspectableCompositeModelRequest<T> modelRequest, CompositeBuildConnection connection) {
        ModelBuilder<Set<ModelResult<T>>> modelBuilder = connection.models(modelRequest.getModelType());
        return mapToLongRunningOperation(modelRequest, modelBuilder);
    }

    private <T> BuildActionExecuter<T> mapToBuildActionExecuter(InspectableBuildActionRequest<T> buildActionRequest, ProjectConnection connection) {
        BuildActionExecuter<T> buildActionExecuter = connection.action(buildActionRequest.getBuildAction());
        return mapToLongRunningOperation(buildActionRequest, buildActionExecuter);
    }

    private BuildLauncher mapToBuildLauncher(InspectableBuildLaunchRequest buildLaunchRequest, ProjectConnection connection) {
        BuildLauncher buildLauncher = connection.newBuild();
        buildLaunchRequest.getLaunchables().apply(buildLauncher);
        return mapToLongRunningOperation(buildLaunchRequest, buildLauncher);
    }

    private TestLauncher mapToTestLauncher(InspectableTestLaunchRequest testLaunchRequest, ProjectConnection connection) {
        TestLauncher testLauncher = connection.newTestLauncher();
        testLaunchRequest.getTests().apply(testLauncher);
        return mapToLongRunningOperation(testLaunchRequest, testLauncher);
    }

    private <T extends LongRunningOperation> T mapToLongRunningOperation(InspectableRequest<?> request, T operation) {
        operation.setColorOutput(request.isColorOutput()).
                setStandardOutput(request.getStandardOutput()).
                setStandardError(request.getStandardError()).
                setStandardInput(request.getStandardInput()).
                setJavaHome(request.getJavaHomeDir()).
                setJvmArguments(request.getJvmArguments()).
                withArguments(request.getArguments()).
                withCancellationToken(request.getCancellationToken());
        for (ProgressListener progressListener : request.getProgressListeners()) {
            operation.addProgressListener(progressListener);
        }
        for (org.gradle.tooling.events.ProgressListener progressListener : request.getTypedProgressListeners()) {
            operation.addProgressListener(progressListener);
        }
        return operation;
    }

    @Override
    public void stop(CleanUpStrategy strategy) {
        switch (strategy) {
            case FORCEFULLY:
                // should happen asynchronously
                throw new UnsupportedOperationException(String.format("Cleanup strategy %s is currently not supported.", CleanUpStrategy.FORCEFULLY));
            case GRACEFULLY:
                // happens synchronously
                closeConnections();
                expireDaemons();
        }
    }

    private void closeConnections() {
        // todo (etst) do not allow new connections once shutdown is in process
        synchronized (this.connections) {
            for (ProjectConnection connection : this.connections.values()) {
                try {
                    connection.close();
                } catch (Exception e) {
                    LOG.error("Error closing the connection: " + e.getMessage(), e);
                }
            }
        }
        synchronized (this.compositeConnections) {
            for (CompositeBuildConnection connection : this.compositeConnections.values()) {
                try {
                    connection.close();
                } catch (Exception e) {
                    LOG.error("Error closing the composite connection: " + e.getMessage(), e);
                }
            }
        }
    }

    private void expireDaemons() {
        // close and re-initialize the services
        ConnectorServices.reset();
    }

    /**
     * Singleton factory to create {@code GradleConnector} instances.
     */
    private enum DefaultGradleConnectorFactory implements Factory<GradleConnector> {

        INSTANCE;

        @Override
        public GradleConnector create() {
            return GradleConnector.newConnector();
        }

    }

}
