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

package com.gradleware.tooling.toolingmodel.repository;

import com.google.common.eventbus.EventBus;
import com.gradleware.tooling.toolingclient.ToolingClient;
import com.gradleware.tooling.toolingmodel.repository.internal.DefaultModelRepositoryProvider;
import org.gradle.internal.Factory;

/**
 * Factory class to create {@link ModelRepositoryProvider} instances.
 *
 * @author Etienne Studer
 */
public final class ModelRepositoryProviderFactory {

    /**
     * Creates a new instance.
     *
     * @param toolingClient the backing tooling client
     */
    public static ModelRepositoryProvider create(ToolingClient toolingClient) {
        return new DefaultModelRepositoryProvider(toolingClient);
    }

    /**
     * Creates a new instance.
     *
     * @param toolingClient the backing tooling client
     * @param environment the environment in which the model repository is used
     */
    public static ModelRepositoryProvider create(ToolingClient toolingClient, Environment environment) {
        return new DefaultModelRepositoryProvider(toolingClient, environment);
    }

    /**
     * Creates a new instance.
     *
     * @param toolingClient the backing tooling client
     * @param environment the environment in which the model repository is used
     * @param eventBusFactory the factory for the event bus that is used to send events upon model changes
     */
    public static ModelRepositoryProvider create(ToolingClient toolingClient, Environment environment, Factory<EventBus> eventBusFactory) {
        return new DefaultModelRepositoryProvider(toolingClient, environment, eventBusFactory);
    }

}
