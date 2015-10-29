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

import com.google.common.collect.ImmutableMap;
import com.gradleware.tooling.toolingmodel.OmniEclipseBuildCommand;
import org.gradle.tooling.model.eclipse.EclipseBuildCommand;

import java.util.Map;

/**
 * Default implementation of the {@link OmniEclipseBuildCommand} interface.
 *
 * @author Donát Csikós
 */
public final class DefaultOmniEclipseBuildCommand implements OmniEclipseBuildCommand {

    private final String name;
    private final Map<String, String> arguments;

    private DefaultOmniEclipseBuildCommand(String name, Map<String, String> arguments) {
        this.name = name;
        this.arguments = ImmutableMap.copyOf(arguments);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, String> getArguments() {
        return arguments;
    }

    public static DefaultOmniEclipseBuildCommand from(EclipseBuildCommand buildCommand) {
        return new DefaultOmniEclipseBuildCommand(buildCommand.getName(), buildCommand.getArguments());
    }

}
