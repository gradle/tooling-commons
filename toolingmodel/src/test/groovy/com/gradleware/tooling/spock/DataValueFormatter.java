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

package com.gradleware.tooling.spock;

/**
 * Formats an arbitrary value as a {@code String}.
 *
 * @author Etienne Studer
 */
public interface DataValueFormatter {

    /**
     * Formats the given value a {@code String}.
     *
     * @param input the value to format
     * @return the formatted value
     */
    String format(Object input);

    /**
     * Default implementation that converts an arbitrary value by calling its {@code toString} method.
     *
     * @see String#valueOf(Object)
     */
    final class DefaultDataValueFormatter implements DataValueFormatter {

        @Override
        public String format(Object input) {
            return String.valueOf(input);
        }

    }

}
