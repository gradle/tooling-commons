package com.gradleware.tooling.spock;

/**
 * Formats values to strings.
 */
public interface DataValueFormatter {

    String format(Object input);

    /**
     * Default implementation that converts the passed values by calling their toString method.
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
