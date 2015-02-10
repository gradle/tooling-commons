package com.gradleware.tooling.toolingutils.binding;

import com.google.common.base.Optional;

/**
 * Contains helper methods related to {@link Validator} instances.
 */
public final class Validators {

    private static final Validator<Object> NO_OP = new Validator<Object>() {
        @Override
        public Optional<String> validate(Object value) {
            return Optional.absent();
        }
    };

    private Validators() {
    }

    public static <T> Validator<T> noOp() {
        @SuppressWarnings("unchecked")
        Validator<T> noOp = (Validator<T>) NO_OP;
        return noOp;
    }

}
