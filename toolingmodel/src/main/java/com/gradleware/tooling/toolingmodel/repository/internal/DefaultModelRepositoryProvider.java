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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.gradleware.tooling.toolingclient.ToolingClient;
import com.gradleware.tooling.toolingmodel.repository.*;
import org.gradle.internal.Factory;

import java.util.Map;

/**
 * Internal implementation of the {@code ModelRepositoryProvider} API.
 *
 * @author Etienne Studer
 */
public final class DefaultModelRepositoryProvider implements ModelRepositoryProvider {

    private final ToolingClient toolingClient;
    private final Environment environment;
    private final Factory<EventBus> eventBusFactory;
    private final Map<FixedRequestAttributes, SingleBuildModelRepository> modelRepositories;
    private final Map<FixedRequestAttributes, CompositeBuildModelRepository> compositeModelRepositories;

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
        this.compositeModelRepositories = Maps.newHashMap();
    }

    @Override
    public SingleBuildModelRepository getModelRepository(FixedRequestAttributes fixedRequestAttributes) {
        Preconditions.checkNotNull(fixedRequestAttributes);
        return getOrCreateModelRepository(fixedRequestAttributes);
    }

    @Override
    public CompositeBuildModelRepository getCompositeModelRepository(FixedRequestAttributes fixedRequestAttribute) {
        return getOrCreateCompositeModelRepository(fixedRequestAttribute);
    }

    private SingleBuildModelRepository getOrCreateModelRepository(FixedRequestAttributes fixedRequestAttributes) {
        SingleBuildModelRepository modelRepository;
        synchronized (this.modelRepositories) {
            if (!this.modelRepositories.containsKey(fixedRequestAttributes)) {
                modelRepository = new DefaultSingleBuildModelRepository(fixedRequestAttributes, this.toolingClient, this.eventBusFactory.create(), this.environment);
                this.modelRepositories.put(fixedRequestAttributes, modelRepository);
            } else {
                modelRepository = this.modelRepositories.get(fixedRequestAttributes);
            }
        }
        return modelRepository;
    }

    private CompositeBuildModelRepository getOrCreateCompositeModelRepository(FixedRequestAttributes fixedRequestAttributes) {
        CompositeBuildModelRepository modelRepository;
        synchronized (this.compositeModelRepositories) {
            if (!this.compositeModelRepositories.containsKey(fixedRequestAttributes)) {
                modelRepository = new DefaultCompositeModelRepository(fixedRequestAttributes, this.toolingClient, this.eventBusFactory.create());
                this.compositeModelRepositories.put(fixedRequestAttributes, modelRepository);
            } else {
                modelRepository = this.compositeModelRepositories.get(fixedRequestAttributes);
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
