package com.gradleware.tooling.domain.generic;

import com.google.common.base.Supplier;
import com.google.common.reflect.TypeToken;

/**
 * Default implementation of the {@link ModelField } interface.
 */
public final class DefaultModelField<T, FIELDS> implements ModelField<T, FIELDS> {

    private final TypeToken<T> fieldValueType;
    private final TypeToken<FIELDS> fieldAggregatorType;
    private final Supplier<T> defaultValue;

    public DefaultModelField(TypeToken<T> fieldValueType, TypeToken<FIELDS> fieldAggregatorType) {
        this(fieldValueType, fieldAggregatorType, new Supplier<T>() {
            @Override
            public T get() {
                throw new UnsupportedOperationException("Default value not implemented for field: " + toString());
            }
        });
    }

    public DefaultModelField(TypeToken<T> fieldValueType, TypeToken<FIELDS> fieldAggregatorType, Supplier<T> defaultValue) {
        this.fieldAggregatorType = fieldAggregatorType;
        this.fieldValueType = fieldValueType;
        this.defaultValue = defaultValue;
    }

    @Override
    public TypeToken<T> getFieldValueType() {
        return this.fieldValueType;
    }

    @Override
    public TypeToken<FIELDS> getFieldAggregatorType() {
        return this.fieldAggregatorType;
    }

    @Override
    public T getDefaultValue() {
        return this.defaultValue.get();
    }

}
