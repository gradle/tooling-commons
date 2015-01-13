package com.gradleware.tooling.domain.model.generic;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Holds the actual model field/value pairs.
 */
public final class ModelValueContainer<FIELDS> {

    private final Map<ModelField, Object> fields;

    public ModelValueContainer() {
        this.fields = Maps.newHashMap();
    }

    public <T> void put(ModelField<T, FIELDS> field, T value) {
        this.fields.put(field, value);
    }

    public <T> T get(ModelField<T, FIELDS> field) {
        if (isPresent(field)) {
            Object value = this.fields.get(field);

            @SuppressWarnings("unchecked")
            T castValue = (T) value;
            return castValue;
        } else {
            return field.getDefaultValue();
        }
    }

    public boolean isPresent(ModelField<?, FIELDS> field) {
        return this.fields.containsKey(field);
    }

}
