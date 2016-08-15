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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.gradleware.tooling.toolingclient.*;
import org.gradle.internal.Factory;
import org.gradle.tooling.*;
import org.gradle.tooling.connection.GradleConnection;
import org.gradle.tooling.connection.GradleConnectionBuilder;
import org.gradle.tooling.connection.GradleConnectionBuilder.ParticipantBuilder;
import org.gradle.tooling.connection.ModelResult;
import org.gradle.tooling.connection.ModelResults;
import org.gradle.tooling.internal.connection.GradleConnectionBuilderInternal;
import org.gradle.tooling.internal.consumer.ConnectorServices;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.util.GradleVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Internal implementation of the {@link ToolingClient} API.
 *
 * @author Etienne Studer
 */
public final class DefaultToolingClient extends ToolingClient implements ExecutableToolingClient {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultToolingClient.class);

    private final Factory<GradleConnector> connectorFactory;
    private final ConnectionStrategy connectionStrategy;
    private final Map<Integer, ProjectConnection> connections;
    private final Map<Integer, GradleConnection> compositeConnections;


    public DefaultToolingClient() {
        this(DefaultGradleConnectorFactory.INSTANCE);
    }

    public DefaultToolingClient(Factory<GradleConnector> connectorFactory) {
        this(connectorFactory, ConnectionStrategy.PER_REQUEST);
    }

    public DefaultToolingClient(Factory<GradleConnector> connectorFactory, ConnectionStrategy connectionStrategy) {
        this.connectorFactory = connectorFactory;
        this.connectionStrategy = connectionStrategy;
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
    public <T> CompositeBuildModelRequest<T> newCompositeModelRequest(Class<T> modelType) {
        Preconditions.checkNotNull(modelType);
        return new DefaultCompositeBuildModelRequest<T>(this, modelType);
    }

    @Override
    public <T> T executeAndWait(InspectableModelRequest<T> modelRequest) {
        ProjectConnection connection = getProjectConnection(modelRequest);
        ModelBuilder<T> operation = mapToModelBuilder(modelRequest, connection);
        try {
            return operation.get();
        } finally {
            closeConnectionIfNecessary(connection);
        }
    }

    @Override
    public <T> LongRunningOperationPromise<T> execute(InspectableModelRequest<T> modelRequest) {
        ProjectConnection connection = getProjectConnection(modelRequest);
        ModelBuilder<T> operation = mapToModelBuilder(modelRequest, connection);
        return closeConnectionIfNecessary(LongRunningOperationPromise.forModelBuilder(operation), connection);
    }

    @Override
    public <T> T executeAndWait(InspectableBuildActionRequest<T> buildActionRequest) {
        ProjectConnection connection = getProjectConnection(buildActionRequest);
        BuildActionExecuter<T> operation = mapToBuildActionExecuter(buildActionRequest, connection);
        try {
            return operation.run();
        } finally {
            closeConnectionIfNecessary(connection);
        }
    }

    @Override
    public <T> LongRunningOperationPromise<T> execute(InspectableBuildActionRequest<T> buildActionRequest) {
        ProjectConnection connection = getProjectConnection(buildActionRequest);
        BuildActionExecuter<T> operation = mapToBuildActionExecuter(buildActionRequest, connection);
        return closeConnectionIfNecessary(LongRunningOperationPromise.forBuildActionExecuter(operation), connection);
    }

    @Override
    public Void executeAndWait(InspectableBuildLaunchRequest buildLaunchRequest) {
        ProjectConnection connection = getProjectConnection(buildLaunchRequest);
        BuildLauncher operation = mapToBuildLauncher(buildLaunchRequest, connection);
        try {
            operation.run();
        } finally {
            closeConnectionIfNecessary(connection);
        }
        return null;
    }

    @Override
    public LongRunningOperationPromise<Void> execute(InspectableBuildLaunchRequest buildLaunchRequest) {
        ProjectConnection connection = getProjectConnection(buildLaunchRequest);
        BuildLauncher operation = mapToBuildLauncher(buildLaunchRequest, connection);
        return closeConnectionIfNecessary(LongRunningOperationPromise.forBuildLauncher(operation), connection);
    }

    @Override
    public Void executeAndWait(InspectableTestLaunchRequest testLaunchRequest) {
        ProjectConnection connection = getProjectConnection(testLaunchRequest);
        TestLauncher operation = mapToTestLauncher(testLaunchRequest, connection);
        try {
            operation.run();
        } finally {
            closeConnectionIfNecessary(connection);
        }
        return null;
    }

    @Override
    public LongRunningOperationPromise<Void> execute(InspectableTestLaunchRequest testLaunchRequest) {
        ProjectConnection connection = getProjectConnection(testLaunchRequest);
        TestLauncher operation = mapToTestLauncher(testLaunchRequest, connection);
        return closeConnectionIfNecessary(LongRunningOperationPromise.forTestLauncher(operation), connection);
    }

    @Override
    public <T> LongRunningOperationPromise<ModelResults<T>> execute(InspectableCompositeBuildModelRequest<T> modelRequest) {
        GradleConnection connection = getCompositeConnection(modelRequest);
        ModelBuilder<ModelResults<T>> modelBuilder = mapToModelBuilder(modelRequest, connection);
        return closeConnectionIfNecessary(LongRunningOperationPromise.forModelBuilder(modelBuilder), connection);
    }

    @Override
    public <T> ModelResults<T> executeAndWait(InspectableCompositeBuildModelRequest<T> modelRequest) {
        GradleConnection connection = getCompositeConnection(modelRequest);
        ModelBuilder<ModelResults<T>> modelBuilder = mapToModelBuilder(modelRequest, connection);
        try {
            ModelResults<T> modelResults = modelBuilder.get();
            return modelResults;
        } finally {
            closeConnectionIfNecessary(connection);
        }
    }

    private ProjectConnection getProjectConnection(InspectableSingleBuildRequest<?> request) {
        return getOrCreateProjectConnection(request);
    }

    private GradleConnection getCompositeConnection(InspectableCompositeBuildRequest<?> request) {
        return getOrCreateCompositeConnection(request);
    }

    private ProjectConnection getOrCreateProjectConnection(InspectableSingleBuildRequest<?> simpleRequest) {
        Preconditions.checkNotNull(simpleRequest);
        if (this.connectionStrategy == ConnectionStrategy.PER_REQUEST) {
            return openConnection(simpleRequest);
        }
        ProjectConnection connection;
        int connectionKey = calculateConnectionKey(simpleRequest);
        synchronized (this.connections) {
            connection = this.connections.get(connectionKey);
            if (connection == null) {
                connection = openConnection(simpleRequest);
                this.connections.put(connectionKey, connection);
            }
        }
        return connection;
    }


    private GradleConnection getOrCreateCompositeConnection(InspectableCompositeBuildRequest<?> compositeRequest) {
        Preconditions.checkNotNull(compositeRequest);
        if (this.connectionStrategy == ConnectionStrategy.PER_REQUEST) {
            return createIntegratedCompositeIfPossible(compositeRequest);
        }
        GradleConnection connection;
        int connectionKey = calculateCompositeConnectionKey(compositeRequest);
        synchronized (this.compositeConnections) {
            connection = this.compositeConnections.get(connectionKey);
            if (connection == null) {
                connection = createIntegratedCompositeIfPossible(compositeRequest);
                this.compositeConnections.put(connectionKey, connection);
            }
        }
        return connection;
    }

    private int calculateConnectionKey(InspectableSingleBuildRequest<?> modelRequest) {
        return Objects.hashCode(
                modelRequest.getProjectDir(),
                modelRequest.getGradleUserHomeDir(),
                modelRequest.getGradleDistribution());
    }

    private int calculateCompositeConnectionKey(InspectableCompositeBuildRequest<?> compositeRequest) {
        List<Object> connectionProperties = Lists.newArrayList();
        connectionProperties.add(compositeRequest.getProjectDir());
        connectionProperties.add(compositeRequest.getGradleDistribution());
        connectionProperties.add(compositeRequest.getGradleUserHomeDir());
        return connectionProperties.hashCode();
    }

    private ProjectConnection openConnection(InspectableSingleBuildRequest<?> modelRequest) {
        GradleConnector connector = this.connectorFactory.create();
        connector.forProjectDirectory(modelRequest.getProjectDir());
        connector.useGradleUserHomeDir(modelRequest.getGradleUserHomeDir());
        modelRequest.getGradleDistribution().apply(connector);
        return connector.connect();
    }

    private GradleConnection createIntegratedCompositeIfPossible(InspectableCompositeBuildRequest<?> compositeRequest) {
        GradleConnectionBuilder connectionBuilder = configureBasicCompositeConnection(compositeRequest);
        GradleConnection aggregateConnection = connectionBuilder.build();

        if (!(connectionBuilder instanceof GradleConnectionBuilderInternal)) {
            return aggregateConnection;
        }

        GradleVersion commonGradleVersion = getCommonGradleVersion(aggregateConnection, compositeRequest);
        if (commonGradleVersion == null || commonGradleVersion.compareTo(GradleVersion.version("2.14-rc-1")) < 0) {
            return aggregateConnection;
        } else {
            ((GradleConnectionBuilderInternal) connectionBuilder).integratedComposite(true);
            ((GradleConnectionBuilderInternal) connectionBuilder).useGradleVersion(commonGradleVersion.getVersion());
            return connectionBuilder.build();
        }
    }

    private GradleVersion getCommonGradleVersion(GradleConnection aggregateConnection, InspectableCompositeBuildRequest<?> compositeRequest) {
        ModelResults<BuildEnvironment> results = fetchBuildEnvironments(aggregateConnection, compositeRequest);

        String commonVersion = null;

        for (ModelResult<BuildEnvironment> result : results) {
            if (result.getFailure() != null) {
                return null;
            }
            BuildEnvironment buildEnvironment = result.getModel();
            String gradleVersion = buildEnvironment.getGradle().getGradleVersion();
            if (commonVersion != null && !commonVersion.equals(gradleVersion)) {
                return null;
            }
            commonVersion = gradleVersion;
        }

        return GradleVersion.version(commonVersion);
    }

    private GradleConnectionBuilder configureBasicCompositeConnection(InspectableCompositeBuildRequest<?> compositeRequest) {
        GradleConnectionBuilder connectionBuilder = GradleConnector.newGradleConnection();
        connectionBuilder.useGradleUserHomeDir(compositeRequest.getGradleUserHomeDir());
        ParticipantBuilder participantBuilder = connectionBuilder.addParticipant(compositeRequest.getProjectDir());
        compositeRequest.getGradleDistribution().apply(participantBuilder);
        return connectionBuilder;
    }

    private ModelResults<BuildEnvironment> fetchBuildEnvironments(GradleConnection aggregateConnection, InspectableCompositeBuildRequest<?> compositeRequest) {
        ModelBuilder<ModelResults<BuildEnvironment>> builder = aggregateConnection.models(BuildEnvironment.class);
        mapToLongRunningOperation(compositeRequest, builder);
        return builder.get();
    }

    private <T> ModelBuilder<T> mapToModelBuilder(InspectableModelRequest<T> modelRequest, ProjectConnection connection) {
        ModelBuilder<T> modelBuilder = connection.model(modelRequest.getModelType());
        modelBuilder.forTasks(modelRequest.getTasks());
        return mapToLongRunningOperation(modelRequest, modelBuilder);
    }

    private <T> ModelBuilder<ModelResults<T>> mapToModelBuilder(InspectableCompositeBuildModelRequest<T> modelRequest, GradleConnection connection) {
        ModelBuilder<ModelResults<T>> modelBuilder = connection.models(modelRequest.getModelType());
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
        operation.
            setColorOutput(request.isColorOutput()).
            setStandardOutput(request.getStandardOutput()).
            setStandardError(request.getStandardError()).
            setJavaHome(request.getJavaHomeDir()).
            setJvmArguments(request.getJvmArguments()).
            withArguments(request.getArguments()).
            withCancellationToken(request.getCancellationToken());

        if (!(request instanceof CompositeBuildRequest)) {
            //stdin is not (yet) supported by composite build
            operation.setStandardInput(request.getStandardInput());
        }
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
                closeConnection(connection);
            }
        }
        synchronized (this.compositeConnections) {
            for (GradleConnection connection : this.compositeConnections.values()) {
                closeConnection(connection);
            }
        }
    }

    private void closeConnection(ProjectConnection connection) {
        try {
            connection.close();
        } catch (Exception e) {
            LOG.warn("Problem closing the connection: " + e.getMessage(), e);
        }
    }

    private void closeConnection(GradleConnection connection) {
        try {
            connection.close();
        } catch (Exception e) {
            LOG.warn("Problem closing the composite connection: " + e.getMessage(), e);
        }
    }

    private <T> void closeConnectionIfNecessary(ProjectConnection connection) {
        if (DefaultToolingClient.this.connectionStrategy == ConnectionStrategy.PER_REQUEST) {
            closeConnection(connection);
        }
    }

    private <T> void closeConnectionIfNecessary(GradleConnection connection) {
        if (DefaultToolingClient.this.connectionStrategy == ConnectionStrategy.PER_REQUEST) {
            closeConnection(connection);
        }
    }

    private <T> LongRunningOperationPromise<T> closeConnectionIfNecessary(final LongRunningOperationPromise<T> delegate, final ProjectConnection connection) {
        return new LongRunningOperationPromise<T>() {

            @Override
            public LongRunningOperationPromise<T> onComplete(Consumer<? super T> completeHandler) {
                closeConnectionIfNecessary(connection);
                delegate.onComplete(completeHandler);
                return this;
            }

            @Override
            public LongRunningOperationPromise<T> onFailure(Consumer<? super GradleConnectionException> failureHandler) {
                closeConnectionIfNecessary(connection);
                delegate.onFailure(failureHandler);
                return this;
            }
        };
    }

    private <T> LongRunningOperationPromise<T> closeConnectionIfNecessary(final LongRunningOperationPromise<T> delegate, final GradleConnection connection) {
        return new LongRunningOperationPromise<T>() {

            @Override
            public LongRunningOperationPromise<T> onComplete(Consumer<? super T> completeHandler) {
                closeConnectionIfNecessary(connection);
                delegate.onComplete(completeHandler);
                return this;
            }

            @Override
            public LongRunningOperationPromise<T> onFailure(Consumer<? super GradleConnectionException> failureHandler) {
                closeConnectionIfNecessary(connection);
                delegate.onFailure(failureHandler);
                return this;
            }
        };
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
