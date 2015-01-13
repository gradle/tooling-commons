package com.gradleware.tooling.domain.internal;

import com.google.common.base.Converter;
import com.gradleware.tooling.domain.repository.BuildInvocationsContainer;
import org.gradle.tooling.model.gradle.BuildInvocations;

import java.util.Map;

/**
 * Converter between {@code BuildInvocations} mapped by project and the {@code BuildInvocationsContainer}.
 */
@SuppressWarnings("NullableProblems")
public final class BuildInvocationsConverter extends Converter<Map<String, BuildInvocations>, BuildInvocationsContainer> {

    public static final BuildInvocationsConverter INSTANCE = new BuildInvocationsConverter();

    private BuildInvocationsConverter() {
    }

    @Override
    protected BuildInvocationsContainer doForward(Map<String, BuildInvocations> mapping) {
        return BuildInvocationsContainer.from(mapping);
    }

    @Override
    protected Map<String, BuildInvocations> doBackward(BuildInvocationsContainer container) {
        return container.asMap();
    }

}
