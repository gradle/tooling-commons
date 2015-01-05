package com.gradleware.tooling.testing;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts the Gradle version from a given object, typically from a URI string.
 */
public final class GradleVersionExtractor {

    // matches the version part of a Gradle distribution location, for example:
    // https://services.gradle.org/distributions/gradle-2.2-20141018225750+0000-bin.zip
    // https://services.gradle.org/distributions/gradle-2.2.1-rc-1-bin.zip
    // https://services.gradle.org/distributions/gradle-2.2.1-bin.zip
    private static final Pattern PATTERN = Pattern.compile("(\\d+\\.\\d+[\\.\\-\\+\\w]*)\\-bin\\.zip");

    private GradleVersionExtractor() {
    }

    /**
     * Returns the Gradle version referenced by the given distribution location.
     *
     * @param uri the distribution location
     * @return the Gradle version of the distribution
     */
    public static Optional<String> getVersion(URI uri) {
        Preconditions.checkNotNull(uri);

        Matcher m = PATTERN.matcher(uri.toString());
        if (m.find()) {
            return Optional.of(m.group(1));
        } else {
            return Optional.absent();
        }
    }

}
