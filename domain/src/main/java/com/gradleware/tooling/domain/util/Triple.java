package com.gradleware.tooling.domain.util;

import java.io.Serializable;

/**
 * Holds three values.
 *
 * @param <S> the type of the first value
 * @param <T> the type of the second value
 * @param <U> the type of the third value
 */
public final class Triple<S, T, U> implements Serializable {

    private static final long serialVersionUID = 1;

    private final S first;
    private final T second;
    private final U third;

    public Triple(S first, T second, U third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public S getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }

    public U getThird() {
        return third;
    }

}
