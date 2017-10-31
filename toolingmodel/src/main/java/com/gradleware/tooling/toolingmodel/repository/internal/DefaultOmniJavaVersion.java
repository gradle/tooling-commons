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

import com.gradleware.tooling.toolingmodel.OmniJavaVersion;
import org.gradle.api.JavaVersion;

/**
 * Default implementation of the {@link OmniJavaVersion} interface.
 *
 * @author Donát Csikós
 */
public final class DefaultOmniJavaVersion implements OmniJavaVersion {

    private final String name;

    private DefaultOmniJavaVersion(JavaVersion javaVersion) {
        this.name = javaVersion.isJava9Compatible() ? javaVersion.getMajorVersion() : javaVersion.toString();
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static DefaultOmniJavaVersion from(JavaVersion name) {
        return new DefaultOmniJavaVersion(name);
    }

}
