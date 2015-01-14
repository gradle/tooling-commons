package com.gradleware.tooling.domain.util;

import java.io.Serializable;

/**
 * Holds two values.
 *
 * @param <S> the type of the first value
 * @param <T> the type of the second value
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public final class Pair<S, T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final S first;
    private final T second;

    public Pair(S first, T second) {
        this.first = first;
        this.second = second;
    }

    public S getFirst() {
        return this.first;
    }

    public T getSecond() {
        return this.second;
    }

}
