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

import org.gradle.tooling.internal.adapter.ProtocolToModelAdapter;
import org.gradle.tooling.internal.consumer.converters.ConsumerTargetTypeProvider;

/**
 * Unpacks entities coming from the Tooling API and adapts them to internal models.
 *
 * Note: class was introduced to have access Gradle 3.0 features from a 2.x TAPI version.
 * It is required to let clients use this library from Java 6. Once The TAPI dependency is upgraded
 * to 3.0+ then this class should be deleted.
 *
 * @author Donat Csikos
 */
public final class ForwardCompatibilityConverter {
    private static final ProtocolToModelAdapter adapter = new ProtocolToModelAdapter(new ConsumerTargetTypeProvider());

    private ForwardCompatibilityConverter() {
    }

    public static <T> T convert(Object input, Class<T> outputType) {
        Object o = adapter.unpack(input);
        return adapter.adapt(outputType, o);
    }

}
