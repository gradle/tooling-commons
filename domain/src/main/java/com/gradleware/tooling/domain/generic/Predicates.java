package com.gradleware.tooling.domain.generic;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;

/**
 * Factory for commonly used predicates.
 */
public final class Predicates {

    public static <T, FIELDS> Predicate<? super Model<FIELDS>> isEqual(final ModelField<T, FIELDS> field, final T value) {
        return new Predicate<Model<FIELDS>>() {
            @Override
            public boolean apply(Model<FIELDS> input) {
                return Objects.equal(input.get(field), value);
            }
        };
    }

    private Predicates() {
    }

}
