package com.gradleware.tooling.toolingmodel;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

/**
 * A hierarchical model belongs to a parent model, unless it is the root model, and may itself contain child models. All models in the hierarchy are of the same type.
 */
public interface HierarchicalModel<T extends HierarchicalModel<T>> {

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
    ImmutableList<T> getChildren();

    /**
     * Returns this model and all the nested child models in its hierarchy.
     *
     * @return this model and all the nested child models in its hierarchy
     */
    ImmutableList<T> getAll();

    /**
     * Returns all models that match the given criteria.
     *
     * @param predicate the criteria to match
     * @return the matching models
     */
    ImmutableList<T> filter(Predicate<? super T> predicate);

    /**
     * Returns the first model that matches the given criteria, if any.
     *
     * @param predicate the criteria to match
     * @return the matching model, if any
     */
    Optional<T> tryFind(Predicate<? super T> predicate);

}
