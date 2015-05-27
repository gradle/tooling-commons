/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 \(the "License"\);
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

package com.gradleware.tooling.toolingutils;

import com.google.common.base.Optional;
import com.gradleware.tooling.toolingutils.binding.Property;
import com.gradleware.tooling.toolingutils.binding.Validator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * JUnit based test that is useful for manual testing of the test integration in Buildship
 * when using tooling commons as the project under test.
 */
public class PropertyJUnitTest {

    @Test
    public void getValue() {
        final String validationErrorMessage = "validation error message";
        Property<String> property = Property.create(new Validator<String>() {
            @Override
            public Optional<String> validate(String value) {
                return value != null ? Optional.<String>absent() : Optional.of(validationErrorMessage);
            }
        });

        assertEquals(Optional.of(validationErrorMessage), property.validate());

        property.setValue("foo");
        assertEquals(Optional.<String>absent(), property.validate());
    }

}
