package com.gradleware.tooling.toolingclient;

/**
 * Represents an operation that accepts a single input argument and returns no result. Unlike most other functional interfaces, {@code Consumer} is expected to operate via
 * side-effects. The {@code Consumer} interface is similar to the one that ships with Java 8.
 *
 * @param <T> the type of the consumed input
 */
public interface Consumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param input the input argument
     */
    void accept(T input);

}
