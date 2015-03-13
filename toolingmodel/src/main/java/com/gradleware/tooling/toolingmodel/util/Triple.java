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

import java.io.Serializable;

/**
 * Holds three values.
 *
 * @param <S> the type of the first value
 * @param <T> the type of the second value
 * @param <U> the type of the third value
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public final class Triple<S , T , U > implements Serializable {

    private static final long serialVersionUID = 1L;

    private final S first;
    private final T second;
    private final U third;

    public Triple(S first, T second, U third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public S getFirst() {
        return this.first;
    }

    public T getSecond() {
        return this.second;
    }

    public U getThird() {
        return this.third;
    }

}
