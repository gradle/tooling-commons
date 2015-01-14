package com.gradleware.tooling.domain.util;

/**
 * Holds three values.
 *
 * @param <S> the type of the first value
 * @param <T> the type of the second value
 * @param <U> the type of the third value
 */
public final class Triple<S, T, U> {

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
