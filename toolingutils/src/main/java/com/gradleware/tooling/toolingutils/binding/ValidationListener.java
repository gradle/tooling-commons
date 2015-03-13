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
 * Validation listeners are invoked when a {@link Property} has been validated.
 */
public interface ValidationListener {

    /**
     * Invoked when the {@link Property} instance to which this listeners is attached has been validated.
     *
     * @param source the property whose value has been validated
     * @param validationErrorMessage {@code Optional} that contains the error message iff the validation fails
     */
    void validationTriggered(Property<?> source, Optional<String> validationErrorMessage);

}
