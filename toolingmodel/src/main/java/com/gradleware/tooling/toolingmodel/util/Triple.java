package com.gradleware.tooling.toolingmodel.util;

import java.io.Serializable;

/**
 * Holds three values.
 *
 * @param <S> the type of the first value
 * @param <T> the type of the second value
 * @param <U> the type of the third value
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public final class Triple<S , T , U > implements Serializable {

    private static final long serialVersionUID = 1L;

    private final S first;
    private final T second;
    private final U third;

    public Triple(S first, T second, U third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public S getFirst() {
        return this.first;
    }

    public T getSecond() {
        return this.second;
    }

    public U getThird() {
        return this.third;
    }

}
