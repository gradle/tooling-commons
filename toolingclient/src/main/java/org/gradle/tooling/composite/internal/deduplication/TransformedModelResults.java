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
package org.gradle.tooling.composite.internal.deduplication;

import java.util.Iterator;

import org.gradle.tooling.composite.ModelResult;
import org.gradle.tooling.composite.ModelResults;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

public class TransformedModelResults<S, T> implements ModelResults<T> {

    private final ModelResults<S> delegate;
    private final Function<? super ModelResult<S>, ? extends ModelResult<T>> transformation;


    public TransformedModelResults(ModelResults<S> delegate, Function<? super ModelResult<S>, ? extends ModelResult<T>> transformation) {
        this.delegate = delegate;
        this.transformation = transformation;
    }

    @Override
    public Iterator<ModelResult<T>> iterator() {
        return Iterators.transform(this.delegate.iterator(), this.transformation);
    }

}