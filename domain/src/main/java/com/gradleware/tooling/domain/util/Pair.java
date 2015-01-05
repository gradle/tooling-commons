package com.gradleware.tooling.domain.util;

import java.io.Serializable;

/**
 * Holds two values.
 *
 * @param <S> the type of the first value
 * @param <T> the type of the second value
 */
public final class Pair<S, T> implements Serializable {

    private static final long serialVersionUID = 1;

    private final S first;
    private final T second;

    public Pair(S first, T second) {
        this.first = first;
        this.second = second;
    }

    public S getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }

}
