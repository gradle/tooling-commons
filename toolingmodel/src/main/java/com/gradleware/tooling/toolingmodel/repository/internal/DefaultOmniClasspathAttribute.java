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

import org.gradle.tooling.model.eclipse.ClasspathAttribute;

import com.google.common.base.Preconditions;

import com.gradleware.tooling.toolingmodel.OmniClasspathAttribute;

/**
 * Default implementation of {@link OmniClasspathAttribute}.
 *
 * @author Stefan Oehme
 *
 */
final class DefaultOmniClasspathAttribute implements OmniClasspathAttribute {

    private String name;
    private String value;

    DefaultOmniClasspathAttribute(String name, String value) {
        this.name = Preconditions.checkNotNull(name);
        this.value = Preconditions.checkNotNull(value);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    static OmniClasspathAttribute from(ClasspathAttribute attribute) {
        return new DefaultOmniClasspathAttribute(attribute.getName(), attribute.getValue());
    }

}
