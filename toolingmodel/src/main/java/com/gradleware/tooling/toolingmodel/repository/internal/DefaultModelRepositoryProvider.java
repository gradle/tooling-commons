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

package com.gradleware.tooling.toolingmodel.repository.internal;

import java.util.Arrays;
import java.util.Map;

import org.gradle.internal.Factory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;

import com.gradleware.tooling.toolingclient.ToolingClient;
import com.gradleware.tooling.toolingmodel.repository.CompositeModelRepository;
import com.gradleware.tooling.toolingmodel.repository.Environment;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.ModelRepository;
import com.gradleware.tooling.toolingmodel.repository.ModelRepositoryProvider;

/**
 * Internal implementation of the {@code ModelRepositoryProvider} API.
 *
 * @author Etienne Studer
 */
public final class DefaultModelRepositoryProvider implements ModelRepositoryProvider {

    private final ToolingClient toolingClient;
    private final Environment environment;
    private final Factory<EventBus> eventBusFactory;
    private final Map<FixedRequestAttributes, ModelRepository> modelRepositories;

    public DefaultModelRepositoryProvider(ToolingClient toolingClient) {
        this(toolingClient, Environment.STANDALONE);
    }

    public DefaultModelRepositoryProvider(ToolingClient toolingClient, Environment environment) {
        this(toolingClient, environment, DefaultEventBusFactory.INSTANCE);
    }

    public DefaultModelRepositoryProvider(ToolingClient toolingClient, Environment environment, Factory<EventBus> eventBusFactory) {
        this.toolingClient = Preconditions.checkNotNull(toolingClient);
        this.environment = Preconditions.checkNotNull(environment);
        this.eventBusFactory = Preconditions.checkNotNull(eventBusFactory);
        this.modelRepositories = Maps.newHashMap();
    }

    @Override
    public ModelRepository getModelRepository(FixedRequestAttributes fixedRequestAttributes) {
        Preconditions.checkNotNull(fixedRequestAttributes);

        return getOrCreateModelRepository(fixedRequestAttributes);
    }

    @Override
    public CompositeModelRepository getCompositeModelRepository(FixedRequestAttributes... fixedRequestAttributes) {
        return new DefaultCompositeModelRepository(Arrays.asList(fixedRequestAttributes), this.toolingClient, eventBusFactory.create());
    }

    private ModelRepository getOrCreateModelRepository(FixedRequestAttributes fixedRequestAttributes) {
        ModelRepository modelRepository;
        synchronized (this.modelRepositories) {
            if (!this.modelRepositories.containsKey(fixedRequestAttributes)) {
                modelRepository = new DefaultModelRepository(fixedRequestAttributes, this.toolingClient, this.eventBusFactory.create(), this.environment);
                this.modelRepositories.put(fixedRequestAttributes, modelRepository);
            } else {
                modelRepository = this.modelRepositories.get(fixedRequestAttributes);
            }
        }
        return modelRepository;
    }

    /**
     * Singleton factory to create {@code EventBus} instances.
     */
    private enum DefaultEventBusFactory implements Factory<EventBus> {

        INSTANCE;

        @Override
        public EventBus create() {
            return new EventBus();
        }

    }
}
