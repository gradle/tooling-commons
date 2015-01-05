package com.gradleware.tooling.toolingapi;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.gradle.tooling.GradleConnector;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a Gradle distribution that can be located locally or remotely, be a fixed version, or be project-specific.
 *
 * @since 2.3
 */
public final class GradleDistribution {

    private final File localInstallationDir;
    private final URI remoteDistributionUri;
    private final String version;

    private GradleDistribution(File localInstallationDir, URI remoteDistributionUri, String version) {
        this.localInstallationDir = localInstallationDir;
        this.remoteDistributionUri = remoteDistributionUri;
        this.version = version;

        checkNoMoreThanOneAttributeSet(localInstallationDir, remoteDistributionUri, version);
    }

    private void checkNoMoreThanOneAttributeSet(File localInstallationDir, URI remoteDistributionUri, String version) {
        int count = 0;
        List<Object> items = Arrays.<Object>asList(localInstallationDir, remoteDistributionUri, version);
        for (Object item : items) {
            if (item != null) {
                count++;
            }
        }
        Preconditions.checkArgument(count <= 1, "Attributes of more than one distribution type specified.");
    }

    /**
     * Configures the specified connector with this distribution.
     *
     * @param connector the connector to configure
     * @since 2.3
     */
    public void apply(GradleConnector connector) {
        Preconditions.checkNotNull(connector);
        if (localInstallationDir != null) {
            connector.useInstallation(localInstallationDir);
        } else if (remoteDistributionUri != null) {
            connector.useDistribution(remoteDistributionUri);
        } else if (version != null) {
            connector.useGradleVersion(version);
        } else {
            connector.useBuildDistribution();
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        GradleDistribution that = (GradleDistribution) other;
        return Objects.equal(localInstallationDir, that.localInstallationDir) &&
                Objects.equal(remoteDistributionUri, that.remoteDistributionUri) &&
                Objects.equal(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(localInstallationDir, remoteDistributionUri, version);
    }

    @Override
    public String toString() {
        if (localInstallationDir != null) {
            return String.format("Gradle installation %s", localInstallationDir.getAbsolutePath());
        } else if (remoteDistributionUri != null) {
            return String.format("Gradle distribution %s", remoteDistributionUri);
        } else if (version != null) {
            return String.format("Gradle version %s", version);
        } else {
            return "Gradle version of target build";
        }
    }

    /**
     * Creates a reference to a local Gradle installation.
     *
     * @param installationDir the local Gradle installation directory to use
     * @return a new distribution instance
     * @see org.gradle.tooling.GradleConnector#useInstallation(java.io.File)
     * @since 2.3
     */
    public static GradleDistribution forLocalInstallation(File installationDir) {
        Preconditions.checkNotNull(installationDir);
        return new GradleDistribution(installationDir, null, null);
    }

    /**
     * Creates a reference to a remote Gradle distribution. The appropriate distribution is downloaded and installed into the user's Gradle home directory.
     *
     * @param distributionUri the remote Gradle distribution location to use
     * @return a new distribution instance
     * @see org.gradle.tooling.GradleConnector#useDistribution(java.net.URI)
     * @since 2.3
     */
    public static GradleDistribution forRemoteDistribution(URI distributionUri) {
        Preconditions.checkNotNull(distributionUri);
        return new GradleDistribution(null, distributionUri, null);
    }

    /**
     * Creates a reference to a specific version of Gradle. The appropriate distribution is downloaded and installed into the user's Gradle home directory.
     *
     * @param version the Gradle version to use
     * @return a new distribution instance
     * @see org.gradle.tooling.GradleConnector#useGradleVersion(String)
     * @since 2.3
     */
    public static GradleDistribution forVersion(String version) {
        Preconditions.checkNotNull(version);
        return new GradleDistribution(null, null, version);
    }

    /**
     * Creates a reference to a project-specific version of Gradle.
     *
     * @return a new distribution instance
     * @see org.gradle.tooling.GradleConnector#useBuildDistribution()
     * @since 2.3
     */
    public static GradleDistribution fromBuild() {
        return new GradleDistribution(null, null, null);
    }

}
