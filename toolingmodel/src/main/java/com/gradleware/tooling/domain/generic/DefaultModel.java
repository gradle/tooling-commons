package com.gradleware.tooling.domain.generic;

/**
 * Default implementation of the {@link Model } interface.
 */
public final class DefaultModel<FIELDS> implements Model<FIELDS> {

    private final ModelValueContainer<FIELDS> valueHolder;

    public DefaultModel() {
        this.valueHolder = new ModelValueContainer<FIELDS>();
    }

    public <V> void put(ModelField<V, FIELDS> field, V value) {
        this.valueHolder.put(field, value);
    }

    @Override
    public <V> V get(ModelField<V, FIELDS> field) {
        return this.valueHolder.get(field);
    }

    @Override
    public boolean isPresent(ModelField<?, FIELDS> field) {
        return this.valueHolder.isPresent(field);
    }

}
