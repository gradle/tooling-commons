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

import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.connection.ModelResult;
import org.gradle.tooling.connection.ModelResults;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

/**
 * A converter that converts the individual models inside a {@link ModelResults} instance using the
 * given delegate.
 *
 * @param <T> the source type
 * @param <U> the target type
 *
 * @author Stefan Oehme
 *
 */
final class ModelResultsConverter<T, U> extends BaseConverter<ModelResults<T>, ModelResults<U>> {

    private final Converter<T, U> resultConverter;

    ModelResultsConverter(Converter<T, U> resultConverter) {
        this.resultConverter = resultConverter;
    }

    @Override
    public ModelResults<U> apply(ModelResults<T> input) {
        return new ConvertedModelResults(input);
    }

    /**
     * A {@link ModelResults} implementation whose iterator returns converted {@link ModelResult}s.
     *
     * @author Stefan Oehme
     *
     */
    private final class ConvertedModelResults implements ModelResults<U> {

        private final ModelResults<T> results;

        private ConvertedModelResults(ModelResults<T> input) {
            this.results = input;
        }

        @Override
        public Iterator<ModelResult<U>> iterator() {
            return Iterators.transform(this.results.iterator(), new Function<ModelResult<T>, ModelResult<U>>() {

                @SuppressWarnings("unchecked")
                @Override
                public ModelResult<U> apply(ModelResult<T> input) {
                    if (input.getFailure() == null) {
                        return new TransformedModelResult(input);
                    } else {
                        return (ModelResult<U>) input;
                    }
                }

            });
        }

        /**
         * A {@link ModelResult} whose model is converted.
         *
         * @author Stefan Oehme
         *
         */
        private final class TransformedModelResult implements ModelResult<U> {

            private final ModelResult<T> result;

            private TransformedModelResult(ModelResult<T> input) {
                this.result = input;
            }

            @Override
            public U getModel() throws GradleConnectionException {
                return ModelResultsConverter.this.resultConverter.apply(this.result.getModel());
            }

            @Override
            public GradleConnectionException getFailure() {
                return this.result.getFailure();
            }

        }

    }

}
