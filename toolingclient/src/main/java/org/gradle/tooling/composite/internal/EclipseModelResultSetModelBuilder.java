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

package org.gradle.tooling.composite.internal;

import org.gradle.api.Transformer;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.event.ListenerNotificationException;
import org.gradle.tooling.*;
import org.gradle.tooling.composite.ModelResult;
import org.gradle.tooling.exceptions.UnsupportedBuildArgumentException;
import org.gradle.tooling.exceptions.UnsupportedOperationConfigurationException;
import org.gradle.tooling.internal.consumer.AbstractLongRunningOperation;
import org.gradle.tooling.internal.consumer.ConnectionParameters;
import org.gradle.tooling.internal.consumer.async.AsyncConsumerActionExecutor;
import org.gradle.tooling.internal.consumer.connection.ConsumerAction;
import org.gradle.tooling.internal.consumer.connection.ConsumerConnection;
import org.gradle.tooling.internal.consumer.parameters.ConsumerOperationParameters;
import org.gradle.tooling.internal.protocol.BuildExceptionVersion1;
import org.gradle.tooling.internal.protocol.InternalBuildCancelledException;
import org.gradle.tooling.internal.protocol.ResultHandlerVersion1;
import org.gradle.tooling.internal.protocol.exceptions.InternalUnsupportedBuildArgumentException;
import org.gradle.tooling.internal.protocol.test.InternalTestExecutionException;
import org.gradle.tooling.model.UnsupportedMethodException;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.internal.Exceptions;
import org.gradle.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Model builder for a Set of Eclipse ModelResults.
 *
 * @param <T>
 * @author Benjamin Muschko
 */
public class EclipseModelResultSetModelBuilder<T> extends AbstractLongRunningOperation<EclipseModelResultSetModelBuilder<T>> implements ModelBuilder<Set<ModelResult<T>>> {
    private final Class<T> modelType;
    private final AsyncConsumerActionExecutor connection;
    private final CompositeModelProducer<EclipseProject> compositeModelProducer;

    public EclipseModelResultSetModelBuilder(Class<T> modelType, AsyncConsumerActionExecutor connection,
                                             ConnectionParameters parameters, Set<ProjectConnection> participants) {
        super(parameters);
        this.modelType = modelType;
        this.connection = connection;
        this.compositeModelProducer = new EclipseProjectCompositeModelProducer(participants);
        operationParamsBuilder.setEntryPoint("Eclipse ModelBuilder API");
    }

    @Override
    protected EclipseModelResultSetModelBuilder<T> getThis() {
        return this;
    }

    @Override
    public ModelBuilder<Set<ModelResult<T>>> forTasks(String... tasks) {
        List<String> rationalizedTasks = rationalizeInput(tasks);
        operationParamsBuilder.setTasks(rationalizedTasks);
        return this;
    }

    @Override
    public ModelBuilder<Set<ModelResult<T>>> forTasks(Iterable<String> tasks) {
        operationParamsBuilder.setTasks(rationalizeInput(tasks));
        return this;
    }

    @Override
    public Set<ModelResult<T>> get() throws GradleConnectionException, IllegalStateException {
        BlockingResultHandler<T> handler = new BlockingResultHandler<T>();
        get(handler);
        return handler.getResult();
    }

    @Override
    public void get(final ResultHandler<? super Set<ModelResult<T>>> handler) throws IllegalStateException {
        final ConsumerOperationParameters operationParameters = getConsumerOperationParameters();
        connection.run(new ConsumerAction<T>() {
            public ConsumerOperationParameters getParameters() {
                return operationParameters;
            }

            public T run(ConsumerConnection connection) {
                return (T) toModelResults(compositeModelProducer.getModel());
            }
        }, new DefaultResultHandler(handler));
    }

    private <T> Set<ModelResult<T>> toModelResults(Set<EclipseProject> eclipseProjects) {
        return CollectionUtils.collect(eclipseProjects, new Transformer<ModelResult<T>, EclipseProject>() {
            @SuppressWarnings("unchecked")
            @Override
            public ModelResult<T> transform(EclipseProject eclipseProject) {
                return new DefaultModelResult<T>((T) eclipseProject);
            }
        });
    }

