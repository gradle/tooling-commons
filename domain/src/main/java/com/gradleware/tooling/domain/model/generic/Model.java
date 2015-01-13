package com.gradleware.tooling.domain.model.generic;

/**
 * A model consists of a set of field/value pairs.
 *
 * @param <FIELDS> the type of the class that enumerates the fields accessible on this model
 */
public interface Model<FIELDS> {

    /**
     * Returns the value for the given field.
     *
     * @param field the field
     * @param <T> the type of the value
     * @return the value
     */
    <T> T get(ModelField<T, FIELDS> field);

    /**
     * Returns {@code true} if the given field is present in this model, regardless whether the value is null or not.
     *
     * @param field the field to query
     * @return {@code true} if the field is present in this model
     */
    boolean isPresent(ModelField<?, FIELDS> field);

}
