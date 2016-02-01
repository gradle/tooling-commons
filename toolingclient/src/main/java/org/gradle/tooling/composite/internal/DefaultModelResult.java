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

package org.gradle.tooling.composite.internal;

import org.gradle.tooling.composite.ModelResult;

/**
 * The default implementation of a model result.
 * @param <T> the model type
 * @author Benjamin Muschko
 */
public final class DefaultModelResult<T> implements ModelResult<T> {
    private final T model;

    public DefaultModelResult(T model) {
        this.model = model;
    }

    @Override
    public T getModel() {
        return this.model;
    }
}