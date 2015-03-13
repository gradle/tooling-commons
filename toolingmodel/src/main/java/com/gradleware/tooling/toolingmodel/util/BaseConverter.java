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

package com.gradleware.tooling.toolingmodel.util;

import com.google.common.base.Converter;

/**
 * Implementation of the abstract {@link Converter} class that throws a {@code UnsupportedOperationException} for each implemented method. Useful when only one direction of the
 * conversion needs to be implemented.
 *
 * @param <A> the type to convert forward to
 * @param <B> the type to convert back from
 */
public abstract class BaseConverter<A, B> extends Converter<A, B> {

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException unless overridden in sub class
     */
    @Override
    protected B doForward(A a) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException unless overridden in sub class
     */
    @Override
    protected A doBackward(B b) {
        throw new UnsupportedOperationException();
    }

}
