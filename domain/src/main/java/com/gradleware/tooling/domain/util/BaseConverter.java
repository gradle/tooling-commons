package com.gradleware.tooling.domain.util;

import com.google.common.base.Converter;

/**
 * Implementation of the abstract {@link Converter} class that throws a {@code UnsupportedOperationException} for each implemented method. Useful when only one direction of the
 * conversion needs to be implemented.
 */
public abstract class BaseConverter<A, B> extends Converter<A, B> {

    /**
     * @throws UnsupportedOperationException unless overridden in sub class
     * @inheritDoc
     */
    @Override
    protected B doForward(A a) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException unless overridden in sub class
     * @inheritDoc
     */
    @Override
    protected A doBackward(B b) {
        throw new UnsupportedOperationException();
    }

}
