package com.gradleware.tooling.toolingmodel.generic;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

/**
 * A hierarchical model is part of a parent/child hierarchy.
 *
 * @param <FIELDS> the type of the class that enumerates the fields accessible on this model
 */
public interface HierarchicalModel<FIELDS> extends Model<FIELDS> {

    /**
     * Returns the parent of this model.
     *
     * @return the parent
     */
    HierarchicalModel<FIELDS> getParent();

    /**
     * Returns the children of this model.
     *
     * @return the children
     */
    ImmutableList<HierarchicalModel<FIELDS>> getChildren();

    /**
     * Returns this model and all the nested child models in its hierarchy.
     *
     * @return this model and all the nested child models in its hierarchy
     */
    ImmutableList<HierarchicalModel<FIELDS>> getAll();

    /**
     * Returns all models that match the given criteria.
     *
     * @param predicate the criteria to match
     * @return the matching models
     */
    ImmutableList<HierarchicalModel<FIELDS>> filter(Predicate<? super HierarchicalModel<FIELDS>> predicate);

    /**
     * Returns the first model that matches the given criteria, if any.
     *
     * @param predicate the criteria to match
     * @return the matching model, if any
     */
    Optional<HierarchicalModel<FIELDS>> tryFind(Predicate<? super HierarchicalModel<FIELDS>> predicate);

}
