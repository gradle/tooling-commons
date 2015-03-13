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

package com.gradleware.tooling.toolingmodel;

import com.google.common.base.Optional;
import com.gradleware.tooling.toolingutils.ImmutableCollection;
import org.gradle.api.specs.Spec;

import java.util.List;

/**
 * A hierarchical model belongs to a parent model, unless it is the root model, and may itself contain child models. All models in the hierarchy are of the same type.
 */
public interface HierarchicalModel<T extends HierarchicalModel<T>> {

    /**
     * Returns the root model of this model.
     *
     * @return the root model, never null
     */
    T getRoot();

    /**
     * Returns the parent model of this model.
     *
     * @return the parent model, can be null
     */
    T getParent();

    /**
     * Returns the immediate child models of this model.
     *
     * @return the immediate child models of this model
     */
    @ImmutableCollection
    List<T> getChildren();

    /**
     * Returns this model and all the nested child models in its hierarchy.
     *
     * @return this model and all the nested child models in its hierarchy
     */
    @ImmutableCollection
    List<T> getAll();

    /**
     * Returns all models that match the given criteria.
     *
     * @param predicate the criteria to match
     * @return the matching models
     */
    @ImmutableCollection
    List<T> filter(Spec<? super T> predicate);

    /**
     * Returns the first model that matches the given criteria, if any.
     *
     * @param predicate the criteria to match
     * @return the matching model, if any
     */
    Optional<T> tryFind(Spec<? super T> predicate);

}
