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

package com.gradleware.tooling.toolingutils.binding;

import com.google.common.base.Optional;

/**
 * Validates a given value and returns a validation error message if and only if the validation fails.
 *
 * @param <T> the type of the validated values
 */
public interface Validator<T> {

    /**
     * Validates the given value.
     *
     * @param value the value to validate
     * @return {@code Optional} that contains the error message iff the validation fails
     */
    Optional<String> validate(T value);

}
