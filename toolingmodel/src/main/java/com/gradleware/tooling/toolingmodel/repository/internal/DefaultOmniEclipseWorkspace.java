/*
 * Copyright 2016 the original author or authors.
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gradle.api.specs.Spec;
import org.gradle.impldep.com.google.common.collect.ImmutableMap;
import org.gradle.impldep.com.google.common.collect.ImmutableSet;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;

import com.gradleware.tooling.toolingclient.GradleBuildIdentifier;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniEclipseWorkspace;

/**
 * Internal implementation of {@link OmniEclipseWorkspace}.
 *
 * @author Stefan Oehme
 */
public final class DefaultOmniEclipseWorkspace implements OmniEclipseWorkspace {

    private final Map<GradleBuildIdentifier, OmniEclipseProject> models;
    private final Map<GradleBuildIdentifier, Exception> failures;
    private final Set<GradleBuildIdentifier> identifiers;

    private DefaultOmniEclipseWorkspace(Map<GradleBuildIdentifier, OmniEclipseProject> models, Map<GradleBuildIdentifier, Exception> failures) {
        this.models = ImmutableMap.copyOf(models);
        this.failures = ImmutableMap.copyOf(failures);
        this.identifiers = ImmutableSet.<GradleBuildIdentifier>builder().addAll(models.keySet()).addAll(failures.keySet()).build();
    }

    @Override
    public Map<GradleBuildIdentifier, OmniEclipseProject> getModels() {
        return this.models;
    }

    @Override
    public Map<GradleBuildIdentifier, Exception> getFailures() {
        return this.failures;
    }

    @Override
    public Set<GradleBuildIdentifier> getIdentifiers() {
        return this.identifiers;
    }

    @Override
    public Optional<OmniEclipseProject> tryFind(Spec<? super OmniEclipseProject> predicate) {
        return Iterables.tryFind(this.models.values(), toPredicate(predicate));
    }

    @Override
    public List<OmniEclipseProject> filter(Spec<? super OmniEclipseProject> predicate) {
        return FluentIterable.from(this.models.values()).filter(toPredicate(predicate)).toList();
    }

    private <T> Predicate<? super T> toPredicate(final Spec<? super T> spec) {
        return new Predicate<T>() {
            @Override
            public boolean apply(T input) {
                return spec.isSatisfiedBy(input);
            }
        };
    }

    public static OmniEclipseWorkspace from(Map<GradleBuildIdentifier, OmniEclipseProject> models, Map<GradleBuildIdentifier, Exception> failures) {
        return new DefaultOmniEclipseWorkspace(models, failures);
    }
}
