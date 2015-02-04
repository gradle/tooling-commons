package com.gradleware.tooling.toolingutils.binding;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Describes a property that contains a value of a given type. The value can be validated through the associated {@link Validator}.
 */
public final class Property<T> {

    public static final Object LOCK = new Object();

    private T value;
    private final Validator<T> validator;
    private final Set<ValidationListener> listeners;

    private Property(Validator<T> validator) {
        this.validator = Preconditions.checkNotNull(validator);
        this.listeners = new LinkedHashSet<ValidationListener>();
    }

    /**
     * Returns the current property value.
     *
     * @return the current property value, can be null
     */
    public T getValue() {
        return this.value;
    }

    /**
     * Sets the given property value. The value is set regardless of whether it is valid or not. The new value is validated and the attached validation listeners are notified about
     * the outcome of the validation.
     *
     * @param value the property value to set, can be null
     * @return {@code Optional} that contains the error message iff the validation has failed
     */
    public Optional<String> setValue(T value) {
        this.value = value;
        Optional<String> errorMessage = validate();
        for (ValidationListener listener : getListeners()) { // do not invoke listeners in synchronized block
            listener.validationTriggered(this, errorMessage);
        }
        return errorMessage;
    }

    /**
     * Validates the property value.
     *
     * @return {@code Optional} that contains the error message iff the validation has failed
     */
    public Optional<String> validate() {
        return this.validator.validate(this.value);
    }

    /**
     * Returns whether the property value is valid or not.
     *
     * @return {@code true} if the property value is valid, {@code false} otherwise
     */
    public boolean isValid() {
        return !validate().isPresent();
    }

    /**
     * Adds the given validation listener.
     *
     * @param listener the listener to add
     */
    public void addValidationListener(ValidationListener listener) {
        synchronized (LOCK) {
            this.listeners.add(listener);
        }
    }

    /**
     * Removes the given validation listener.
     *
     * @param listener the listener to remove
     */
    public void removeValidationListener(ValidationListener listener) {
        synchronized (LOCK) {
            this.listeners.remove(listener);
        }
    }

    private ImmutableList<ValidationListener> getListeners() {
        synchronized (LOCK) {
            return ImmutableList.copyOf(this.listeners);
        }
    }

    /**
     * Creates a new instance.
     *
     * @param validator the validator through which the the property value can be validated
     * @return the new instance
     */
    public static <T> Property<T> create(Validator<T> validator) {
        return new Property<T>(validator);
    }

}
