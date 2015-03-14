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

import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingmodel.OmniJavaEnvironment;
import org.gradle.tooling.model.build.JavaEnvironment;

import java.io.File;
import java.util.List;

/**
 * Default implementation of the {@link OmniJavaEnvironment} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniJavaEnvironment implements OmniJavaEnvironment {

    private final File javaHome;
    private final ImmutableList<String> jvmArguments;

    private DefaultOmniJavaEnvironment(File javaHome, List<String> jvmArguments) {
        this.javaHome = javaHome;
        this.jvmArguments = ImmutableList.copyOf(jvmArguments);
    }

    @Override
    public File getJavaHome() {
        return this.javaHome;
    }

    @Override
    public ImmutableList<String> getJvmArguments() {
        return this.jvmArguments;
    }

    public static DefaultOmniJavaEnvironment from(JavaEnvironment javaEnvironment) {
        return new DefaultOmniJavaEnvironment(javaEnvironment.getJavaHome(), javaEnvironment.getJvmArguments());
    }

}