    /**
     * The default implementation of a model result.
     *
     * @author Benjamin Muschko
     */
    private static final class DefaultModelResult<T> implements ModelResult<T> {
        private final T model;

        private DefaultModelResult(T model) {
            this.model = model;
        }

        @Override
        public T getModel() {
            return model;
        }
    }

    /**
     * Implementation of a result handler that blocks until request is fully processed.
     *
     * @param <T> type
     */
    private class BlockingResultHandler<T> implements ResultHandler<Set<ModelResult<T>>> {
        private final BlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(1);
        private final Object NULL = new Object();

        public Set<ModelResult<T>> getResult() {
            Object result;
            try {
                result = queue.take();
            } catch (InterruptedException e) {
                throw UncheckedException.throwAsUncheckedException(e);
            }

            if (result instanceof Throwable) {
                throw UncheckedException.throwAsUncheckedException(attachCallerThreadStackTrace((Throwable) result));
            }
            if (result == NULL) {
                return null;
            }
            return (Set<ModelResult<T>>)result;
        }

        private Throwable attachCallerThreadStackTrace(Throwable failure) {
            List<StackTraceElement> adjusted = new ArrayList<StackTraceElement>();
            adjusted.addAll(Arrays.asList(failure.getStackTrace()));
            List<StackTraceElement> currentThreadStack = Arrays.asList(Thread.currentThread().getStackTrace());
            if (!currentThreadStack.isEmpty()) {
                adjusted.addAll(currentThreadStack.subList(2, currentThreadStack.size()));
            }
            failure.setStackTrace(adjusted.toArray(new StackTraceElement[adjusted.size()]));
            return failure;
        }

        public void onComplete(Set<ModelResult<T>> result) {
            queue.add(result == null ? NULL : result);
        }

        public void onFailure(GradleConnectionException failure) {
            queue.add(failure);
        }
    }

    /**
     * Default implementation of a result handler.
     *
     * @param <T> type
     */
    public class DefaultResultHandler<T> implements ResultHandlerVersion1<Set<ModelResult<T>>> {
        private final ResultHandler<? super Set<ModelResult<T>>> handler;

        public DefaultResultHandler(ResultHandler<? super Set<ModelResult<T>>> handler) {
            this.handler = handler;
        }

        @Override
        public void onComplete(Set<ModelResult<T>> result) {
            handler.onComplete(result);
        }

        @Override
        public void onFailure(Throwable failure) {
            if (failure instanceof InternalUnsupportedBuildArgumentException) {
                handler.onFailure(new UnsupportedBuildArgumentException(connectionFailureMessage(failure)
                        + "\n" + failure.getMessage(), failure));
            } else if (failure instanceof UnsupportedOperationConfigurationException) {
                handler.onFailure(new UnsupportedOperationConfigurationException(connectionFailureMessage(failure)
                        + "\n" + failure.getMessage(), failure.getCause()));
            } else if (failure instanceof GradleConnectionException) {
                handler.onFailure((GradleConnectionException) failure);
            } else if (failure instanceof InternalBuildCancelledException) {
                handler.onFailure(new BuildCancelledException(connectionFailureMessage(failure), failure.getCause()));
            } else if (failure instanceof InternalTestExecutionException) {
                handler.onFailure(new TestExecutionException(connectionFailureMessage(failure), failure.getCause()));
            } else if (failure instanceof BuildExceptionVersion1) {
                handler.onFailure(new BuildException(connectionFailureMessage(failure), failure.getCause()));
            } else if (failure instanceof ListenerNotificationException) {
                handler.onFailure(new ListenerFailedException(connectionFailureMessage(failure), ((ListenerNotificationException) failure).getCauses()));
            } else {
                handler.onFailure(new GradleConnectionException(connectionFailureMessage(failure), failure));
            }
        }

        private String connectionFailureMessage(Throwable failure) {
            String message = String.format("Could not fetch model of type '%s' using %s.", modelType.getSimpleName(), connection.getDisplayName());
            if (!(failure instanceof UnsupportedMethodException) && failure instanceof UnsupportedOperationException) {
                message += "\n" + Exceptions.INCOMPATIBLE_VERSION_HINT;
            }
            return message;
        }
    }
}
