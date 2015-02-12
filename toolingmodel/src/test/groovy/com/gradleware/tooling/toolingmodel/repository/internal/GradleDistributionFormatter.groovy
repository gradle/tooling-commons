package com.gradleware.tooling.toolingmodel.repository.internal

import com.gradleware.tooling.spock.DataValueFormatter
import com.gradleware.tooling.testing.GradleVersionExtractor
import com.gradleware.tooling.toolingclient.GradleDistribution

/**
 * Custom formatting for the {@link GradleDistribution} data value used in Spock test parameterization. The main purpose is to have shorter string representations than what is
 * returned by {@link GradleDistribution#toString()}. The main reason being that TeamCity truncates long test names which leads to tests with identical names and a false test
 * count is displayed as a consequence.
 */
public final class GradleDistributionFormatter implements DataValueFormatter {

    @SuppressWarnings("GroovyAccessibility")
    @Override
    public String format(Object input) {
        if (input instanceof GradleDistribution) {
            if (input.localInstallationDir != null) {
                File dir = input.localInstallationDir
                dir.absolutePath.substring(dir.absolutePath.lastIndexOf(File.separatorChar) + 1) // last path segment
            } else if (input.remoteDistributionUri != null) {
                GradleVersionExtractor.getVersion(input.remoteDistributionUri).get()
            } else if (input.version != null) {
                input.version
            } else {
                String.valueOf(input);
            }
        } else {
            String.valueOf(input);
        }
    }

}
