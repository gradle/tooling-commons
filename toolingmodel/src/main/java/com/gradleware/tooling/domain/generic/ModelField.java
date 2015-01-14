package com.gradleware.tooling.domain.generic;

import com.google.common.reflect.TypeToken;

/**
 * Represents a model field.
 */
public interface ModelField<T, FIELDS> {

    /**
     * The type of the value that this field can represent.
     *
     * @return the type of the value
     */
    TypeToken<T> getFieldValueType();

    /**
     * The type of the class that enumerates the fields of which this field is a part.
     *
     * @return the type of the class that enumerates the fields of which this field is a part
     */
    TypeToken<FIELDS> getFieldAggregatorType();

    /**
     * Returns the default value of this field.
     *
     * @return the default value
     */
    T getDefaultValue();

}
