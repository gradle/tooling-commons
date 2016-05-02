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

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.UnsupportedMethodException;
import org.gradle.tooling.model.eclipse.ClasspathAttribute;
import org.gradle.tooling.model.eclipse.EclipseClasspathEntry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import com.gradleware.tooling.toolingmodel.OmniClasspathAttribute;
import com.gradleware.tooling.toolingmodel.OmniClasspathEntry;

/**
 * Default implementation of {@link OmniClasspathEntry}.
 *
 * @author Stefan Oehme
 *
 */
abstract class AbstractOmniClasspathEntry implements OmniClasspathEntry {

    private List<OmniClasspathAttribute> classpathAttributes;

    AbstractOmniClasspathEntry(List<OmniClasspathAttribute> classpathAttributes) {
        this.classpathAttributes = ImmutableList.copyOf(classpathAttributes);
    }

    @Override
    public List<OmniClasspathAttribute> getClasspathAttributes() {
        return this.classpathAttributes;
    }

    protected static List<OmniClasspathAttribute> getClasspathAttributes(EclipseClasspathEntry entry) {
        DomainObjectSet<? extends ClasspathAttribute> attributes;
        try {
            attributes = entry.getClasspathAttributes();
        } catch (UnsupportedMethodException e) {
            return ImmutableList.of();
        }
        Builder<OmniClasspathAttribute> builder = ImmutableList.builder();
        for (ClasspathAttribute attribute : attributes) {
            builder.add(DefaultOmniClasspathAttribute.from(attribute));
        }
        return builder.build();
    }

}
