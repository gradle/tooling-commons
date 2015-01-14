package com.gradleware.tooling.spock;

/**
 * Formats an arbitrary value as a {@code String}.
 */
public interface DataValueFormatter {

    /**
     * Formats the given value a {@code String}.
     *
     * @param input the value to format
     * @return the formatted value
     */
    String format(Object input);

    /**
     * Default implementation that converts an arbitrary value by calling its {@code toString} method.
     *
     * @see String#valueOf(Object)
     */
    final class DefaultDataValueFormatter implements DataValueFormatter {

        @Override
        public String format(Object input) {
            return String.valueOf(input);
        }

    }

}
