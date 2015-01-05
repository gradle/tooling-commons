package com.gradleware.tooling.domain.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.gradleware.tooling.domain.Environment;
import com.gradleware.tooling.domain.FixedRequestAttributes;
import com.gradleware.tooling.domain.ModelRepository;
import com.gradleware.tooling.domain.ModelRepositoryProvider;
import com.gradleware.tooling.toolingapi.ToolingClient;
import org.gradle.internal.Factory;

import java.util.Map;

/**
 * Internal implementation of the {@code ModelRepositoryProvider} API.
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

    private ModelRepository getOrCreateModelRepository(FixedRequestAttributes fixedRequestAttributes) {
        ModelRepository modelRepository;
        synchronized (modelRepositories) {
            if (!modelRepositories.containsKey(fixedRequestAttributes)) {
                DefaultModelRepository targetModelRepository = new DefaultModelRepository(fixedRequestAttributes, eventBusFactory.create(), toolingClient);
                modelRepository = new ContextAwareModelRepository(targetModelRepository, environment);
                modelRepositories.put(fixedRequestAttributes, modelRepository);
            } else {
                modelRepository = modelRepositories.get(fixedRequestAttributes);
            }
        }
        return modelRepository;
    }

    enum DefaultEventBusFactory implements Factory<EventBus> {

        INSTANCE;

        @Override
        public EventBus create() {
            return new EventBus();
        }

    }
}
