package com.gradleware.tooling.domain.util;

import com.google.common.base.Converter;

/**
 * Implementation of the abstract {@link Converter} class that throws a {@code UnsupportedOperationException} for each implemented method. Useful when only one direction of the
 * conversion needs to be implemented.
 */
public abstract class BaseConverter<A, B> extends Converter<A, B> {

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException unless overridden in sub class
     */
    @Override
    protected B doForward(A a) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException unless overridden in sub class
     */
    @Override
    protected A doBackward(B b) {
        throw new UnsupportedOperationException();
    }

}
