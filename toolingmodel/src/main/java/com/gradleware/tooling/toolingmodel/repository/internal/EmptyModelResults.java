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

import java.util.Iterator;

import org.gradle.tooling.connection.ModelResult;
import org.gradle.tooling.connection.ModelResults;

import com.google.common.collect.Iterators;

/**
 * An empty model result, when there are no builds in the composite. Workaround for the fact that
 * composite build currently fails when it is empty.
 *
 * @param <T> the type of models contained
 *
 * @author Stefan Oehme
 */
public class EmptyModelResults<T> implements ModelResults<T> {

    @Override
    public Iterator<ModelResult<T>> iterator() {
        return Iterators.emptyIterator();
    }

}
