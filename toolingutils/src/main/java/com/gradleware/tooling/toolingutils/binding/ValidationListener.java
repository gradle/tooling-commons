package com.gradleware.tooling.toolingutils.binding;

import com.google.common.base.Optional;

/**
 * Validation listeners are invoked when a {@link Property} has been validated.
 */
public interface ValidationListener {

    /**
     * Invoked when the {@link Property} instance to which this listeners is attached has been validated.
     *
     * @param source the property whose value has been validated
     * @param validationErrorMessage {@code Optional} that contains the error message iff the validation fails
     */
    void validationTriggered(Property<?> source, Optional<String> validationErrorMessage);

}
