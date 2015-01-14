package com.gradleware.tooling.domain.generic;

/**
 * Model that never contains any fields.
 */
public final class EmptyModel<FIELDS> implements Model<FIELDS> {

    private final ModelValueContainer<FIELDS> valueHolder;

    public EmptyModel() {
        this.valueHolder = new ModelValueContainer<FIELDS>();
    }

    @Override
    public <T> T get(ModelField<T, FIELDS> field) {
        return this.valueHolder.get(field);
    }

    @Override
    public boolean isPresent(ModelField<?, FIELDS> field) {
        return this.valueHolder.isPresent(field);
    }

}
