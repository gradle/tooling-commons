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

import com.google.common.base.Function;

/**
 * Naive definition how to convert objects back and forth.
 * <p/>
 * This class was introduced because the Guava 15.0 dependency has to be used. When upgrading to
 * Guava 18 this class should be replaced with the {@code com.google.common.base.Converter} type.
 *
 * @param <F> the type to convert back from
 * @param <T> the type to convert forward to
 *
 * @author Donát Csikós
 */
public abstract class Converter<F, T> implements Function<F, T> {

    public abstract F revert(T arg);

    public static <I> Converter<I, I> identity() {
        return new Converter<I, I>() {

            @Override
            public I apply(I source) {
                return source;
            }

            @Override
            public I revert(I source) {
                return source;
            }

        };
    }

}
