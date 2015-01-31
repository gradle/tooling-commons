package com.gradleware.tooling.toolingutils.binding;

import com.google.common.base.Optional;

/**
 * Validates a given value and returns a validation error message if and only if the validation fails.
 */
public interface Validator<T> {

    /**
     * Validates the given value.
     *
     * @param value the value to validate
     * @return {@code Optional} that contains the error message iff the validation fails
     */
    Optional<String> validate(T value);

}
